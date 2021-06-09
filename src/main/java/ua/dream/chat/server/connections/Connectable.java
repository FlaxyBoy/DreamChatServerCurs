package ua.dream.chat.server.connections;

import org.jetbrains.annotations.NotNull;
import ua.dream.chat.network.netty.packet.out.PacketOut5UserData;

public abstract class Connectable {

    protected final UserConnection connection;

    protected Connectable(@NotNull UserConnection connection) {
        this.connection = connection;
    }

    public UserConnection getConnection() {
        return connection;
    }

    public void disconnect() {
        connection.setUser(null);
    }

    protected abstract PacketOut5UserData getData();
}
