package ua.dream.chat.server.commands;

import ua.dream.chat.server.user.Sender;

public interface CommandExecutor {

    public boolean onExecute(Sender sender , Command command , String label , String[] args);
}
