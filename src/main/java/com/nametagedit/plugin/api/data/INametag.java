package com.nametagedit.plugin.api.data;

import net.kyori.adventure.text.format.NamedTextColor;

public interface INametag {
    String getPrefix();

    String getSuffix();

    int getSortPriority();

    boolean isPlayerTag();

    default NamedTextColor nameFormattingOverride() {
        return null;
    }
}