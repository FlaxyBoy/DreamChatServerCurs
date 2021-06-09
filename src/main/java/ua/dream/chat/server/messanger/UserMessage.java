package ua.dream.chat.server.messanger;

import org.jetbrains.annotations.NotNull;
import ua.dream.chat.network.netty.packet.out.PacketOut8Message;
import ua.dream.chat.server.user.Sender;

public class UserMessage implements Message {

    private final String senderLogin;
    private final String senderDisplayName;
    private final String message;

    public UserMessage(Sender user , String message) {
        senderLogin = user.getLogin();
        senderDisplayName = user.getDisplayName();
        this.message = message;
    }

    @Override
    public @NotNull String getSenderLogin() {
        return senderLogin;
    }

    @Override
    public @NotNull String getDisplayName() {
        return senderDisplayName;
    }

    @Override
    public @NotNull String getMessage() {
        return message;
    }

    @Override
    public PacketOut8Message encode() {
        return new PacketOut8Message(message , senderLogin , senderDisplayName , PacketOut8Message.Type.USER_MESSAGE);
    }

}
