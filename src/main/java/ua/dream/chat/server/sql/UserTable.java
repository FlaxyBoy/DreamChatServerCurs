package ua.dream.chat.server.sql;

import ua.dream.chat.server.utils.EncoderUtils;
import ua.dream.chat.utils.validate.CheckUserData;

import java.net.InetSocketAddress;
import java.sql.*;

public class UserTable {

    private final Connection connection;
    private PreparedStatement selectByLogin;
    private PreparedStatement insertUser;
    private PreparedStatement updateDisplayName;

    public UserTable(SqlConnector connector) throws SQLException {
        connection = connector.getHikari().getConnection();
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS `USERS` (" +
                "login varchar(" + CheckUserData.MAX_USER_NAME_LENGTH + ") , " +
                "real_login varchar(" + CheckUserData.MAX_USER_NAME_LENGTH + ") , " +
                "password BLOB , " +
                "display_name varchar(" + CheckUserData.MAX_DISPLAY_NAME_LENGTH + ") DEFAULT '', " +
                "register_date LONG NOT NULL , " +
                "PRIMARY KEY (login)" +
                ");");
        statement.close();
        prepare();
    }

    private void prepare() throws SQLException {
        selectByLogin = connection.prepareStatement("SELECT * FROM `USERS` WHERE `real_login` = (?)");
        insertUser = connection.prepareStatement("INSERT INTO `USERS` (`login` , `real_login` , `password` ," +
                "`register_date`) VALUES ((?) , (?) , (?) , (?))");
        updateDisplayName = connection.prepareStatement("UPDATE `USERS` SET `display_name` = (?) WHERE `login` = (?)");
    }

    public synchronized void updateDisplayName(String login , String displayName) throws SQLException {
        updateDisplayName.setString( 1 , displayName);
        updateDisplayName.setString( 2 , login);
        updateDisplayName.execute();
    }

    public synchronized UserData getUserData(String login) throws SQLException {
        selectByLogin.setString(1 , login);
        ResultSet set = selectByLogin.executeQuery();
        if(!set.next()) {
            set.close();
            return null;
        }
        UserData data = new UserData(set.getString("login") , set.getBytes("password"));
        set.close();
        return data;
    }

    public synchronized void insertUser(String login , String password , InetSocketAddress address) throws SQLException {
        insertUser.setString(1 , login.toLowerCase());
        insertUser.setString(2 , login);
        insertUser.setBytes(3 , EncoderUtils.toSha256(password));
        insertUser.setLong(4 , System.currentTimeMillis());
        insertUser.executeUpdate();
    }

    public synchronized boolean checkAccount(String login) throws SQLException {
        selectByLogin.setString(1 , login.toLowerCase());
        ResultSet set = selectByLogin.executeQuery();
        if(set.next()) {
            set.close();
            return false;
        }
        set.close();
        return true;
    }


    protected synchronized void close() {
        closeConnection(selectByLogin , insertUser);
        if(connection != null ) {
            try {
                connection.close();
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection(PreparedStatement... statements) {
        for(Statement statement : statements) {
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    public static class UserData {

        private final String login;
        private final byte[] password;

        public UserData(String login , byte[] password) {
            this.login = login;
            this.password = password;
        }

        public String getLogin() {
            return login;
        }

        public byte[] getPassword() {
            return password;
        }
    }

}
