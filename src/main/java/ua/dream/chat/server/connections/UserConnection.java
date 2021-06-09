package ua.dream.chat.server.connections;

import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.Nullable;
import ua.dream.chat.network.netty.frame.Packet;
import ua.dream.chat.network.netty.frame.PacketOutHandler;
import ua.dream.chat.network.netty.packet.out.PacketOut6UserLogout;
import ua.dream.chat.server.messanger.LogMessage;
import ua.dream.chat.server.messanger.Message;

import java.util.Objects;


public class UserConnection {

    private Connectable user;
    private final SocketChannel channel;

    protected UserConnection(SocketChannel channel) {
        this.channel = channel;
        this.user = null;
    }

    public void sendPacket(Packet<PacketOutHandler> packet) {
        channel.writeAndFlush(packet);
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setUser(@Nullable Connectable user , String... reason) {
        this.user = user;
        if(!Objects.isNull(user)) {
            sendPacket(user.getData());
            LogMessage.loadMessage(this);
        }else {
            if(reason.length > 0)
                sendPacket(new PacketOut6UserLogout());
            else
                sendPacket(new PacketOut6UserLogout(reason[0]));
        }
    }

    public void sendPacketMessage(Message message) {
        this.sendPacket(message.encode());
    }

    @Nullable
    public Connectable getUser() {
        return user;
    }
}
