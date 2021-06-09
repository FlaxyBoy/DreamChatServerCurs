package ua.dream.chat.server.user;

import org.jetbrains.annotations.NotNull;
import ua.dream.chat.network.netty.packet.out.PacketOut5UserData;
import ua.dream.chat.server.connections.Connectable;
import ua.dream.chat.server.connections.UserConnection;
import ua.dream.chat.server.messanger.Message;
import ua.dream.chat.server.messanger.ServerMessage;

public class User extends Connectable implements Sender {

    private final String login;
    private String displayName;

    public User(@NotNull UserConnection connection , @NotNull String login , @NotNull String displayName) {
        super(connection);
        this.login = login;
        this.displayName = displayName;
    }

    public String getLogin() {
        return login;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void sendMessage(Message message) {
        connection.sendPacketMessage(message);
    }

    public void sendMessage(String message) { this.sendMessage(new ServerMessage(message));}

    @Override
    protected PacketOut5UserData getData() {
        return new PacketOut5UserData(this.getLogin(), this.getDisplayName());
    }
}
