package com.nametagedit.plugin;

import com.nametagedit.plugin.api.data.FakeTeam;
import com.nametagedit.plugin.packets.Packets;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter") // We synchronize on the FakeTeam parameter in methods that send packets
public class NametagManager {

    private final Map<String, FakeTeam> TEAMS = new ConcurrentHashMap<>();
    private final ReadWriteLock TEAMS_LOCK = new ReentrantReadWriteLock();

    private final Map<String, FakeTeam> CACHED_FAKE_TEAMS = new ConcurrentHashMap<>();
    private final NametagEdit plugin;

    public NametagManager(final NametagEdit plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the current team given a prefix and suffix
     * If there is no team similar to this, then a new
     * team is created.
     */
    @Nullable
    private FakeTeam getFakeTeam(String name, int sortPriority, Component prefix, Component suffix, boolean visible) {
        return TEAMS.values().stream().filter(fakeTeam -> fakeTeam.isSimilar(name, sortPriority, prefix, suffix, visible)).findFirst().orElse(null);
    }

    /**
     * Adds a player to a FakeTeam. If they are already on this team,
     * we do NOT change that.
     */
    private void addPlayerToTeam(String player, Component prefix, Component suffix, int sortPriority, boolean playerTag, boolean visible, NamedTextColor nameFormattingOverride) {
        String addingName;
        Player adding = Bukkit.getPlayerExact(player);
        if (adding != null)
            addingName = adding.getName();
        else {
            addingName = Bukkit.getOfflinePlayer(player).getName();
        }

        if (addingName == null)
            return;

        player = addingName;

        FakeTeam previous = getFakeTeam(player);

        if (previous != null && previous.isSimilar(player, sortPriority, prefix, suffix, visible)) {
            plugin.debug(player + " already belongs to a similar team (" + previous.getName() + ")");
            return;
        }

        reset(player);

        FakeTeam joining;
        try {
            TEAMS_LOCK.readLock().lock();
            joining = getFakeTeam(player, sortPriority, prefix, suffix, visible);
        } finally {
            TEAMS_LOCK.readLock().unlock();
        }

        if (joining != null) {
            synchronized (joining) {
                joining.addMember(player);
            }
            plugin.debug("Using existing team for " + player);
        } else {
            joining = FakeTeam.create(player, prefix, suffix, sortPriority);
            joining.setVisible(visible);
            joining.addMember(player);
            joining.setNameFormattingOverride(nameFormattingOverride);

            try {
                TEAMS_LOCK.writeLock().lock();

                TEAMS.put(joining.getName(), joining);
                plugin.debug("Created FakeTeam " + joining.getName() + ". Total teams: " + TEAMS.size());
            } finally {
                TEAMS_LOCK.writeLock().unlock();
            }

            addTeamPackets(joining);
        }

        addPlayerToTeamPackets(joining, player);
        cache(player, joining);

        plugin.debug(player + " has been added to team " + joining.getName());
    }

    public FakeTeam reset(String player) {
        return reset(player, decache(player));
    }

    private FakeTeam reset(String player, FakeTeam fakeTeam) {
        if (fakeTeam == null)
            return null;

        boolean delete = false;
        synchronized (fakeTeam) {
            if (fakeTeam.removeMember(player)) {
                Player removing = Bukkit.getPlayerExact(player);
                if (removing != null) {
                    delete = removePlayerFromTeamPackets(fakeTeam, removing.getName());
                } else {
                    OfflinePlayer toRemoveOffline = Bukkit.getOfflinePlayerIfCached(player);
                    if (toRemoveOffline == null || toRemoveOffline.getName() == null)
                        return fakeTeam;

                    delete = removePlayerFromTeamPackets(fakeTeam, toRemoveOffline.getName());
                }

                plugin.debug(player + " was removed from " + fakeTeam.getName());
            }
        }

        if (delete) {
            try {
                TEAMS_LOCK.writeLock().lock();

                TEAMS.remove(fakeTeam.getName());
                plugin.debug("FakeTeam " + fakeTeam.getName() + " has been deleted. Total teams: " + TEAMS.size());
            } finally {
                TEAMS_LOCK.writeLock().unlock();
            }

            removeTeamPackets(fakeTeam);
        }

        return fakeTeam;
    }

    // ==============================================================
    // Below are public methods to modify the cache
    // ==============================================================
    private FakeTeam decache(String player) {
        return CACHED_FAKE_TEAMS.remove(player);
    }

    public FakeTeam getFakeTeam(String player) {
        return CACHED_FAKE_TEAMS.get(player);
    }

    private void cache(String player, FakeTeam fakeTeam) {
        CACHED_FAKE_TEAMS.put(player, fakeTeam);
    }

    // ==============================================================
    // Below are public methods to modify certain data
    // ==============================================================
    public void setNametag(String player, Component prefix, Component suffix) {
        setNametag(player, prefix, suffix, -1);
    }
    
    public void setNametag(String player, Component prefix, Component suffix, boolean visible) {
        setNametag(player, prefix, suffix, -1, false, visible, null);
    }

    void setNametag(String player, Component prefix, Component suffix, int sortPriority) {
        setNametag(player, prefix, suffix, sortPriority, false, true, null);
    }

    void setNametag(String player, Component prefix, Component suffix, int sortPriority, boolean playerTag, boolean visible, NamedTextColor nameFormattingOverride) { // TODO something to make these overloads less ridiculous
        addPlayerToTeam(player, prefix != null ? prefix : Component.empty(), suffix != null ? suffix : Component.empty(), sortPriority, playerTag, visible, nameFormattingOverride);
    }

    void sendTeams(Player player) {
        final List<Player> receiving = List.of(player);

        try {
            TEAMS_LOCK.readLock().lock();

            for (final FakeTeam fakeTeam : TEAMS.values()) {
                synchronized (fakeTeam) {
                    Packets.send(receiving, ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(fakeTeam, true));
                }
            }
        } finally {
            TEAMS_LOCK.readLock().unlock();
        }
    }

    void reset() {
        try {
            TEAMS_LOCK.writeLock().lock();
            for (FakeTeam fakeTeam : TEAMS.values()) {
                removeTeamPackets(fakeTeam);
            }

            CACHED_FAKE_TEAMS.clear();
            TEAMS.clear();
        } finally {
            TEAMS_LOCK.writeLock().unlock();
        }
    }

    int clearEmptyTeams() {
        final Set<FakeTeam> emptyTeams = new HashSet<>();

        try {
            TEAMS_LOCK.writeLock().lock();

            for (final Map.Entry<String, FakeTeam> entry : TEAMS.entrySet()) {
                if (entry.getValue().getPlayers().isEmpty()) {
                    TEAMS.remove(entry.getKey());
                    emptyTeams.add(entry.getValue());
                }
            }
        } finally {
            TEAMS_LOCK.writeLock().unlock();
        }

        for (final FakeTeam team : emptyTeams) {
            removeTeamPackets(team);
        }

        return emptyTeams.size();
    }

    // ==============================================================
    // Below are private methods to construct a new Scoreboard packet
    // ==============================================================
    private void removeTeamPackets(FakeTeam fakeTeam) {
        synchronized (fakeTeam) {
            Packets.broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(fakeTeam));
        }
    }

    private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, String... players) {
        return removePlayerFromTeamPackets(fakeTeam, Arrays.asList(players));
    }

    private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, Collection<String> players) {
        synchronized (fakeTeam) {
            Packets.broadcast(ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(fakeTeam, players, ClientboundSetPlayerTeamPacket.Action.REMOVE));
            return fakeTeam.getMembers().isEmpty();
        }
    }

    private void addTeamPackets(FakeTeam fakeTeam) {
        synchronized (fakeTeam) {
            Packets.broadcast(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(fakeTeam, true));
        }
    }

    private void addPlayerToTeamPackets(FakeTeam fakeTeam, String player) {
        synchronized (fakeTeam) {
            Packets.broadcast(ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(fakeTeam, List.of(player), ClientboundSetPlayerTeamPacket.Action.ADD));
        }
    }

}