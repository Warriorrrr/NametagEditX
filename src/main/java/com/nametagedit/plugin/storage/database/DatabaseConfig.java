package com.nametagedit.plugin.storage.database;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.storage.database.tasks.*;
import com.nametagedit.plugin.utils.Configuration;
import com.nametagedit.plugin.utils.UUIDFetcher;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DatabaseConfig implements AbstractConfig {

    private NametagEdit plugin;
    private NametagHandler handler;
    private HikariDataSource hikari;

    // These are used if the user wants to customize the
    // schema structure. Perhaps more cosmetic.
    public static String TABLE_GROUPS;
    public static String TABLE_PLAYERS;
    public static String TABLE_CONFIG;

    public DatabaseConfig(NametagEdit plugin, NametagHandler handler, Configuration config) {
        this.plugin = plugin;
        this.handler = handler;
        TABLE_GROUPS = "`" + config.getString("MySQL.GroupsTable", "nte_groups") + "`";
        TABLE_PLAYERS = "`" + config.getString("MySQL.PlayersTable", "nte_players") + "`";
        TABLE_CONFIG = "`" + config.getString("MySQL.ConfigTable", "nte_config") + "`";
    }

    @Override
    public void load() {
        FileConfiguration config = handler.getConfig();
        shutdown();
        hikari = new HikariDataSource();
        hikari.setMaximumPoolSize(config.getInt("MinimumPoolSize", 10));
        hikari.setPoolName("NametagEdit Pool");

        String port = "3306";

        if (config.isSet("MySQL.Port")) {
            port = config.getString("MySQL.Port");
        }

        hikari.setJdbcUrl("jdbc:mysql://" + config.getString("MySQL.Hostname") + ":" + port + "/" + config.getString("MySQL.Database"));
        hikari.addDataSourceProperty("useSSL", false);
        hikari.addDataSourceProperty("requireSSL", false);
        hikari.addDataSourceProperty("verifyServerCertificate", false);
        hikari.addDataSourceProperty("user", config.getString("MySQL.Username"));
        hikari.addDataSourceProperty("password", config.getString("MySQL.Password"));

        hikari.setUsername(config.getString("MySQL.Username"));
        hikari.setPassword(config.getString("MySQL.Password"));

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new DatabaseUpdater(handler, hikari, plugin).run());
    }

    @Override
    public void reload() {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new DataDownloader(handler, hikari).run());
    }

    @Override
    public void shutdown() {
        if (hikari != null) {
            hikari.close();
        }
    }

    @Override
    public void load(Player player, boolean loggedIn) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new PlayerLoader(player.getUniqueId(), plugin, handler, hikari, loggedIn).run());
    }

    @Override
    public void save(PlayerData... playerData) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new PlayerSaver(playerData, hikari).run());
    }

    @Override
    public void save(GroupData... groupData) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new GroupSaver(groupData, hikari).run());
    }

    @Override
    public void savePriority(boolean playerTag, String key, final int priority) {
        if (playerTag) {
            UUIDFetcher.lookupUUID(key, plugin, uuid -> {
                if (uuid != null) {
                    plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new PlayerPriority(uuid, priority, hikari).run());
                } else {
                    plugin.getLogger().severe("An error has occurred while looking for UUID.");
                }
            });
        } else {
            plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new GroupPriority(key, priority, hikari).run());
        }
    }

    @Override
    public void delete(GroupData groupData) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new GroupDeleter(groupData.getGroupName(), hikari).run());
    }

    @Override
    public void add(GroupData groupData) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new GroupAdd(groupData, hikari).run());
    }

    @Override
    public void clear(UUID uuid, String targetName) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new PlayerDeleter(uuid, hikari).run());
    }

    @Override
    public void orderGroups(CommandSender commandSender, List<String> order) {
        String formatted = Arrays.toString(order.toArray());
        formatted = formatted.substring(1, formatted.length() - 1).replace(",", "");

        String format = formatted;
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> new GroupConfigUpdater("order", format, hikari).run());
    }

}
