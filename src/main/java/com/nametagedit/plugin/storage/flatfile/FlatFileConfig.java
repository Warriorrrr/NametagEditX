package com.nametagedit.plugin.storage.flatfile;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.utils.UUIDFetcher;
import com.nametagedit.plugin.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FlatFileConfig implements AbstractConfig {

    private File groupsFile;
    private File playersFile;

    private YamlConfiguration groups;
    private YamlConfiguration players;

    private final NametagEdit plugin;
    private final NametagHandler handler;

    public FlatFileConfig(NametagEdit plugin, NametagHandler handler) {
        this.plugin = plugin;
        this.handler = handler;
    }

    @Override
    public void load() {
        groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        groups = Utils.getConfig(groupsFile, "groups.yml", plugin);
        playersFile = new File(plugin.getDataFolder(), "players.yml");
        players = Utils.getConfig(playersFile, "players.yml", plugin);
        loadGroups();
        loadPlayers();

        handler.applyTags();
    }

    @Override
    public void reload() {
        handler.clearMemoryData();

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> load());
    }

    @Override
    public void shutdown() {
        // NOTE: Nothing to do
    }

    @Override
    public void load(Player player, boolean loggedIn) {
        loadPlayerTag(player);
        plugin.getHandler().applyTagToPlayer(player, loggedIn);
    }

    @Override
    public void save(PlayerData... data) {
        for (PlayerData playerData : data) {
            UUID uuid = playerData.getUuid();
            String name = playerData.getName();
            players.set("Players." + uuid + ".Name", name);
            players.set("Players." + uuid + ".Prefix", Utils.deformat(playerData.getPrefix()));
            players.set("Players." + uuid + ".Suffix", Utils.deformat(playerData.getSuffix()));
            players.set("Players." + uuid + ".SortPriority", playerData.getSortPriority());

            if (playerData.nameFormattingOverride() != null) {
                players.set("Players." + uuid + ".NameFormattingOverride", playerData.nameFormattingOverride().toString());
            }

            players.set("Players." + uuid + ".NameVisible", playerData.isVisible());
        }

        save(players, playersFile);
    }

    @Override
    public void save(final GroupData... data) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            for (GroupData groupData : data) {
                storeGroup(groupData);
            }

            save(groups, groupsFile);
        });
    }

    @Override
    public void savePriority(boolean playerTag, String key, final int priority) {
        if (playerTag) {
            final Player target = Bukkit.getPlayerExact(key);
            if (target != null) {
                if (players.contains("Players." + target.getUniqueId())) {
                    players.set("Players." + target.getUniqueId() + ".SortPriority", priority);
                    save(players, playersFile);
                }
                return;
            }

            UUIDFetcher.lookupUUID(key, plugin, uuid -> {
                if (players.contains("Players." + uuid.toString())) {
                    players.set("Players." + uuid + ".SortPriority", priority);
                    save(players, playersFile);
                }
            });
        }
    }

    @Override
    public void delete(GroupData groupData) {
        groups.set("Groups." + groupData.getGroupName(), null);
        save(groups, groupsFile);
    }

    @Override
    public void add(GroupData groupData) {
        // NOTE: Nothing to do
    }

    @Override
    public void clear(UUID uuid, String targetName) {
        handler.removePlayerData(uuid);
        players.set("Players." + uuid.toString(), null);
        save(players, playersFile);
    }

    @Override
    public void orderGroups(CommandSender commandSender, List<String> order) {
        groups.set("Groups", null);
        for (String set : order) {
            GroupData groupData = handler.getGroupData(set);
            if (groupData != null) {
                storeGroup(groupData);
            }
        }

        for (GroupData groupData : handler.getGroupData().values()) {
            if (!groups.contains("Groups." + groupData.getGroupName())) {
                storeGroup(groupData);
            }
        }

        save(groups, groupsFile);
    }

    private void save(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlayerTag(Player player) {
        PlayerData data = PlayerData.fromFile(player.getUniqueId().toString(), players);
        if (data != null) {
            handler.storePlayerData(player.getUniqueId(), data);
        }
    }

    private void loadPlayers() {
        for (Player player : Utils.getOnline()) {
            loadPlayerTag(player);
        }
    }

    private void loadGroups() {
        List<GroupData> groupData = new ArrayList<>();
        for (String groupName : groups.getConfigurationSection("Groups").getKeys(false)) {
            GroupData data = new GroupData(
                    groupName,
                    groups.getString("Groups." + groupName + ".Prefix", ""),
                    groups.getString("Groups." + groupName + ".Suffix", ""),
                    groups.getString("Groups." + groupName + ".Permission", "nte.default"),
                    groups.getInt("Groups." + groupName + ".SortPriority", -1)
            );

            String formattingOverride = groups.getString("Groups." + groupName + ".NameFormattingOverride", "");
            if (!formattingOverride.isEmpty()) {
                data.nameFormattingOverride(NamedTextColor.NAMES.value(formattingOverride.toLowerCase(Locale.ROOT)));
            }

            groupData.add(data);
        }

        handler.assignGroupData(groupData);
    }

    private void storeGroup(GroupData groupData) {
        groups.set("Groups." + groupData.getGroupName() + ".Permission", groupData.getPermission());
        groups.set("Groups." + groupData.getGroupName() + ".Prefix", Utils.deformat(groupData.getPrefix()));
        groups.set("Groups." + groupData.getGroupName() + ".Suffix", Utils.deformat(groupData.getSuffix()));
        groups.set("Groups." + groupData.getGroupName() + ".SortPriority", groupData.getSortPriority());

        if (groupData.nameFormattingOverride() != null) {
            groups.set("Groups." + groupData.getGroupName() + ".NameFormattingOverride", groupData.nameFormattingOverride().toString());
        }
    }

}