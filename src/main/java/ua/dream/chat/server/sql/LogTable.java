package ua.dream.chat.server.sql;

import ua.dream.chat.utils.validate.CheckUserData;

import java.net.InetSocketAddress;
import java.sql.*;

public class LogTable {

    private final Connection connection;
    private PreparedStatement insert;

    public LogTable(SqlConnector connector) throws SQLException {
        this.connection = connector.getHikari().getConnection();
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS `USER_LOG` (" +
                "login varchar(" + CheckUserData.MAX_USER_NAME_LENGTH + ") , " +
                "ip_addres varchar(64) , " +
                "join_date LONG NOT NULL)");
        statement.close();
        prepare();
    }

    private void prepare() throws SQLException {
        insert = connection.prepareStatement("INSERT INTO `USER_LOG`(`login`, `ip_addres`, `join_date`) VALUES ((?),(?),(?))");
    }

    public synchronized void insert(InetSocketAddress address , String login) throws SQLException {
        insert.setString(1 , login);
        insert.setString(2 , address.getHostName());
        insert.setLong(3 , System.currentTimeMillis());
        insert.execute();
    }

    protected synchronized void close() {
        closeConnection(insert);
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
}
