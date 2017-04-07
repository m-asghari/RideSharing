package edu.usc.infolab.geo.util;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private final String url;
    private final String username;
    private final String password;

    private final BasicDataSource dataSource;

    public ConnectionManager(String driver, String url, String username, String password) {

        this.url = url;
        this.username = username;
        this.password = password;

        // Load driver for normal connections
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Configure datasource
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        this.dataSource = dataSource;
    }

    public static ConnectionManager getDefaultManager() {
        String driver = "org.postgresql.Driver";
        String url = String.format("jdbc:postgresql://%s:%s/%s",
                Utility.getProperty("db_host"),
                Utility.getProperty("db_port"),
                "LA_RN");
        String username = Utility.getProperty("db_user");
        String password = Utility.getProperty("db_password");
        return new ConnectionManager(driver, url, username, password);
    }

    public static Connection getDefaultConnection() throws SQLException {
        return getDefaultConnection("LA_RN");
    }

    public static Connection getDefaultConnection(String dbName) throws SQLException {
        String driver = "org.postgresql.Driver";
        // Loads driver for normal connections
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String url = String.format("jdbc:postgresql://%s:%s/%s",
                Utility.getProperty("db_host"),
                Utility.getProperty("db_port"),
                dbName);
        String username = Utility.getProperty("db_user");
        String password = Utility.getProperty("db_password");
        return DriverManager.getConnection(url, username, password);
    }

    public synchronized Connection getNormalConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        return connection;
    }

    private synchronized Connection getPooledConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private synchronized void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }
}
