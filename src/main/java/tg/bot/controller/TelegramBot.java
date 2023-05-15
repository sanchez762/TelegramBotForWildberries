package tg.bot.controller;

import lombok.extern.slf4j.Slf4j;
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
import tg.bot.model.User;
import tg.bot.model.UserRepository;


@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot implements BotCommands {

    private final UserRepository userRepository;
    private final BotConfig config;

    public TelegramBot(BotConfig config, UserRepository userRepository) {
        this.config = config;
        this.userRepository = userRepository;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
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

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatID, update.getMessage().getChat().getFirstName());
                    break;

                case "/help":
                    sendMessage(chatID, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatID, "Not command");
            }
        }
    }

    private void registerUser(Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());

            userRepository.save(user);
            log.info("user saved: "+user);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name;
        log.info("Answer to user " + name);
        sendMessage(chatId, answer);

    }

    private void sendMessage(long chatID, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(textToSend);

        try{
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

}
