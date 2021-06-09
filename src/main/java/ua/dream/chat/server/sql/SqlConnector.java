package ua.dream.chat.server.sql;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;

public class SqlConnector {

    private final HikariDataSource hikari;
    private final UserTable userTable;
    private final LogTable logUserTable;

    public SqlConnector(String serverName , String port , String database , String user , String password) throws SQLException {
        hikari = new HikariDataSource();
        this.hikari.setMaximumPoolSize(16);
        this.hikari.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        this.hikari.setPoolName("CoreHikariPool");
        this.hikari.addDataSourceProperty("serverName", serverName);
        this.hikari.addDataSourceProperty("port", port);
        this.hikari.addDataSourceProperty("databaseName", database);
        this.hikari.addDataSourceProperty("user", user);
        this.hikari.addDataSourceProperty("password", password);
        userTable = new UserTable(this);
        logUserTable = new LogTable(this);
    }

    public HikariDataSource getHikari() {
        return hikari;
    }

    public UserTable getUserTable() {
        return userTable;
    }

    public LogTable getLogUserTable() {
        return logUserTable;
    }

    public synchronized void close() {
        hikari.close();
        userTable.close();
        logUserTable.close();
    }
}
