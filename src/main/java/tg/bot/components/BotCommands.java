package tg.bot.components;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import java.util.List;

public interface BotCommands {
    List<BotCommand> LIST_OF_COMMANDS = List.of(
            new BotCommand("/mylinks", "Позволяет получить информацию по всем добавленым товарам"),
            /*new BotCommand("/deletedata", "delete my data"),*/
            new BotCommand("/help", "информация как пользоваться ботом")
            /*new BotCommand("/settings", "set your preferences")*/
    );
    String HELP_TEXT = """
            Этот бот позволяет автоматически получать информацию по товарам с сайта wildberries (наличие и цена).
            Для этого необходимо отправить боту ссылку на товар.
            Если товар имеет несколько цветов/форм необходимо выбрать цвет/форму так, как будто вы хотите положить этот товар в корзину.
            Аналогично нужно выбрать размер (если он имеется).
            Внимание! В мобильном приложении сначала необходимо добавить товар в избранное, затем нажать поделиться и отправить ссылку боту.

            Вы можете вызвать эти команды из основного меню:
            /mydata отобразить данные о всех добавленых товарах
            /help отобразить эту информацию снова""";
}
