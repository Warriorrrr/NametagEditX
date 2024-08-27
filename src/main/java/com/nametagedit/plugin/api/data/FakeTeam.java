package com.nametagedit.plugin.api.data;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents a Scoreboard Team. It is used
 * to keep track of the current members of a Team, and
 * is responsible for
 */
public class FakeTeam extends PlayerTeam {

    // This represents the number of FakeTeams that have been created.
    // It is used to generate a unique Team name.
    private static final AtomicInteger ID = new AtomicInteger(0);

    private final String name;
    private Component prefix;
    private Component suffix;
    private boolean visible = true;

    private NamedTextColor nameFormattingOverride = null;

    private FakeTeam(String name, Component prefix, Component suffix) {
        super(null, name);
        this.name = name;

        setPrefix(prefix);
        setSuffix(suffix);
    }

    public static FakeTeam create(Component prefix, Component suffix, int sortPriority, boolean playerTag) {
        String generatedName = getNameFromInput(sortPriority) + ID.incrementAndGet() + (playerTag ? "+P" : "");
        generatedName = generatedName.substring(0, Math.min(256, generatedName.length()));

        return new FakeTeam(generatedName, prefix, suffix);
    }

    public void addMember(final @NotNull String player) {
        this.getPlayers().add(Objects.requireNonNull(player, "player"));
    }

    @Unmodifiable
    public Collection<String> getMembers() {
        return Collections.unmodifiableCollection(this.getPlayers());
    }

    public boolean removeMember(final String player) {
        return this.getPlayers().remove(player);
    }

    public boolean removeMembers(final Collection<String> players) {
        return this.getPlayers().removeAll(players);
    }

    @NotNull
    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public net.minecraft.network.chat.@NotNull Component getPlayerPrefix() {
        return PaperAdventure.asVanilla(this.prefix);
    }

    @Override
    public void setPlayerPrefix(net.minecraft.network.chat.Component component) {
        setPrefix(PaperAdventure.asAdventure(component));
    }

    @Override
    public net.minecraft.network.chat.@NotNull Component getPlayerSuffix() {
        return PaperAdventure.asVanilla(this.suffix);
    }

    @Override
    public void setPlayerSuffix(net.minecraft.network.chat.Component component) {
        setSuffix(PaperAdventure.asAdventure(component));
    }

    @Override
    public Team.@NotNull Visibility getNameTagVisibility() {
        return this.visible ? Visibility.ALWAYS : Visibility.NEVER;
    }

    @Override
    @NotNull
    public ChatFormatting getColor() {
        if (this.nameFormattingOverride != null) {
            return PaperAdventure.asVanilla(this.nameFormattingOverride);
        } else if (this.prefix.color() != null) {
            return PaperAdventure.asVanilla(Objects.requireNonNull(this.prefix.color()));
        } else
            return ChatFormatting.RESET;
    }

    public void setNameFormattingOverride(NamedTextColor nameFormattingOverride) {
        this.nameFormattingOverride = nameFormattingOverride;
    }

    public Component getPrefix() {
        return this.prefix;
    }

    public void setPrefix(@NotNull Component prefix) {
        this.prefix = prefix;
    }

    public Component getSuffix() {
        return this.suffix;
    }

    public void setSuffix(Component suffix) {
        this.suffix = suffix;
    }

    public boolean isSimilar(Component prefix, Component suffix, boolean visible) {
        return this.prefix.equals(prefix) && this.suffix.equals(suffix) && this.visible == visible;
    }

    /**
     * This is a special method to sort nametags in
     * the tablist. It takes a priority and converts
     * it to an alphabetic representation to force a
     * specific sort.
     *
     * @param input the sort priority
     * @return the team name
     */
    private static String getNameFromInput(int input) {
        if (input < 0) return "Z";
        char letter = (char) ((input / 5) + 65);
        int repeat = input % 5 + 1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            builder.append(letter);
        }
        return builder.toString();
    }

}
