package com.lucidplugins.lucidscurriushelper;

import com.lucidplugins.api.utils.CombatUtils;
import com.lucidplugins.api.utils.InventoryUtils;
import com.lucidplugins.api.utils.NpcUtils;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;

import java.util.List;
import java.util.Set;

public class CombatHandler {
    private final LucidScurriusHelperConfig config;
    private final Client client;
    private final List<Projectile> attacks;
    private final Set<Integer> foodIds;
    private final PluginState state;

    public CombatHandler(LucidScurriusHelperConfig config,
                        Client client, List<Projectile> attacks, Set<Integer> foodIds, PluginState state) {
        this.config = config;
        this.client = client;
        this.attacks = attacks;
        this.foodIds = foodIds;
        this.state = state;
    }

    public static class Constants {
        public static final int RANGED_PROJECTILE = 1995;
        public static final int MAGIC_PROJECTILE = 1996; // Adding magic projectile constant
        public static final int SCURRIUS_MELEE_POSE = 9257; // Adding melee pose constant
    }

    void handlePrayers() {
        if (!config.autoPray()) {
            return;
        }

        boolean hasMagicAttack = false;
        boolean hasRangedAttack = false;
        boolean hasMeleeAttack = false;

        for (Projectile projectile : attacks) {
            if (projectile.getId() == Constants.RANGED_PROJECTILE) {
                hasRangedAttack = true;
            } else if (projectile.getId() == Constants.MAGIC_PROJECTILE) {
                hasMagicAttack = true;
            }
        }

        NPC scurrius = NpcUtils.getNearestNpc("Scurrius");
        if (scurrius != null && 
            scurrius.getAnimation() == -1 &&
 
            scurrius.getPoseAnimation() == Constants.SCURRIUS_MELEE_POSE) {
            hasMeleeAttack = true;
        }

        if (hasMagicAttack) {
            CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC);
        } else if (hasRangedAttack) {
            CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MISSILES);
        } else if (hasMeleeAttack) {
            CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE);
        } else if (client.getTickCount() - state.getLastActivateTick() > 2) {
            CombatUtils.deactivatePrayers(true);
        }
    }

    void handleAutoEat() {
        if (!config.autoEat() || client.getTickCount() - state.getLastEatTick() < 3) {
            return;
        }

        if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.eatAtHp()) {
            for (int foodId : foodIds) {
                if (InventoryUtils.contains(foodId)) {
                    InventoryUtils.itemInteract(foodId, "Eat");
                    state.setLastEatTick(client.getTickCount());
                    return;
                }
            }
            state.setNeedsRestock(true);
        }
    }
}