package com.lucidplugins.jstfightcaves.Variables;

import net.runelite.api.Skill;

public enum AttackStyle {
    ACCURATE("Accurate", Skill.ATTACK),
    AGGRESSIVE("Aggressive", Skill.STRENGTH),
    DEFENSIVE("Defensive", Skill.DEFENCE),
    CONTROLLED("Controlled", Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE),
    RANGING("Ranging", Skill.RANGED),
    LONGRANGE("Longrange", Skill.RANGED, Skill.DEFENCE),
    CASTING("Casting", Skill.MAGIC),
    DEFENSIVE_CASTING("Defensive Casting", Skill.MAGIC, Skill.DEFENCE),
    OTHER("Other", new Skill[0]);

    private final String name;
    private final Skill[] skills;

    private AttackStyle(String name, Skill ... skills) {
        this.name = name;
        this.skills = skills;
    }

    public String getName() {
        return this.name;
    }

    public Skill[] getSkills() {
        return this.skills;
    }
}
