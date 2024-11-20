package com.lucidplugins.jstfightcaves.QLearning;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import java.util.Objects;

@Getter
public class State {
    private final int health;
    private final int prayerPoints;
    private final WorldPoint location;
    private final boolean underAttack;
    private final boolean inCombat;
    private final boolean inSafeSpot;
    private final NPC nearestNPC;
    private static final Client client = RuneLite.getInjector().getInstance(Client.class);

    public State(Player player, NPC nearestNPC, boolean underAttack, int health, boolean inCombat, boolean inSafeSpot) {
        this.health = health;
        this.prayerPoints = client != null ? client.getBoostedSkillLevel(Skill.PRAYER) : 0;
        this.inCombat = inCombat;
        this.location = player != null ? player.getWorldLocation() : null;
        this.underAttack = underAttack;
        this.inSafeSpot = inSafeSpot;
        this.nearestNPC = nearestNPC;
    }

    public int getPlayerHealth() {
        return health;
    }

    public boolean isUnderAttack() {
        return underAttack;
    }

    public boolean isInSafeSpot() {
        return inSafeSpot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return health == state.health &&
                prayerPoints == state.prayerPoints &&
                inCombat == state.inCombat &&
                underAttack == state.underAttack &&
                inSafeSpot == state.inSafeSpot &&
                Objects.equals(location, state.location) &&
                Objects.equals(nearestNPC, state.nearestNPC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(health, prayerPoints, inCombat, location, underAttack, inSafeSpot, nearestNPC);
    }
}
