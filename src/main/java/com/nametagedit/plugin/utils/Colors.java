package com.nametagedit.plugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Colors {
    private static final Map<String, String> LEGACY_LOOKUP = new HashMap<>();
    private static final Pattern LEGACY_PATTERN = Pattern.compile("[§&][0-9a-fk-or]");

    public static Component miniMessage(final String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public static Component color(final String text) {
        return miniMessage(translateLegacyCharacters(text));
    }

    public static String translateLegacyCharacters(String input) {
        final Matcher matcher = LEGACY_PATTERN.matcher(input);

        while (matcher.find()) {
            String legacy = matcher.group();
            input = input.replace(legacy, LEGACY_LOOKUP.getOrDefault(legacy.substring(1), legacy));
        }

        return input;
    }

    public static final String DARK_RED = "<dark_red>";
    public static final String RED = "<red>";
    public static final String GOLD = "<gold>";
    public static final String YELLOW = "<yellow>";
    public static final String DARK_GREEN = "<dark_green>";
    public static final String GREEN = "<green>";
    public static final String DARK_AQUA = "<dark_aqua>";
    public static final String AQUA = "<aqua>";
    public static final String DARK_BLUE = "<dark_blue>";
    public static final String BLUE = "<blue>";
    public static final String LIGHT_PURPLE = "<light_purple>";
    public static final String DARK_PURPLE = "<dark_purple>";
    public static final String WHITE = "<white>";
    public static final String GRAY = "<gray>";
    public static final String DARK_GRAY = "<dark_gray>";
    public static final String BLACK = "<black>";

    public static final String OBFUSCATED = "<obfuscated>";
    public static final String BOLD = "<bold>";
    public static final String STRIKETHROUGH = "<strikethrough>";
    public static final String UNDERLINED = "<underlined>";
    public static final String ITALIC = "<italic>";
    public static final String RESET = "<reset>";

    static {
        LEGACY_LOOKUP.put("0", BLACK);
        LEGACY_LOOKUP.put("1", DARK_BLUE);
        LEGACY_LOOKUP.put("2", DARK_GREEN);
        LEGACY_LOOKUP.put("3", DARK_AQUA);
        LEGACY_LOOKUP.put("4", DARK_RED);
        LEGACY_LOOKUP.put("5", DARK_PURPLE);
        LEGACY_LOOKUP.put("6", GOLD);
        LEGACY_LOOKUP.put("7", GRAY);
        LEGACY_LOOKUP.put("8", DARK_GRAY);
        LEGACY_LOOKUP.put("9", BLUE);
        LEGACY_LOOKUP.put("a", GREEN);
        LEGACY_LOOKUP.put("b", AQUA);
        LEGACY_LOOKUP.put("c", RED);
        LEGACY_LOOKUP.put("d", LIGHT_PURPLE);
        LEGACY_LOOKUP.put("e", YELLOW);
        LEGACY_LOOKUP.put("f", WHITE);

        LEGACY_LOOKUP.put("k", OBFUSCATED);
        LEGACY_LOOKUP.put("l", BOLD);
        LEGACY_LOOKUP.put("m", STRIKETHROUGH);
        LEGACY_LOOKUP.put("n", UNDERLINED);
        LEGACY_LOOKUP.put("o", ITALIC);
        LEGACY_LOOKUP.put("r", RESET);
    }
}
