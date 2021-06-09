package ua.dream.chat.server.user;

import ua.dream.chat.network.netty.packet.out.PacketOut3RegistrationFailed;
import ua.dream.chat.network.netty.packet.out.PacketOut4LoginFailed;
import ua.dream.chat.server.App;
import ua.dream.chat.server.connections.UserConnection;
import ua.dream.chat.server.sql.LogTable;
import ua.dream.chat.server.sql.UserTable;
import ua.dream.chat.server.utils.EncoderUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserManager {

    private static Map<String , String> adminMap = new HashMap<>();

    static {
        adminMap.put("admin" , "admin");
    }

    public static void loginUser(UserConnection connection , String login , String password) {
        UserTable table = App.getConnector().getUserTable();
        LogTable logTable = App.getConnector().getLogUserTable();
        try {
            if(adminMap.containsKey(login)) {
                if(adminMap.get(login).equals(password)) {
                    connection.setUser(new Administrator(connection , login));
                    logTable.insert(connection.getChannel().remoteAddress() , login);
                    return;
                }
                connection.sendPacket(new PacketOut4LoginFailed(PacketOut4LoginFailed.Reason.SERVER_EXCEPTION));
                return;
            }
            UserTable.UserData data = table.getUserData(login);
            if(data == null) {
                connection.sendPacket(new PacketOut4LoginFailed(PacketOut4LoginFailed.Reason.USER_NOT_REGISTERED));
                return;
            }
            if(!Arrays.equals(EncoderUtils.toSha256(password) , data.getPassword())) {
                connection.sendPacket(new PacketOut4LoginFailed(PacketOut4LoginFailed.Reason.WRONG_PASSWORD));
                return;
            }
            connection.setUser(new User(connection , login , password));
            logTable.insert(connection.getChannel().remoteAddress() , ((User) connection.getUser()).getLogin());
        }catch (Exception e) {
            connection.sendPacket(new PacketOut4LoginFailed(PacketOut4LoginFailed.Reason.SERVER_EXCEPTION));
            e.printStackTrace();
        }
    }

    public static void registerUser(UserConnection connection , String login , String password) {
        System.out.println("Register user packet");
        UserTable table = App.getConnector().getUserTable();
        LogTable logTable = App.getConnector().getLogUserTable();
        try {
            if(!table.checkAccount(login)) {
                connection.sendPacket(new PacketOut3RegistrationFailed(PacketOut3RegistrationFailed.Reason.LOGIN_IS_OCCUPIED));
                return;
            }
            table.insertUser(login , password , connection.getChannel().remoteAddress());
            connection.setUser(new User(connection , login , password));
            logTable.insert(connection.getChannel().remoteAddress() , ((User) connection.getUser()).getLogin());
        }catch (Exception e) {
            connection.sendPacket(new PacketOut3RegistrationFailed(PacketOut3RegistrationFailed.Reason.SERVER_EXCEPTION));
            e.printStackTrace();
        }



    }


}
