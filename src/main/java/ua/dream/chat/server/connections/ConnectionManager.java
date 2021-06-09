package ua.dream.chat.server.connections;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.dream.chat.network.netty.frame.Packet;
import ua.dream.chat.server.user.Sender;
import ua.dream.chat.utils.validate.Check;

import java.util.*;
import java.util.stream.Collectors;

public class ConnectionManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private static final ConnectionManager instance = new ConnectionManager();

    public static ConnectionManager getInstance() {
        return instance;
    }

    private final Map<SocketChannel, UserConnection> connectionMap = Collections.synchronizedMap(new HashMap<>());

    private ConnectionManager() {

    }

    public synchronized List<UserConnection> getUserConnections(String name) {
        return connectionMap.values().stream().filter(connection -> {
            return connection.getUser() instanceof Sender && ((Sender) connection.getUser()).getLogin()
                    .equalsIgnoreCase(name);
        }).collect(Collectors.toList());
    }

    public synchronized UserConnection getConnection(SocketChannel channel) {
        return connectionMap.get(channel);
    }

    public synchronized void connect(ChannelHandlerContext context) {
        SocketChannel channel = (SocketChannel) context.channel();
        connectionMap.put(channel , new UserConnection(channel));
        LOGGER.info(channel.remoteAddress().getHostName() + ":" + channel.remoteAddress().getPort() + " is connected");
    }

    public synchronized void disconnect(ChannelHandlerContext context) {
        SocketChannel channel = (SocketChannel) context.channel();
        UserConnection connection = connectionMap.get(channel);
        Check.notNull(connection , "connection is not created");
        connectionMap.remove(channel);
        LOGGER.info(channel.remoteAddress().getHostName() + ":" + channel.remoteAddress().getPort() + " is disconnected");
    }

    public synchronized void broadcastClient(Packet<?> packet) {
        connectionMap.forEach((channel, userConnection) -> {
            if(userConnection.getUser() != null) channel.writeAndFlush(packet);
        });
    }

}
