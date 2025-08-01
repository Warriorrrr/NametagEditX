package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GroupConfigUpdater implements Runnable {

    private final String setting;
    private final String value;
    private final HikariDataSource hikari;

    public GroupConfigUpdater(String setting, String value, HikariDataSource hikari) {
        this.setting = setting;
        this.value = value;
        this.hikari = hikari;
    }

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            final String QUERY = "INSERT INTO " + DatabaseConfig.TABLE_GROUPS + " VALUES(?, ?) ON DUPLICATE KEY UPDATE `value`=?";
            PreparedStatement update = connection.prepareStatement(QUERY);
            update.setString(1, setting);
            update.setString(2, value);
            update.setString(3, value);
            update.execute();
            update.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
