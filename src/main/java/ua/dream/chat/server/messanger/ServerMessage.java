package ua.dream.chat.server.messanger;

import org.jetbrains.annotations.NotNull;
import ua.dream.chat.network.netty.packet.out.PacketOut8Message;

public class ServerMessage implements Message{

    private String message;

    public ServerMessage(String message) {
        this.message = message;
    }

    @Override
    public @NotNull String getSenderLogin() {
        return "SERVER NOTIFY";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "SERVER NOTIFY";
    }

    @Override
    public @NotNull String getMessage() {
        return message;
    }

    @Override
    public PacketOut8Message encode() {
        return new PacketOut8Message(message , "SERVER NOTIFY" , "SERVER NOTIFY" , PacketOut8Message.Type.SERVER_MESSAGE);
    }
}
