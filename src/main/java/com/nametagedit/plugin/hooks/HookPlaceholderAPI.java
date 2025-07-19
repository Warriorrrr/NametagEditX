package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagEdit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class HookPlaceholderAPI implements Listener {
    private static final String NAME = "PlaceholderAPI";

    public boolean usingPlaceholderAPI;

    public HookPlaceholderAPI(NametagEdit plugin) {
        this.usingPlaceholderAPI = plugin.getServer().getPluginManager().isPluginEnabled(NAME);
    }

    @EventHandler
    public void onPAPIDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equals(NAME)) {
            this.usingPlaceholderAPI = false;
        }
    }

    @EventHandler
    public void onPAPIEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals(NAME)) {
            this.usingPlaceholderAPI = true;
        }
    }
}
