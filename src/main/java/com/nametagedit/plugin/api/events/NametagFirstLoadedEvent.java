package com.nametagedit.plugin.api.events;

import com.nametagedit.plugin.api.data.INametag;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an Event that is fired when a
 * player joins the server and receives their nametag.
 */
public class NametagFirstLoadedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final INametag nametag;

    public NametagFirstLoadedEvent(final Player player, final INametag nametag) {
        this.player = player;
        this.nametag = nametag;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public INametag getNametag() {
        return nametag;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}