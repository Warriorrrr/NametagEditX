package com.nametagedit.plugin.api.data;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * This class represents a group nametag. There
 * are several properties available.
 */
public class GroupData implements INametag {

    private final String groupName;
    private String prefix;
    private String suffix;
    private String permission;
    private Permission bukkitPermission;
    private int sortPriority;

    private NamedTextColor nameFormattingOverride = null;

    public GroupData(String groupName, String prefix, String suffix, String permission, int sortPriority) {
        this.groupName = groupName;
        this.prefix = prefix;
        this.suffix = suffix;
        setPermission(permission);
        this.sortPriority = sortPriority;
    }

    public void setPermission(String permission) {
        this.permission = permission;
        bukkitPermission = new Permission(permission, PermissionDefault.FALSE);
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public int getSortPriority() {
        return sortPriority;
    }

    public void setSortPriority(int sortPriority) {
        this.sortPriority = sortPriority;
    }

    public String getPermission() {
        return permission;
    }

    public Permission getBukkitPermission() {
        return bukkitPermission;
    }

    @Override
    public NamedTextColor nameFormattingOverride() {
        return nameFormattingOverride;
    }

    @Override
    public void nameFormattingOverride(NamedTextColor nameFormattingOverride) {
        this.nameFormattingOverride = nameFormattingOverride;
    }

    @Override
    public boolean isPlayerTag() {
        return false;
    }

}