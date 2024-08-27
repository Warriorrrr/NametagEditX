package com.nametagedit.plugin;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.INametag;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.api.events.NametagEvent;
import com.nametagedit.plugin.api.events.NametagFirstLoadedEvent;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.storage.flatfile.FlatFileConfig;
import com.nametagedit.plugin.utils.Colors;
import com.nametagedit.plugin.utils.Configuration;
import com.nametagedit.plugin.utils.UUIDFetcher;
import com.nametagedit.plugin.utils.Utils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NametagHandler implements Listener {

    public static boolean DISABLE_PUSH_ALL_TAGS = false;
    private boolean debug;

    private boolean tabListEnabled;
    private boolean longNametagsEnabled;
    private boolean refreshTagOnWorldChange;

    private ScheduledTask clearEmptyTeamTask;
    private ScheduledTask refreshNametagTask;
    private final AbstractConfig abstractConfig;

    private final Configuration config;

    private final Map<String, GroupData> groupData = new HashMap<>();
    private final Map<UUID, PlayerData> playerData = new HashMap<>();

    private final NametagEdit plugin;
    private final NametagManager nametagManager;

    public NametagHandler(NametagEdit plugin, NametagManager nametagManager) {
        this.config = getCustomConfig(plugin);
        this.plugin = plugin;
        this.nametagManager = nametagManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Apply config properties
        this.applyConfig();

        if (config.getBoolean("MySQL.Enabled")) {
            abstractConfig = new DatabaseConfig(plugin, this, config);
        } else {
            abstractConfig = new FlatFileConfig(plugin, this);
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> abstractConfig.load());
    }

    /**
     * This function loads our custom config with comments, and includes changes
     */
    private Configuration getCustomConfig(Plugin plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.saveDefaultConfig();

            Configuration newConfig = new Configuration(file);
            newConfig.reload(true);
            return newConfig;
        } else {
            Configuration oldConfig = new Configuration(file);
            oldConfig.reload(false);

            file.delete();
            plugin.saveDefaultConfig();

            Configuration newConfig = new Configuration(file);
            newConfig.reload(true);

            for (String key : oldConfig.getKeys(false)) {
                if (newConfig.contains(key)) {
                    newConfig.set(key, oldConfig.get(key));
                }
            }

            newConfig.save();
            return newConfig;
        }
    }

    /**
     * Cleans up any nametag data on the server to prevent memory leaks
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        nametagManager.reset(event.getPlayer().getName());
    }

    /**
     * Applies tags to a player
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        nametagManager.sendTeams(player);

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> abstractConfig.load(player, true));
    }

    /**
     * Some users may have different permissions per world.
     * If this is enabled, their tag will be reloaded on TP.
     */
    @EventHandler
    public void onTeleport(final PlayerChangedWorldEvent event) {
        if (!refreshTagOnWorldChange) return;

        event.getPlayer().getScheduler().runDelayed(plugin, t -> applyTagToPlayer(event.getPlayer(), false), () -> {}, 3L);
    }

    private void handleClear(UUID uuid, String player) {
        removePlayerData(uuid);
        nametagManager.reset(player);
        abstractConfig.clear(uuid, player);
    }

    public void clearMemoryData() {
        synchronized (this.groupData) {
            this.groupData.clear();
        }

        synchronized (this.playerData) {
            this.playerData.clear();
        }
    }

    public void removePlayerData(UUID uuid) {
        synchronized (this.playerData) {
            this.playerData.remove(uuid);
        }
    }

    public void storePlayerData(UUID uuid, PlayerData data) {
        synchronized (this.playerData) {
            this.playerData.put(uuid, data);
        }
    }

    public void assignGroupData(Map<String, GroupData> groupData) {
        synchronized (this.groupData) {
            this.groupData.clear();
            this.groupData.putAll(groupData);
        }
    }

    public void assignData(Map<String, GroupData> groupData, Map<UUID, PlayerData> playerData) {
        assignGroupData(groupData);

        synchronized (this.playerData) {
            this.playerData.clear();
            this.playerData.putAll(playerData);
        }
    }

    // ==========================================
    // Below are methods used by the API/Commands
    // ==========================================
    boolean debug() {
        return debug;
    }

    void toggleDebug() {
        debug = !debug;
        config.set("Debug", debug);
        config.save();
    }

    void toggleLongTags() {
        longNametagsEnabled = !longNametagsEnabled;
        config.set("Tablist.LongTags", longNametagsEnabled);
        config.save();
    }

    // =================================================
    // Below are methods that we have to be careful with
    // as they can be called from different threads
    // =================================================
    public PlayerData getPlayerData(Player player) {
        return player == null ? null : playerData.get(player.getUniqueId());
    }

    void addGroup(GroupData data) {
        abstractConfig.add(data);

        synchronized (this.groupData) {
            this.groupData.put(data.getGroupName(), data);
        }
    }

    void deleteGroup(GroupData data) {
        abstractConfig.delete(data);

        synchronized (this.groupData) {
            this.groupData.remove(data.getGroupName());
        }
    }

    @Unmodifiable
    public Map<String, GroupData> getGroupData() {
        synchronized (this.groupData) {
            return Collections.unmodifiableMap(this.groupData);
        }
    }

    public GroupData getGroupData(String groupName) {
        synchronized (this.groupData) {
            return this.groupData.get(groupName);
        }
    }

    /**
     * Replaces placeholders when a player tag is created.
     * Maxim and Clip's plugins are searched for, and input
     * is replaced. We use direct imports to avoid any problems!
     * (So don't change that)
     */
    public Component formatWithPlaceholders(Player player, String input) {
        plugin.debug("Formatting text..");
        if (input == null) return Component.empty();
        if (player == null) return Colors.color(input);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            plugin.debug("Trying to use PlaceholderAPI for placeholders");
            input = PlaceholderAPI.setPlaceholders(player, input);
        }

        plugin.debug("Applying colors..");
        return Colors.color(input);
    }

    private ScheduledTask createTask(String path, ScheduledTask existing, Runnable runnable) {
        if (existing != null) {
            existing.cancel();
        }

        if (config.getInt(path, -1) <= 0) return null;
        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, t -> runnable.run(), 1, config.getInt(path), TimeUnit.SECONDS);
    }

    public void reload() {
        config.reload(true);
        applyConfig();
        nametagManager.reset();
        abstractConfig.reload();
    }

    private void applyConfig() {
        this.debug = config.getBoolean("Debug");
        this.tabListEnabled = config.getBoolean("Tablist.Enabled");
        this.longNametagsEnabled = config.getBoolean("Tablist.LongTags");
        this.refreshTagOnWorldChange = config.getBoolean("RefreshTagOnWorldChange");
        DISABLE_PUSH_ALL_TAGS = config.getBoolean("DisablePush");

        clearEmptyTeamTask = createTask("ClearEmptyTeamsInterval", clearEmptyTeamTask, () -> Bukkit.getGlobalRegionScheduler().run(plugin, task -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nte teams clear")));

        refreshNametagTask = createTask("RefreshInterval", refreshNametagTask, () -> {
            nametagManager.reset();
            applyTags();
        });
    }

    public void applyTags() {
        for (Player online : Utils.getOnline()) {
            if (online != null) {
                online.getScheduler().run(plugin, t -> applyTagToPlayer(online, false), () -> {});
            }
        }

        plugin.debug("Applied tags to all online players.");
    }

    public void applyTagToPlayer(final Player player, final boolean loggedIn) {
        // If on the primary thread, run async
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getAsyncScheduler().runNow(plugin, t -> applyTagToPlayer(player, loggedIn));
            return;
        }

        INametag tempNametag = getPlayerData(player);
        if (tempNametag == null) {
            for (GroupData group : getGroupData().values()) {
                if (player.hasPermission(group.getBukkitPermission())) {
                    tempNametag = group;
                    break;
                }
            }
        }

        if (tempNametag == null) return;
        plugin.debug("Applying " + (tempNametag.isPlayerTag() ? "PlayerTag" : "GroupTag") + " to " + player.getName());

        final INametag nametag = tempNametag;
        player.getScheduler().run(plugin, t -> {
            nametagManager.setNametag(player.getName(), formatWithPlaceholders(player, nametag.getPrefix()),
                formatWithPlaceholders(player, nametag.getSuffix()), nametag.getSortPriority(), false, true, nametag.nameFormattingOverride());

            // If the TabList is disabled...
            if (!tabListEnabled) {
                // apply the default white username to the player.
                player.playerListName(Component.text(PlainTextComponentSerializer.plainText().serialize(player.playerListName()), NamedTextColor.WHITE));
            } else {
                if (longNametagsEnabled) {
                    player.playerListName(formatWithPlaceholders(player, nametag.getPrefix() + player.getName() + nametag.getSuffix()));
                } else {
                    player.playerListName(null);
                }
            }

            if (loggedIn) {
                Bukkit.getPluginManager().callEvent(new NametagFirstLoadedEvent(player, nametag));
            }
        }, () -> {});
    }

    void clear(final CommandSender sender, final String player) {
        Player target = Bukkit.getPlayerExact(player);
        if (target != null) {
            handleClear(target.getUniqueId(), player);
            return;
        }

        UUIDFetcher.lookupUUID(player, plugin, uuid -> {
            if (uuid == null) {
                NametagMessages.UUID_LOOKUP_FAILED.send(sender);
            } else {
                handleClear(uuid, player);
            }
        });
    }

    void save(CommandSender sender, boolean playerTag, String key, int priority) {
        if (playerTag) {
            Player player = Bukkit.getPlayerExact(key);

            PlayerData data = getPlayerData(player);
            if (data == null) {
                abstractConfig.savePriority(true, key, priority);
                return;
            }

            data.setSortPriority(priority);
            abstractConfig.save(data);
        } else {
            GroupData groupData = getGroupData(key);

            if (groupData == null) {
                sender.sendMessage(ChatColor.RED + "Group " + key + " does not exist!");
                return;
            }

            groupData.setSortPriority(priority);
            abstractConfig.save(groupData);
        }
    }

    public void save(String targetName, NametagEvent.ChangeType changeType, String value) {
        save(null, targetName, changeType, value);
    }

    // Reduces checks to have this method (ie not saving data twice)
    public void save(String targetName, String prefix, String suffix) {
        Player player = Bukkit.getPlayerExact(targetName);

        PlayerData data = getPlayerData(player);
        if (data == null) {
            data = new PlayerData(targetName, null, "", "", -1);
            if (player != null) {
                storePlayerData(player.getUniqueId(), data);
            }
        }

        data.setPrefix(prefix);
        data.setSuffix(suffix);

        if (player != null) {
            applyTagToPlayer(player, false);
            data.setUuid(player.getUniqueId());
            abstractConfig.save(data);
            return;
        }

        final PlayerData finalData = data;
        UUIDFetcher.lookupUUID(targetName, plugin, (uuid) -> {
            if (uuid != null) {
                storePlayerData(uuid, finalData);
                finalData.setUuid(uuid);
                abstractConfig.save(finalData);
            }
        });
    }

    void save(final CommandSender sender, String targetName, NametagEvent.ChangeType changeType, String value) {
        Player player = Bukkit.getPlayerExact(targetName);

        PlayerData data = getPlayerData(player);
        if (data == null) {
            data = new PlayerData(targetName, null, "", "", -1);
            if (player != null) {
                storePlayerData(player.getUniqueId(), data);
            }
        }

        if (changeType == NametagEvent.ChangeType.PREFIX) {
            data.setPrefix(value);
        } else {
            data.setSuffix(value);
        }

        if (player != null) {
            applyTagToPlayer(player, false);
            abstractConfig.save(data);
            return;
        }

        final PlayerData finalData = data;
        UUIDFetcher.lookupUUID(targetName, plugin, (uuid) -> {
            if (uuid == null && sender != null) { // null is passed in api
                NametagMessages.UUID_LOOKUP_FAILED.send(sender);
            }
            else {
                storePlayerData(uuid, finalData);
                abstractConfig.save(finalData);
            }
        });
    }

    public boolean isLongNametagsEnabled() {
        return longNametagsEnabled;
    }

    public AbstractConfig getAbstractConfig() {
        return abstractConfig;
    }

    public Configuration getConfig() {
        return config;
    }

    public Map<UUID, PlayerData> getPlayerData() {
        return playerData;
    }

    public NametagEdit getPlugin() {
        return plugin;
    }

    public boolean isDebug() {
        return debug;
    }
}