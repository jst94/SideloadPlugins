/*
 * Copyright (c) 2017, Devin French <https://github.com/devinfrench>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *	list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *	this list of conditions and the following disclaimer in the documentation
 *	and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lucidplugins.inferno;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.lucidplugins.api.utils.Reachable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.lucidplugins.inferno.InfernoPlugin.*;

public class InfernoNPC {
    private NPC npc;
    private Type type;
    private Attack nextAttack;
    private int ticksTillNextAttack;
    private int lastAnimation;
    private boolean lastCanAttack;
    private int idleTicks;
    private final Map<WorldPoint, Integer> safeSpotCache;

    InfernoNPC(NPC npc) {
        this.npc = npc;
        this.type = Type.typeFromId(npc.getId());
        this.nextAttack = this.type.getDefaultAttack();
        this.ticksTillNextAttack = 0;
        this.lastAnimation = -1;
        this.lastCanAttack = false;
        this.idleTicks = 0;
        this.safeSpotCache = new HashMap<WorldPoint, Integer>();
    }

    void updateNextAttack(Attack nextAttack, int ticksTillNextAttack) {
        this.nextAttack = nextAttack;
        this.ticksTillNextAttack = ticksTillNextAttack;
    }

    private void updateNextAttack(Attack nextAttack) {
        this.nextAttack = nextAttack;
    }

    boolean canAttack(Client client, WorldPoint target) {
        boolean hasRange = false;
        if (this.safeSpotCache.containsKey(target)) {
            return this.safeSpotCache.get(target) == 2;
        }
        boolean hasLos = new WorldArea(target, 1, 1).hasLineOfSightTo(client.getTopLevelWorldView(), this.getNpc().getWorldArea());
        if (this.getType().getDefaultAttack() == Attack.MELEE) {
            this.getNpc().getWorldArea().isInMeleeDistance(target);
        } else {
            boolean bl = hasRange = this.getNpc().getWorldArea().distanceTo(target) <= this.getType().getRange();
        }
        if (hasLos && hasRange) {
            this.safeSpotCache.put(target, 2);
        }
        return hasLos && hasRange;
    }

    boolean canMoveToAttack(Client client, WorldPoint target, List<WorldPoint> obstacles) {
        if (!this.safeSpotCache.containsKey(target)) {
            ArrayList<WorldPoint> realObstacles = new ArrayList<WorldPoint>();
            for (WorldPoint obstacle : obstacles) {
                if (this.getNpc().getWorldArea().toWorldPointList().contains(obstacle)) continue;
                realObstacles.add(obstacle);
            }
            WorldArea targetArea = new WorldArea(target, 1, 1);
            WorldArea currentWorldArea = this.getNpc().getWorldArea();
            int steps = 0;
            while (true) {
                if (++steps > 30) {
                    return false;
                }
                boolean hasRange = false;
                WorldArea predictedWorldArea = null;
                int dx = targetArea.getX() - currentWorldArea.getX();
                int dy = targetArea.getY() - currentWorldArea.getY();
                if (Reachable.canTravelInDirection(currentWorldArea, dx, dy, x -> {
                    for (WorldPoint obstacle : realObstacles) {
                        if (new WorldArea(x, 1, 1).intersectsWith(new WorldArea(obstacle, 1, 1))) {
                            return false;
                        }
                    }
                    return true;
                })) {
                    predictedWorldArea = new WorldArea(
                        currentWorldArea.getX() + Integer.signum(dx),
                        currentWorldArea.getY() + Integer.signum(dy),
                        currentWorldArea.getWidth(),
                        currentWorldArea.getHeight(),
                        currentWorldArea.getPlane()
                    );
                }
                if (predictedWorldArea == null) {
                    this.safeSpotCache.put(target, 1);
                    return true;
                }
                if (predictedWorldArea == currentWorldArea) {
                    this.safeSpotCache.put(target, 0);
                    return false;
                }
                boolean hasLos = new WorldArea(target, 1, 1).hasLineOfSightTo(client.getTopLevelWorldView(), predictedWorldArea);
                if (this.getType().getDefaultAttack() == Attack.MELEE) {
                    predictedWorldArea.isInMeleeDistance(target);
                } else {
                    boolean bl = hasRange = predictedWorldArea.distanceTo(target) <= this.getType().getRange();
                }
                if (hasLos && hasRange) {
                    this.safeSpotCache.put(target, 1);
                    return true;
                }
                currentWorldArea = predictedWorldArea;
            }
        }
        return this.safeSpotCache.get(target) == 1 || this.safeSpotCache.get(target) == 2;
    }

    private boolean couldAttackPrevTick(Client client, WorldPoint lastPlayerLocation) {
        return new WorldArea(lastPlayerLocation, 1, 1).hasLineOfSightTo(client.getTopLevelWorldView(), this.getNpc().getWorldArea());
    }

    void gameTick(Client client, WorldPoint lastPlayerLocation, boolean finalPhase) {
        Attack currentAttack;
        this.safeSpotCache.clear();
        if (this.ticksTillNextAttack > 0) {
            --this.ticksTillNextAttack;
        }
        if (this.getType() == Type.JAD && this.getNpc().getAnimation() != -1 && this.getNpc().getAnimation() != this.lastAnimation && (currentAttack = Attack.attackFromId(this.getNpc().getAnimation())) != null && currentAttack != Attack.UNKNOWN) {
            this.updateNextAttack(currentAttack, this.getType().getTicksAfterAnimation());
        }
        if (this.ticksTillNextAttack <= 0) {
            switch (this.getType()) {
                case ZUK: {
                    if (this.getNpc().getAnimation() != 7566) break;
                    if (finalPhase) {
                        this.updateNextAttack(this.getType().getDefaultAttack(), 7);
                        break;
                    }
                    this.updateNextAttack(this.getType().getDefaultAttack(), 10);
                    break;
                }
                case JAD: {
                    if (this.getNextAttack() == Attack.UNKNOWN) break;
                    this.updateNextAttack(this.getType().getDefaultAttack(), 8);
                    break;
                }
                case BLOB: {
                    if (!this.lastCanAttack && this.couldAttackPrevTick(client, lastPlayerLocation)) {
                        this.updateNextAttack(Attack.UNKNOWN, 3);
                        break;
                    }
                    if (!this.lastCanAttack && this.canAttack(client, client.getLocalPlayer().getWorldLocation())) {
                        this.updateNextAttack(Attack.UNKNOWN, 4);
                        break;
                    }
                    if (this.getNpc().getAnimation() == -1) break;
                    this.updateNextAttack(this.getType().getDefaultAttack(), this.getType().getTicksAfterAnimation());
                    break;
                }
                case BAT: {
                    if (!this.canAttack(client, client.getLocalPlayer().getWorldLocation()) || this.getNpc().getAnimation() == 7577 || this.getNpc().getAnimation() == -1) break;
                    this.updateNextAttack(this.getType().getDefaultAttack(), this.getType().getTicksAfterAnimation());
                    break;
                }
                case MELEE: 
                case RANGER: 
                case MAGE: {
                    if (this.getNpc().getAnimation() != 7597 && this.getNpc().getAnimation() != 7605 && this.getNpc().getAnimation() != 7604 && this.getNpc().getAnimation() != 7610 && this.getNpc().getAnimation() != 7612) {
                        if (this.getNpc().getAnimation() == 7600) {
                            this.updateNextAttack(this.getType().getDefaultAttack(), 12);
                            break;
                        }
                        if (this.getNpc().getAnimation() != 7611) break;
                        this.updateNextAttack(this.getType().getDefaultAttack(), 8);
                        break;
                    }
                    this.updateNextAttack(this.getType().getDefaultAttack(), this.getType().getTicksAfterAnimation());
                    break;
                }
                default: {
                    if (this.getNpc().getAnimation() == -1) break;
                    this.updateNextAttack(this.getType().getDefaultAttack(), this.getType().getTicksAfterAnimation());
                }
            }
        }
        if (this.getType() == Type.BLOB && this.getTicksTillNextAttack() == 3 && client.getLocalPlayer().getWorldLocation().distanceTo(this.getNpc().getWorldArea()) <= Type.BLOB.getRange()) {
            Attack nextBlobAttack = Attack.UNKNOWN;
            if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)) {
                nextBlobAttack = Attack.MAGIC;
            } else if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)) {
                nextBlobAttack = Attack.RANGED;
            }
            this.updateNextAttack(nextBlobAttack);
        }
        this.lastAnimation = this.getNpc().getAnimation();
        this.lastCanAttack = this.canAttack(client, client.getLocalPlayer().getWorldLocation());
    }

    NPC getNpc() {
        return this.npc;
    }

    Type getType() {
        return this.type;
    }

    Attack getNextAttack() {
        return this.nextAttack;
    }

    int getTicksTillNextAttack() {
        return this.ticksTillNextAttack;
    }

    void setTicksTillNextAttack(int ticksTillNextAttack) {
        this.ticksTillNextAttack = ticksTillNextAttack;
    }

    public int getIdleTicks() {
        return idleTicks;
    }

    public void incrementIdleTicks() {
        this.idleTicks++;
    }

    public void resetIdleTicks() {
        this.idleTicks = 0;
    }

    static enum Type {
        NIBBLER(new int[]{7691}, Attack.MELEE, 4, 99, 100),
        BAT(new int[]{7692}, Attack.RANGED, 3, 4, 7),
        BLOB(new int[]{7693}, Attack.UNKNOWN, 6, 15, 4),
        MELEE(new int[]{7697}, Attack.MELEE, 4, 1, 3),
        RANGER(new int[]{7698, 7702}, Attack.RANGED, 4, 98, 2),
        MAGE(new int[]{7699, 7703}, Attack.MAGIC, 4, 98, 1),
        JAD(new int[]{7700, 7704}, Attack.UNKNOWN, 3, 99, 0),
        HEALER_JAD(new int[]{3128, 7701, 7705}, Attack.MELEE, 4, 1, 6),
        ZUK(new int[]{7706}, Attack.UNKNOWN, 10, 99, 99),
        HEALER_ZUK(new int[]{7708}, Attack.UNKNOWN, -1, 99, 100);

        private final int[] npcIds;
        private final Attack defaultAttack;
        private final int ticksAfterAnimation;
        private final int range;
        private final int priority;

        private Type(int[] npcIds, Attack defaultAttack, int ticksAfterAnimation, int range, int priority) {
            this.npcIds = npcIds;
            this.defaultAttack = defaultAttack;
            this.ticksAfterAnimation = ticksAfterAnimation;
            this.range = range;
            this.priority = priority;
        }

        static Type typeFromId(int npcId) {
            for (Type type : Type.values()) {
                if (!ArrayUtils.contains(type.getNpcIds(), npcId)) continue;
                return type;
            }
            return null;
        }

        int[] getNpcIds() {
            return this.npcIds;
        }

        Attack getDefaultAttack() {
            return this.defaultAttack;
        }

        int getTicksAfterAnimation() {
            return this.ticksAfterAnimation;
        }

        int getRange() {
            return this.range;
        }

        int getPriority() {
            return this.priority;
        }
    }

    static enum Attack {
        MELEE(Prayer.PROTECT_FROM_MELEE, Color.ORANGE, Color.RED, new int[]{7574, 7582, 7597, 7604, 7612}),
        RANGED(Prayer.PROTECT_FROM_MISSILES, Color.GREEN, new Color(0, 128, 0), new int[]{7578, 7581, 7605, 7593}),
        MAGIC(Prayer.PROTECT_FROM_MAGIC, Color.CYAN, Color.BLUE, new int[]{7583, 7610, 7592}),
        UNKNOWN(null, Color.WHITE, Color.GRAY, new int[0]);

        private final Prayer prayer;
        private final Color normalColor;
        private final Color criticalColor;
        private final int[] animationIds;

        private Attack(Prayer prayer, Color normalColor, Color criticalColor, int[] animationIds) {
            this.prayer = prayer;
            this.normalColor = normalColor;
            this.criticalColor = criticalColor;
            this.animationIds = animationIds;
        }

        static Attack attackFromId(int animationId) {
            for (Attack attack : Attack.values()) {
                if (!ArrayUtils.contains(attack.getAnimationIds(), animationId)) continue;
                return attack;
            }
            return null;
        }

        Prayer getPrayer() {
            return this.prayer;
        }

        Color getNormalColor() {
            return this.normalColor;
        }

        Color getCriticalColor() {
            return this.criticalColor;
        }

        int[] getAnimationIds() {
            return this.animationIds;
        }
    }
}
