package ua.dream.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.dream.chat.network.netty.coodec.PacketDecoder;
import ua.dream.chat.network.netty.coodec.PacketEncoder;
import ua.dream.chat.server.handlers.ClientHandler;
import ua.dream.chat.server.handlers.PacketProcessor;
import ua.dream.chat.utils.validate.Check;

public final class NettyServer {

    private static Logger logger = LoggerFactory.getLogger(NettyServer.class);

    public static final int MAX_THREAD_COUNT = 8;

    public static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);
    private static final WriteBufferWaterMark SERVER_WRITE_MARK = new WriteBufferWaterMark(1 << 20,
            1 << 21);

    public static final String DECODER_HANDLER_NAME = "decoder"; // Read
    public static final String ENCODER_HANDLER_NAME = "encoder"; // Write
    public static final String CLIENT_CHANNEL_NAME = "handler"; // Read

    private boolean initialized = false;

    private final PacketProcessor packetProcessor;

    private boolean tcpNoDelay = false;

    private EventLoopGroup boss, worker;
    private ServerBootstrap bootstrap;

    private ServerSocketChannel serverChannel;

    private String address;
    private int port;

    public NettyServer(@NotNull PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
    }

    public void init() {
        Check.stateCondition(initialized, "Netty server has already been initialized!");
        this.initialized = true;

        Class<? extends ServerChannel> channel;
        final int workerThreadCount = MAX_THREAD_COUNT;

        // Find boss/worker event group
        {
            if (Epoll.isAvailable()) {
                boss = new EpollEventLoopGroup(2);
                worker = new EpollEventLoopGroup(workerThreadCount);

                channel = EpollServerSocketChannel.class;

                LOGGER.info("Using epoll");
            } else if (KQueue.isAvailable()) {
                boss = new KQueueEventLoopGroup(2);
                worker = new KQueueEventLoopGroup(workerThreadCount);

                channel = KQueueServerSocketChannel.class;

                LOGGER.info("Using kqueue");
            } else {
                boss = new NioEventLoopGroup(2);
                worker = new NioEventLoopGroup(workerThreadCount);

                channel = NioServerSocketChannel.class;

                LOGGER.info("Using NIO");
            }
        }

        // Add default allocator settings
        {
            if (System.getProperty("io.netty.allocator.numDirectArenas") == null) {
                System.setProperty("io.netty.allocator.numDirectArenas", String.valueOf(workerThreadCount));
            }

            if (System.getProperty("io.netty.allocator.numHeapArenas") == null) {
                System.setProperty("io.netty.allocator.numHeapArenas", String.valueOf(workerThreadCount));
            }

            if (System.getProperty("io.netty.allocator.maxOrder") == null) {
                // The default page size is 8192 bytes, a bit shift of 5 makes it 262KB
                // largely enough for this type of server
                System.setProperty("io.netty.allocator.maxOrder", "5");
            }
        }

        bootstrap = new ServerBootstrap()
                .group(boss, worker)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, SERVER_WRITE_MARK)
                .channel(channel);


        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(@NotNull SocketChannel ch) {
                ChannelConfig config = ch.config();
                config.setOption(ChannelOption.TCP_NODELAY, tcpNoDelay);
                config.setOption(ChannelOption.SO_SNDBUF, 262_144);
                config.setAllocator(ByteBufAllocator.DEFAULT);

                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(DECODER_HANDLER_NAME, new PacketDecoder());

                pipeline.addLast(ENCODER_HANDLER_NAME, new PacketEncoder());

                pipeline.addLast(CLIENT_CHANNEL_NAME, new ClientHandler(packetProcessor));
            }
        });
    }

    public void start(int port) {
        this.address = address;
        this.port = port;

        // Bind address
        try {
            ChannelFuture cf = bootstrap.bind(port).sync();
            if (!cf.isSuccess()) {
                throw new IllegalStateException("Unable to bind server at " + address + ":" + port);
            }

            this.serverChannel = (io.netty.channel.socket.ServerSocketChannel) cf.channel();
            serverChannel.closeFuture().sync();
        } catch (InterruptedException ex) {
            LOGGER.trace(ex.getMessage() , ex);
        } finally {
            System.out.println("Disable server");
            App.stop();
        }
    }

    @Nullable
    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public void stop() {
        try {
            this.serverChannel.close().sync();
            this.worker.shutdownGracefully();
            this.boss.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}