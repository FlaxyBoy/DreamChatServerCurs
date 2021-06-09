package ua.dream.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.dream.chat.network.netty.coodec.PacketDecoder;
import ua.dream.chat.network.netty.coodec.PacketEncoder;
import ua.dream.chat.server.handlers.ClientHandler;
import ua.dream.chat.server.handlers.PacketProcessor;

public class NettyBootstrap {


    public static final String DECODER_HANDLER_NAME = "decoder"; // Read
    public static final String ENCODER_HANDLER_NAME = "encoder"; // Write
    public static final String CLIENT_CHANNEL_NAME = "handler"; // Read


    private static final Logger logger = LoggerFactory.getLogger(NettyBootstrap.class);
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ServerBootstrap bootstrap;
    private ChannelFuture future;


    public NettyBootstrap(PacketProcessor processor) {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(@NotNull SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(DECODER_HANDLER_NAME, new PacketDecoder());

                pipeline.addLast(ENCODER_HANDLER_NAME, new PacketEncoder());

                pipeline.addLast(CLIENT_CHANNEL_NAME, new ClientHandler(processor));
            }
        });
    }

    public void start() {
        try {
            future = bootstrap.bind("localhost" , 8888).sync();
            logger.info("Server started");;
            future.channel().closeFuture().sync();
            System.out.println("End");
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            App.stop();
        }
    }

}
