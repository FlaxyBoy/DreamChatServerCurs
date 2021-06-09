package ua.dream.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.dream.chat.network.netty.coodec.PacketDecoder;
import ua.dream.chat.network.netty.coodec.PacketEncoder;
import ua.dream.chat.server.commands.CommandManager;
import ua.dream.chat.server.commands.executors.ChangeDisplayNameCommand;
import ua.dream.chat.server.commands.executors.HelpCommand;
import ua.dream.chat.server.commands.executors.KickCommand;
import ua.dream.chat.server.commands.executors.PrivateMessageCommand;
import ua.dream.chat.server.handlers.ClientHandler;
import ua.dream.chat.server.handlers.PacketInHandlerImpl;
import ua.dream.chat.server.handlers.PacketProcessor;
import ua.dream.chat.server.sql.SqlConnector;

import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static AtomicBoolean enable = new AtomicBoolean(true);

    private static NettyBootstrap server;
    private static SqlConnector connector;
    private static Thread thread;

    private static ChannelFuture future;

    public static void main(String[] args) throws SQLException {
        connector = new SqlConnector("localhost" , "3306" , "DreamChat" , "" , "");
        PacketProcessor processor = new PacketProcessor(new PacketInHandlerImpl());
        server = new NettyBootstrap(processor);
        thread = new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                logger.info("Start server");
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {

                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast(new PacketDecoder() , new PacketEncoder() , new ClientHandler(processor));
                            }

                        });
                future = bootstrap.bind(8888).sync();
                logger.info("Server started");
                future.channel().closeFuture().sync();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully(); enable.set(false);
                connector.close();
                System.out.println("Close");
            }
        });
        thread.setName("Server-Thread-1");
        thread.start();
        registerCommands();
        startConsole();
    }

    public static void startConsole() {
        Scanner scanner = new Scanner(System.in);
        while (enable.get()) {
            String s = scanner.nextLine();
            if(s.equalsIgnoreCase("/stop")) {
                thread.stop();
                return;
            }
        }
    }

    private static void registerCommands() {
        CommandManager.register(new HelpCommand());
        CommandManager.register(new PrivateMessageCommand());
        CommandManager.register(new KickCommand());
        CommandManager.register(new ChangeDisplayNameCommand());
    }

    public static synchronized void stop() {

    }

    public static SqlConnector getConnector() {
        return connector;
    }

    public static NettyBootstrap getServer() {
        return server;
    }
}
