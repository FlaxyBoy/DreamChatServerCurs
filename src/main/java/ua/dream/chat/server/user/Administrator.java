package ua.dream.chat.server.user;

import org.jetbrains.annotations.NotNull;
import ua.dream.chat.network.netty.packet.out.PacketOut5UserData;
import ua.dream.chat.server.connections.Connectable;
import ua.dream.chat.server.connections.UserConnection;
import ua.dream.chat.server.messanger.Message;
import ua.dream.chat.server.messanger.ServerMessage;

public class Administrator extends Connectable implements Sender {

    private final String login;

    public Administrator(@NotNull UserConnection connection , @NotNull String login) {
        super(connection);
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getDisplayName() {
        return "admin";
    }

    public void sendMessage(Message message) {
        connection.sendPacketMessage(message);
    }

    @Override
    public void sendMessage(String message) {
        this.sendMessage(new ServerMessage(message));
    }

    @Override
    protected PacketOut5UserData getData() {
        return new PacketOut5UserData(this.getLogin(), this.getDisplayName());
    }
}

