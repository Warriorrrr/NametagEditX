package com.nametagedit.plugin.api.data;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * This class represents a Scoreboard Team. It is used
 * to keep track of the current members of a Team, and
 * is responsible for
 */
public class FakeTeam extends PlayerTeam {

    private final String name;
    private Component prefix;
    private Component suffix;
    private boolean visible = true;

    private ChatFormatting computedNameFormatting = ChatFormatting.RESET;
    private NamedTextColor nameFormattingOverride = null;

    // Both used for similarity checks
    private final String usedPlayerName; // The name used for generating this teams' full name
    private final int sortPriority;

    private FakeTeam(String fullName, String usedPlayerName, int sortPriority, Component prefix, Component suffix) {
        //noinspection DataFlowIssue
        super(null, fullName); // null is passed here as the scoreboard, but this class overrides used methods that may require it
        this.name = fullName;

        this.usedPlayerName = usedPlayerName;
        this.sortPriority = sortPriority;

        setPrefix(prefix);
        setSuffix(suffix);
    }

    public static FakeTeam create(@NotNull String player, Component prefix, Component suffix, int sortPriority, boolean playerTag, boolean visible) {
        Objects.requireNonNull(player, "player");

        String generatedName = "NT_team_" + getNameFromInput(sortPriority) + "_player_" + player + (playerTag ? "+P" : "") + (!visible ? "-V" : "");
        generatedName = generatedName.substring(0, Math.min(Short.MAX_VALUE, generatedName.length()));

        return new FakeTeam(generatedName, player, sortPriority, prefix, suffix);
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
        return this.computedNameFormatting;
    }

    @NotNull
    private ChatFormatting computeNameFormatting() {
        if (this.nameFormattingOverride != null) {
            return Objects.requireNonNullElse(PaperAdventure.asVanilla(this.nameFormattingOverride), ChatFormatting.RESET);
        } else if (!Component.empty().equals(this.prefix)) {

            Style lastStyle = Style.empty();
            for (Component child : this.prefix.iterable(ComponentIteratorType.DEPTH_FIRST))
                lastStyle = child.style();

            return Optional.ofNullable(lastStyle.color()).map(NamedTextColor::nearestTo).map(PaperAdventure::asVanilla).orElse(ChatFormatting.RESET);
        } else {
            return ChatFormatting.RESET;
        }
    }

    public void setNameFormattingOverride(NamedTextColor nameFormattingOverride) {
        this.nameFormattingOverride = nameFormattingOverride;
    }

    public Component getPrefix() {
        return this.prefix;
    }

    public void setPrefix(@NotNull Component prefix) {
        this.prefix = prefix;
        this.computedNameFormatting = computeNameFormatting();
    }

    public Component getSuffix() {
        return this.suffix;
    }

    public void setSuffix(Component suffix) {
        this.suffix = suffix;
    }

    public boolean isSimilar(String name, int sortPriority, Component prefix, Component suffix, boolean visible) {
        return this.usedPlayerName.equals(name) && this.sortPriority == sortPriority && this.prefix.equals(prefix) && this.suffix.equals(suffix) && this.visible == visible;
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
        if (input <= 0) return "A";

        final StringBuilder name = new StringBuilder();

        while (input > 0) {
            if (input >= 26) {
                name.append('Z');
                input -= 26;
            } else {
                char letter = (char) ('A' + (input - 1) % 26);
                name.append(letter);
                break;
            }
        }

        return name.toString();
    }

}
