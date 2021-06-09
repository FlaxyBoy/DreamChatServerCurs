package ua.dream.chat.server.commands;

import ua.dream.chat.server.messanger.ServerMessage;
import ua.dream.chat.server.user.Sender;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {


    private static final List<Command> commands = new ArrayList<>();

    public static void sendCommand(Sender sender , String message) {
        String[] commandSplit = message.split(" ");
        System.out.println("Dispatch command " + commandSplit[0].substring(1));
        Command command = findCommand(commandSplit[0].substring(1));
        if(command == null) {
            sender.sendMessage(new ServerMessage("command not found, /help to show command list"));
            return;
        }
        if(!command.getPermitted().contains(sender.getClass())) {
            sender.sendMessage(new ServerMessage("you dont have access to this command"));
            return;
        }
        String[] args = new String[commandSplit.length - 1];
        System.arraycopy(commandSplit, 1, args, 0, commandSplit.length - 1);
        if(!command.onExecute(sender , command , commandSplit[0].substring(1) , args)) {
            sender.sendMessage(new ServerMessage(command.getUsage()));
            return;
        }
    }

    public static List<Command> getCommands() {
        return commands;
    }

    private static Command findCommand(String command) {
        for(Command c : commands) {
            if(c.check(command)) return c;
        }
        return null;
    }

    public static void register(Command command) {
        commands.add(command);
    }

}
