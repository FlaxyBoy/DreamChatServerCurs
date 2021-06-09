package ua.dream.chat.server.handlers;

import ua.dream.chat.network.netty.frame.Packet;
import ua.dream.chat.network.netty.frame.PacketInHandler;
import ua.dream.chat.server.connections.ConnectionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class ClientHandler extends ChannelInboundHandlerAdapter {

    private final PacketProcessor processor;

    public ClientHandler(PacketProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ConnectionManager.getInstance()
                .connect(ctx);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof Packet<?>) {
            Packet<PacketInHandler> packet = (Packet<PacketInHandler>) msg;
            processor.process(packet, ctx);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ConnectionManager.getInstance()
                .disconnect(ctx);
    }
}
