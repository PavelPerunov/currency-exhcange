package com.perunovpavel.util;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private static final String URL_KEY = "db.url";

    @SneakyThrows
    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(PropertiesUtil.getProperties(URL_KEY));
        } catch (SQLException exception) {
            throw new SQLException(exception);
        }
    }
}
