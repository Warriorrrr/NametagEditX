package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GroupDeleter implements Runnable {

    private final String groupName;
    private final HikariDataSource hikari;

    public GroupDeleter(String groupName, HikariDataSource hikari) {
        this.groupName = groupName;
        this.hikari = hikari;
    }

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            final String QUERY = "DELETE FROM " + DatabaseConfig.TABLE_GROUPS + " WHERE `name`=?";
            PreparedStatement delete = connection.prepareStatement(QUERY);
            delete.setString(1, groupName);
            delete.execute();
            delete.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
