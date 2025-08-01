package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerLoader implements Runnable {

    private UUID uuid;
    private Plugin plugin;
    private NametagHandler handler;
    private HikariDataSource hikari;
    private boolean loggedIn;

    public PlayerLoader(UUID uuid, Plugin plugin, NametagHandler handler, HikariDataSource hikari, boolean loggedIn) {
        this.uuid = uuid;
        this.plugin = plugin;
        this.handler = handler;
        this.hikari = hikari;
        this.loggedIn = loggedIn;
    }

    @Override
    public void run() {
        String tempPrefix = null;
        String tempSuffix = null;
        int priority = -1;
        boolean found = false;

        try (Connection connection = hikari.getConnection()) {
            final String QUERY = "SELECT `prefix`, `suffix`, `priority` FROM " + DatabaseConfig.TABLE_PLAYERS + " WHERE `uuid`=?";

            try (PreparedStatement select = connection.prepareStatement(QUERY)) {
                select.setString(1, uuid.toString());

                ResultSet resultSet = select.executeQuery();
                if (resultSet.next()) {
                    tempPrefix = resultSet.getString("prefix");
                    tempSuffix = resultSet.getString("suffix");
                    priority = resultSet.getInt("priority");
                    found = true;
                }

                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            final String prefix = tempPrefix == null ? "" : tempPrefix;
            final String suffix = tempSuffix == null ? "" : tempSuffix;
            final boolean finalFound = found;

            final int finalPriority = priority;

            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getScheduler().run(plugin, t -> {
                    if (finalFound) {
                        PlayerData data = handler.getPlayerData(player);
                        if (data == null) {
                            data = new PlayerData(player.getName(), player.getUniqueId(), prefix, suffix, finalPriority);
                            handler.storePlayerData(player.getUniqueId(), data);
                        } else {
                            data.setPrefix(prefix);
                            data.setSuffix(suffix);
                        }
                    }

                    handler.applyTagToPlayer(player, loggedIn);
                }, () -> {
                });
            }
        }
    }

}
