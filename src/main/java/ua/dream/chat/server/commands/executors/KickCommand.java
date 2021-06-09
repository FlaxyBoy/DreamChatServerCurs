package ua.dream.chat.server.commands.executors;

import ua.dream.chat.server.commands.Command;
import ua.dream.chat.server.connections.ConnectionManager;
import ua.dream.chat.server.connections.UserConnection;
import ua.dream.chat.server.messanger.ServerMessage;
import ua.dream.chat.server.user.Administrator;
import ua.dream.chat.server.user.Sender;

import java.util.List;

public class KickCommand extends Command {

    public KickCommand() {
        super("kick", new String[] {}, "command for kick user", "/kick", Administrator.class);
    }

    @Override
    public boolean onExecute(Sender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            return false;
        }
        List<UserConnection> accepterList = ConnectionManager.getInstance().getUserConnections(args[0]);
        if(accepterList.size() == 0) {
            sender.sendMessage("User " + args[0] + " is offline");
            return true;
        }
        accepterList.forEach(connection -> {
            connection.setUser(null);
        });
        ConnectionManager.getInstance().broadcastClient(new ServerMessage("User " + args[0] + " is disconected").encode());
        return true;
    }
}
