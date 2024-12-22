package com.nametagedit.plugin.packets;

import com.nametagedit.plugin.NametagEdit;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Packets {
    public static void broadcast(final Packet<?> packet) {
        send(Bukkit.getServer().getOnlinePlayers(), packet);
    }

    public static void send(final Collection<? extends Player> players, final Packet<?> packet) {
        if (packet == null)
            return;

        for (final Player player : players) {
            player.getScheduler().run(NametagEdit.getInstance(), task -> {
                ((CraftPlayer) player).getHandle().connection.send(packet);
            }, null);
        }
    }
}
