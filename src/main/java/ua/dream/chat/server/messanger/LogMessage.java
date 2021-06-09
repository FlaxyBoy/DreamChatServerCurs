package ua.dream.chat.server.messanger;

import com.google.common.collect.Queues;
import ua.dream.chat.server.connections.ConnectionManager;
import ua.dream.chat.server.connections.UserConnection;

import java.util.Queue;

public class LogMessage {

    private static Queue<Message> messages = Queues.newConcurrentLinkedQueue();

    public static void addMessage(Message message) {
        messages.add(message);
        while (messages.size() > 100) messages.poll();
        ConnectionManager.getInstance().broadcastClient(message.encode());
    }

    public static void loadMessage(UserConnection connection) {
        messages.forEach(message -> {
            connection.sendPacket(message.encode());
        });
    }
}
