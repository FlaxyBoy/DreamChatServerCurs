package ua.dream.chat.server.commands.executors;

import ua.dream.chat.network.netty.packet.out.PacketOut8Message;
import ua.dream.chat.server.commands.Command;
import ua.dream.chat.server.connections.ConnectionManager;
import ua.dream.chat.server.connections.UserConnection;
import ua.dream.chat.server.user.Administrator;
import ua.dream.chat.server.user.Sender;
import ua.dream.chat.server.user.User;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

public class PrivateMessageCommand extends Command {

    public PrivateMessageCommand() {
        super("msg", new String[]{"m" , "tell" , "w" , "whisper"}, "send private message for user", "/msg {user} {message}", User.class , Administrator.class);
    }

    @Override
    public boolean onExecute(Sender sender, Command command, String label, String[] args) {
        if(args.length < 2) return false;
        if(args[0].equalsIgnoreCase(sender.getLogin())) {
            sender.sendMessage("you cannot write private messages to yourself");
            return true;
        }
        List<UserConnection> connections = ConnectionManager.getInstance().getUserConnections(args[0]);
        if(connections.size() == 0) {
            sender.sendMessage(args[0] + " is offline");
            return true;
        }
        StringJoiner joiner = new StringJoiner(" ");
        for(int i = 1 ; i < args.length ; i++) joiner.add(args[i]);
        AtomicReference<Sender> accepter = new AtomicReference<>();
        connections.forEach(connection -> {
            connection.sendPacket(new PacketOut8Message(joiner.toString() , sender.getLogin() , sender.getDisplayName() , PacketOut8Message.Type.PRIVATE_MESSAGE));
            accepter.set((Sender) connection.getUser());
        });
        ConnectionManager.getInstance().getUserConnections(sender.getLogin()).forEach(connection -> {
            connection.sendPacket(new PacketOut8Message(joiner.toString() , accepter.get().getLogin() , accepter.get().getDisplayName() , PacketOut8Message.Type.SEND_PRIVATE_MESSAGE));
        });
        return true;
    }
}
