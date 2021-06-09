package ua.dream.chat.server.handlers;

import io.netty.channel.socket.SocketChannel;
import ua.dream.chat.network.netty.frame.PacketInHandler;
import ua.dream.chat.network.netty.packet.in.PacketIn1Register;
import ua.dream.chat.network.netty.packet.in.PacketIn2Login;
import ua.dream.chat.network.netty.packet.in.PacketIn7UserMessage;
import ua.dream.chat.network.netty.packet.out.PacketOut3RegistrationFailed;
import ua.dream.chat.network.netty.packet.out.PacketOut4LoginFailed;
import ua.dream.chat.server.commands.CommandManager;
import ua.dream.chat.server.connections.ConnectionManager;
import ua.dream.chat.server.connections.UserConnection;
import ua.dream.chat.server.messanger.LogMessage;
import ua.dream.chat.server.messanger.UserMessage;
import ua.dream.chat.server.user.Sender;
import ua.dream.chat.server.user.UserManager;
import ua.dream.chat.utils.validate.Check;

public final class PacketInHandlerImpl implements PacketInHandler {



    @Override
    public void handle(PacketIn1Register packet, SocketChannel channel) {
        UserConnection connection = getConnection(channel);
        if(connection.getUser() != null) {
            connection.sendPacket(new PacketOut3RegistrationFailed(
                    PacketOut3RegistrationFailed.Reason.USER_IS_AUTHORIZED
            ));
            return;
        }
        UserManager.registerUser(connection , packet.getUserName() , packet.getPassword());
    }

    @Override
    public void handle(PacketIn2Login packet, SocketChannel channel) {
        UserConnection connection = getConnection(channel);
        if(connection.getUser() != null) {
            connection.sendPacket(new PacketOut4LoginFailed(
                    PacketOut4LoginFailed.Reason.USER_IS_AUTHORIZED
            ));
            return;
        }
        UserManager.loginUser(connection , packet.getUserName() , packet.getPassword());
    }

    @Override
    public void handle(PacketIn7UserMessage packet, SocketChannel channel) {
        UserConnection connection = getConnection(channel);
        if(connection.getUser() == null) return;
        if(packet.getMessage().startsWith("/")) {
            CommandManager.sendCommand((Sender) connection.getUser(), packet.getMessage());
            return;
        }
        LogMessage.addMessage(new UserMessage((Sender) connection.getUser(), packet.getMessage()));
    }

    private UserConnection getConnection(SocketChannel channel) {
        UserConnection connection = ConnectionManager.getInstance()
                .getConnection(channel);
        Check.notNull(connection , "connection is null");
        return connection;
    }
}
