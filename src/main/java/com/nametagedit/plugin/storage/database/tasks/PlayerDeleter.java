package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDeleter implements Runnable {

    private final UUID uuid;
    private final HikariDataSource hikari;

    public PlayerDeleter(UUID uuid, HikariDataSource hikari) {
        this.uuid = uuid;
        this.hikari = hikari;
    }

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            final String QUERY = "DELETE FROM " + DatabaseConfig.TABLE_PLAYERS + " WHERE `uuid`=?";
            PreparedStatement delete = connection.prepareStatement(QUERY);
            delete.setString(1, uuid.toString());
            delete.execute();
            delete.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
