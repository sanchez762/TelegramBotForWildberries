package tg.bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tg.bot.components.BotCommands;
import tg.bot.config.BotConfig;
import tg.bot.model.Links;
import tg.bot.model.LinksRepository;
import tg.bot.model.User;
import tg.bot.model.UserRepository;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot implements BotCommands {

    private final HashMap<Long, String> mapOfRequest = new HashMap<>();
    private final UserRepository userRepository;
    private final LinksRepository linksRepository;
    private final BotConfig config;

    public TelegramBot(BotConfig config, UserRepository userRepository, LinksRepository linksRepository) {
        this.config = config;
        this.userRepository = userRepository;
        this.linksRepository = linksRepository;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка настройки команд бота: " + e.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatID = update.getMessage().getChatId();

            if(mapOfRequest.containsKey(chatID) && mapOfRequest.get(chatID).equals("Delete")){
                delete(chatID, messageText);
                return;
            }
            if(mapOfRequest.containsKey(chatID) && mapOfRequest.get(chatID).equals("Time")){
                setTime(chatID, messageText);
                return;
            }

            switch (messageText) {
                case "/start" -> {
                    registerUser(update.getMessage());
                    startCommandReceived(chatID, update.getMessage().getChat().getFirstName());
                }
                case "/help" -> sendMessage(chatID, HELP_TEXT);
                case "/delete" -> {
                    mapOfRequest.put(chatID, "Delete");
                    sendMessage(chatID, "Введите id товара для удаления:");
                }
                case "/mylinks" -> showLinks(chatID);
                case "/settings" -> {
                    mapOfRequest.put(chatID, "Time");
                    sendMessage(chatID, "Введите желаемое время рассылки по московскому времени, " +
                            "одним числом, в формате: (0-23)");
                }
                default -> {
                    if (messageText.contains("https://wildberries.") ||
                            messageText.contains("https://www.wildberries.")){
                        String url = messageText.substring(messageText.indexOf("https://"));
                        addLink(url, chatID);
                    } else sendMessage(chatID, "Команда не поддерживается");
                }
            }
        }
    }

    private void setTime(long chatID, String messageText) {
        try {
            userRepository.setTime(chatID, Integer.parseInt(messageText));
        } catch (NumberFormatException e) {
            log.error("Неверно указано время");
        }
        mapOfRequest.remove(chatID);
        sendMessage(chatID, "Время установлено");
    }

    private void delete(long chatID, String messageText) {
        try {
            linksRepository.deleteByLinkId(Long.valueOf(messageText));
            sendMessage(chatID, "Ссылка удалена");
        } catch (NumberFormatException e) {
            sendMessage(chatID, "Неверный id товара, попробуйте снова");
        }
        mapOfRequest.remove(chatID);
    }

    private void showLinks(long chatID) {
        List<Links> list = userRepository.findById(chatID).orElse(null).getList();
        for(Links link:list){
            try {
                sendMessage(chatID, Parser.parse(link.getLink()) + "\nID для удаления: " + link.getId());
            } catch (IOException | ParseException e) {
                log.error("Ошибка парсинга ссылки: " + e.getMessage());
            }
        }
    }

    private void addLink(String url, Long chatID) {
        User user = userRepository.getReferenceById(chatID);
        Links link = new Links();
        link.setLink(url);
        link.setUser(user);
        linksRepository.save(link);
        sendMessage(chatID, "Ссылка добавлена");
        try {
            sendMessage(chatID, Parser.parse(url) + "\nId для удаления: " + link.getId());
        } catch (IOException | ParseException e) {
            log.error("Ошибка парсинга ссылки: " + e.getMessage());
        }
    }

    private void registerUser(Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setTime(12);

            userRepository.save(user);
            log.info("Пользователь добавлен: "+user);
        }
    }

    private void startCommandReceived(long chatID, String name) {
        sendMessage(chatID, "Привет, " + name);
        sendMessage(chatID, HELP_TEXT);

    }

    private void sendMessage(long chatID, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(textToSend);

        try{
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    private void sendAuto(){
        int time = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).getHour();
        var listOfUser = userRepository.findAllByTime(time);

        for (User user:listOfUser) {

            List<Links> list = user.getList();
            for (Links link:list){
                try {
                    sendMessage(user.getChatId(), Parser.parse(link.getLink()));
                } catch (IOException | ParseException e) {
                    log.error("Ошибка парсинга ссылки: " + e.getMessage());
                }
            }
        }
    }
}
