package com.nametagedit.plugin.api.data;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Locale;
import java.util.UUID;

/**
 * This class represents a player nametag. There
 * are several properties available.
 */
public class PlayerData implements INametag {

    private String name;
    private UUID uuid;
    private String prefix;
    private String suffix;
    private int sortPriority;

    private NamedTextColor nameFormattingOverride;
    private boolean visible = true;

    public PlayerData(final String name, final UUID uuid, final String prefix, final String suffix, final int sortPriority) {
        this.name = name;
        this.uuid = uuid;
        this.prefix = prefix;
        this.suffix = suffix;
        this.sortPriority = sortPriority;
    }

    public static PlayerData fromFile(String key, YamlConfiguration file) {
        if (!file.contains("Players." + key)) return null;

        PlayerData data = new PlayerData(
                file.getString("Players." + key + ".Name"),
                UUID.fromString(key),
                file.getString("Players." + key + ".Prefix", ""),
                file.getString("Players." + key + ".Suffix", ""),
                file.getInt("Players." + key + ".SortPriority", -1)
        );

        String formattingOverride = file.getString("Players." + key + ".NameFormattingOverride", "");
        if (!formattingOverride.isEmpty()) {
            data.nameFormattingOverride(NamedTextColor.NAMES.value(formattingOverride.toLowerCase(Locale.ROOT)));
        }

        data.visible = file.getBoolean("Players." + key + ".NameVisible", true);

        return data;
    }

    @Override
    public boolean isPlayerTag() {
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public int getSortPriority() {
        return sortPriority;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setSortPriority(int sortPriority) {
        this.sortPriority = sortPriority;
    }
    @Override
    public NamedTextColor nameFormattingOverride() {
        return this.nameFormattingOverride;
    }

    @Override
    public void nameFormattingOverride(NamedTextColor nameFormattingOverride) {
        this.nameFormattingOverride = nameFormattingOverride;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }
}