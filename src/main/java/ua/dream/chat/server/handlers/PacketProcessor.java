package ua.dream.chat.server.handlers;

import com.google.common.collect.Queues;
import ua.dream.chat.network.netty.frame.Packet;
import ua.dream.chat.network.netty.frame.PacketInHandler;
import ua.dream.chat.server.App;
import ua.dream.chat.utils.async.AsyncUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

import java.util.Queue;

public final class PacketProcessor {

    private final Queue<PacketContainer> packets = Queues.newConcurrentLinkedQueue();
    private final PacketInHandler handler;

    public PacketProcessor(PacketInHandler handler) {
        this.handler = handler;
        AsyncUtils.runAsync(() -> {
            while (true) {
                update();
                try {
                    synchronized (PacketProcessor.this) {
                        PacketProcessor.this.wait();
                    }
                } catch (InterruptedException e) {
                    if(!App.enable.get()) {
                        update();
                        return;
                    }
                }
            }
        });
    }

    public void process(Packet<PacketInHandler> packet , ChannelHandlerContext context) {
        packets.add(new PacketContainer(packet , (SocketChannel) context.channel()));
        synchronized (this) {
            this.notify();
        }
    }

    public void update() {
        while (!packets.isEmpty()) {
            PacketContainer packet = packets.poll();
            if(packet.getContext() != null && packet.getContext().isActive()) {
                try {
                    packet.getPacket().handle(handler, packet.getContext());
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
