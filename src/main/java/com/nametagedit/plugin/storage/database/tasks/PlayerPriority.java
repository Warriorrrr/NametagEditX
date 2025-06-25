package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerPriority implements Runnable {

    private final UUID player;
    private final int priority;
    private final HikariDataSource hikari;

    public PlayerPriority(UUID player, int priority, HikariDataSource hikari) {
        this.player = player;
        this.priority = priority;
        this.hikari = hikari;
    }

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + DatabaseConfig.TABLE_PLAYERS + " SET `priority`=? WHERE `uuid`=?");
            preparedStatement.setInt(1, priority);
            preparedStatement.setString(2, player.toString());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
