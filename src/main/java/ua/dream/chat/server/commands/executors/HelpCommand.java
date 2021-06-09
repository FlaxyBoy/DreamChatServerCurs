package ua.dream.chat.server.commands.executors;

import ua.dream.chat.server.commands.Command;
import ua.dream.chat.server.commands.CommandManager;
import ua.dream.chat.server.messanger.ServerMessage;
import ua.dream.chat.server.user.Administrator;
import ua.dream.chat.server.user.Sender;
import ua.dream.chat.server.user.User;

import java.util.concurrent.atomic.AtomicInteger;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", new String[]{"допомога"}, "show all commands", "/help", User.class , Administrator.class);
    }

    @Override
    public boolean onExecute(Sender sender, Command command, String label, String[] args) {
        AtomicInteger numbers = new AtomicInteger(1);
        CommandManager.getCommands().forEach(cmd -> {
            if(cmd.getPermitted().contains(sender.getClass()))
            sender.sendMessage(new ServerMessage(numbers.getAndIncrement() + ") " + cmd.getName() + " - " + cmd.getDescription()));
        });
        return true;
    }
}
