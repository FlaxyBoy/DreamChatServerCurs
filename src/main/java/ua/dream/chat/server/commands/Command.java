package ua.dream.chat.server.commands;

import ua.dream.chat.server.user.Sender;

import java.util.Arrays;
import java.util.List;

public class Command implements CommandExecutor {

    private String name;
    private String[] allies;
    private String description;
    private String usage;
    private List<Class<? extends Sender>> permitted;
    private CommandExecutor executor;


    public Command(String name , String[] allies , String description , String usage , Class<? extends Sender>... permitted) {
        this.name = name;
        this.allies = allies;
        this.description = description;
        this.usage = usage;
        this.permitted = Arrays.asList(permitted);
        executor = null;
    }

    public String[] getAllies() {
        return allies;
    }

    public Command setExecutor(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public List<Class<? extends Sender>> getPermitted() {
        return permitted;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public boolean check(String value) {
        if(name.equalsIgnoreCase(value)) return true;
        for(String s : allies) {
            if(s.equalsIgnoreCase(value)) return true;
        }
        return false;
    }


    @Override
    public boolean onExecute(Sender sender, Command command, String label, String[] args) {
        if(executor != null) return executor.onExecute(sender , command , label , args);
        return false;
    }

}
