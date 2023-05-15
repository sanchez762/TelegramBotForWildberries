package tg.bot.components;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import java.util.List;

public interface BotCommands {
    List<BotCommand> LIST_OF_COMMANDS = List.of(
            new BotCommand("/start", "get a welcome message"),
            new BotCommand("/mydata", "get your data stored"),
            new BotCommand("/deletedata", "delete my data"),
            new BotCommand("/help", "info how to use this bot"),
            new BotCommand("/settings", "set your preferences")
    );
    String HELP_TEXT = """
            This bot is created to demonstrate Spring capabilities.

            You can execute commands from the main menu on the left or by typing a command:

            Type /start to see a welcome message

            Type /mydata to see data stored about yourself

            Type /help to see this message again""";
}
