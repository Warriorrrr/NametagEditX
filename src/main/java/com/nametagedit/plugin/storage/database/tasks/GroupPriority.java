package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GroupPriority implements Runnable {

    private final String group;
    private final int priority;
    private final HikariDataSource hikari;

    public GroupPriority(String group, int priority, HikariDataSource hikari) {
        this.group = group;
        this.priority = priority;
        this.hikari = hikari;
    }

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + DatabaseConfig.TABLE_GROUPS + " SET `priority`=? WHERE `name`=?");
            preparedStatement.setInt(1, priority);
            preparedStatement.setString(2, group);
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
