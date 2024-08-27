package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class HookLuckPerms implements Listener {

    private final NametagHandler handler;

    public HookLuckPerms(NametagHandler handler) {
        this.handler = handler;
        EventBus eventBus = LuckPermsProvider.get().getEventBus();
        eventBus.subscribe(handler.getPlugin(), NodeMutateEvent.class, this::onNodeMutate);
    }

    private void onNodeMutate(NodeMutateEvent event) {
        if (event instanceof NodeAddEvent nae) {
            if (!isGroupApplicable(nae.getNode().getKey()))
                return;
        } else if (event instanceof NodeRemoveEvent nre) {
            if (!isGroupApplicable(nre.getNode().getKey()))
                return;
        } else if (event instanceof NodeClearEvent nce) {
            boolean found = false;
            for (Node node : nce.getNodes()) {
                if (isGroupApplicable(node.getKey())) {
                    found = true;
                    break;
                }
            }

            if (!found)
                return;
        }

        if (event.getTarget() instanceof User user)
            handler.applyTagToPlayer(Bukkit.getPlayer(user.getUniqueId()), false);
        else if (event.getTarget() instanceof Group group) {
            for (final Player player : Bukkit.getServer().getOnlinePlayers())
                if (player.hasPermission("group." + group.getName()))
                    handler.applyTagToPlayer(player, false);
        }
    }

    private boolean isGroupApplicable(String key) {
        for (GroupData group : this.handler.getGroupData().values()) {
            if (group.getPermission().equals(key))
                return true;
        }

        return false;
    }
}
