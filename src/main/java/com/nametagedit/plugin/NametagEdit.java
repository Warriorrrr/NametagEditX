package com.nametagedit.plugin;

import com.nametagedit.plugin.api.INametagApi;
import com.nametagedit.plugin.api.NametagAPI;
import com.nametagedit.plugin.hooks.HookLuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class NametagEdit extends JavaPlugin {

    private static NametagEdit instance;

    private static INametagApi api;

    private NametagHandler handler;
    private NametagManager manager;

    public static INametagApi getApi() {
        return api;
    }

    @Override
    public void onEnable() {
        testCompat();
        if (!isEnabled()) return;

        instance = this;

        manager = new NametagManager(this);
        handler = new NametagHandler(this, manager);

        PluginManager pluginManager = Bukkit.getPluginManager();

        if (checkShouldRegister("LuckPerms")) {
            pluginManager.registerEvents(new HookLuckPerms(handler), this);
        }

        Objects.requireNonNull(getCommand("ne")).setExecutor(new NametagCommand(handler));

        if (api == null) {
            api = new NametagAPI(handler, manager);
        }
    }

    public static NametagEdit getInstance(){
        return instance;
    }

    @Override
    public void onDisable() {
        //manager.reset();
        handler.getAbstractConfig().shutdown();
    }

    void debug(String message) {
        if (handler != null && handler.debug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    private boolean checkShouldRegister(String plugin) {
        if (Bukkit.getPluginManager().getPlugin(plugin) == null) return false;
        getLogger().info("Found " + plugin + "! Hooking in.");
        return true;
    }

    private void testCompat() {
        try {
            Class.forName(CraftPlayer.class.getName());
        } catch (ClassNotFoundException e) {
            Bukkit.getPluginManager().disablePlugin(this);
            getLogger().severe("\n------------------------------------------------------\n" +
                    "[WARNING] NametagEdit v" + getDescription().getVersion() + " Failed to load! [WARNING]" +
                    "\n------------------------------------------------------" +
                    "\nThis might be an issue with reflection. REPORT this:\n> " +
                    e +
                    "\nThe plugin will now self destruct.\n------------------------------------------------------");
        }
    }

    public NametagHandler getHandler() {
        return handler;
    }

    public NametagManager getManager() {
        return manager;
    }
}