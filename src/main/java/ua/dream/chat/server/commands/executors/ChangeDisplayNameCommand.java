package ua.dream.chat.server.commands.executors;

import ua.dream.chat.server.App;
import ua.dream.chat.server.commands.Command;
import ua.dream.chat.server.connections.ConnectionManager;
import ua.dream.chat.server.connections.UserConnection;
import ua.dream.chat.server.user.Administrator;
import ua.dream.chat.server.user.Sender;
import ua.dream.chat.server.user.User;
import ua.dream.chat.utils.validate.CheckUserData;

import java.sql.SQLException;
import java.util.List;

public class ChangeDisplayNameCommand extends Command {

    public ChangeDisplayNameCommand() {
        super("change_display_name", new String[]{"cdn"}, "Command to chage display name", "/change_display_name {user} {display name}", Administrator.class);
    }

    @Override
    public boolean onExecute(Sender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            return false;
        }
        List<UserConnection> accepterList = ConnectionManager.getInstance().getUserConnections(args[0]);
        try {
            CheckUserData.checkDisplayName(args[1]);
        }catch (Exception e) {
            sender.sendMessage("Invalid display name");
            return true;
        }
        if(accepterList.size() == 0) {
            sender.sendMessage("User " + args[0] + " is offline");
            return true;
        }
        if(!(accepterList.get(0).getUser() instanceof User)) {
            sender.sendMessage("You cant change display name for administrator");
            return true;
        }
        try {
            App.getConnector().getUserTable().updateDisplayName(args[0].toUpperCase() , args[1]);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return true;
        }
        accepterList.forEach(connection -> {
            if(connection.getUser() != null) {
                ((User) connection.getUser()).setDisplayName(args[0]);
                ((User) connection.getUser()).sendMessage("Your display name change on " + args[0]);
            }
        });
        return true;
    }
}
