package ua.dream.chat.server.user;

import ua.dream.chat.server.messanger.Message;

public interface Sender {

    public abstract void sendMessage(Message message);

    public abstract void sendMessage(String message);

    public abstract String getLogin();

    public abstract String getDisplayName();

}
