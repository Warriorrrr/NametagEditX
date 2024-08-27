package com.nametagedit.plugin.utils;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String format(String[] text, int to, int from) {
        return StringUtils.join(text, ' ', to, from).replace("'", "");
    }

    public static String deformat(String input) {
        return input.replace("§", "&");
    }

    public static String formatLegacy(String input) {
        return formatLegacy(input, false);
    }

    public static String formatLegacy(String input, boolean limitChars) {
        String colored = colorLegacy(input);

        return limitChars && colored.length() > 256 ? colored.substring(0, 256) : colored;
    }

    public static String colorLegacy(String text) {
        if (text == null) return "";

        text = ChatColor.translateAlternateColorCodes('&', text);

        final char colorChar = ChatColor.COLOR_CHAR;

        final Matcher matcher = hexPattern.matcher(text);
        final StringBuilder buffer = new StringBuilder(text.length() + 4 * 8);

        while (matcher.find()) {
            final String group = matcher.group(1);

            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }

        text = matcher.appendTail(buffer).toString();
        return text;
    }

    public static Collection<? extends Player> getOnline() {
        return Bukkit.getServer().getOnlinePlayers();
    }

    public static YamlConfiguration getConfig(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
                return new YamlConfiguration();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public static YamlConfiguration getConfig(File file, String resource, Plugin plugin) {
        if (!file.exists()) {
            try (InputStream inputStream = plugin.getResource(resource)) {
                if (inputStream != null) {
                    Files.copy(inputStream, file.toPath());
                }
            } catch (IOException e) {
                plugin.getSLF4JLogger().warn("Failed to copy resource '{}' from plugin jar", resource, e);
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }
}