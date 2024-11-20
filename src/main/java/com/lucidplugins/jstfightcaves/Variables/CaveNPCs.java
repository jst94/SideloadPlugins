package com.lucidplugins.jstfightcaves.Variables;

import com.lucidplugins.jstfightcaves.Variables.AttackType;

import net.runelite.api.Prayer;

public class CaveNPCs {
    private String name;
    private int attackSpeed;
    private Prayer requiredPrayer;
    private int attackPriority;
    private AttackType attackType;
    private int attackAnimationId;
    private boolean isMelee;
    private int attackRange;

    public CaveNPCs(String name, int attackSpeed, Prayer requiredPrayer, int attackPriority, int attackAnimationId, boolean isMelee, int attackRange) {
        this.name = name;
        this.attackSpeed = attackSpeed;
        this.requiredPrayer = requiredPrayer;
        this.attackPriority = attackPriority;
        this.attackAnimationId = attackAnimationId;
        this.isMelee = isMelee;
        this.attackRange = attackRange;
    }

    public String getName() {
        return this.name;
    }

    public Prayer getPrayer() {
        return this.requiredPrayer;
    }

    public int getAttackSpeed() {
        return this.attackSpeed;
    }

    public int getAttackPriority() {
        return this.attackPriority;
    }

    public int getAttackAnimationId() {
        return this.attackAnimationId;
    }

    public boolean isMelee() {
        return this.isMelee;
    }

    public int getAttackRange() {
        return this.attackRange;
    }

    public Prayer getRequiredPrayer() {
        return this.requiredPrayer;
    }

    public AttackType getAttackType() {
        return this.attackType;
    }
}
