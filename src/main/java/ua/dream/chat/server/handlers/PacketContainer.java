package ua.dream.chat.server.handlers;

import ua.dream.chat.network.netty.frame.Packet;
import ua.dream.chat.network.netty.frame.PacketInHandler;
import io.netty.channel.socket.SocketChannel;

public final class PacketContainer {

    private final Packet<PacketInHandler> packet;
    private final SocketChannel context;

    protected PacketContainer(Packet<PacketInHandler> packet , SocketChannel context) {
        this.packet = packet;
        this.context = context;
    }

    protected SocketChannel getContext() {
        return context;
    }

    protected Packet<PacketInHandler> getPacket() {
        return packet;
    }
}
