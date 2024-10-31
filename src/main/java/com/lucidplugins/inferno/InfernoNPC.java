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

class InfernoNPC
{
	@Getter(AccessLevel.PACKAGE)
	private NPC npc;
	@Getter(AccessLevel.PACKAGE)
	private Type type;
	@Getter(AccessLevel.PACKAGE)
	private Attack nextAttack;
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private int ticksTillNextAttack;
	@Getter(AccessLevel.PACKAGE)
	private int idleTicks;
	private int lastAnimation;
	private boolean lastCanAttack;
	//0 = not in LOS, 1 = in LOS after move, 2 = in LOS
	private final Map<WorldPoint, Integer> safeSpotCache;

	InfernoNPC(NPC npc)
	{
		this.npc = npc;
		this.type = Type.typeFromId(npc.getId());
		this.nextAttack = type.getDefaultAttack();
		this.ticksTillNextAttack = 0;
		this.lastAnimation = -1;
		this.lastCanAttack = false;
		this.idleTicks = 0;
		this.safeSpotCache = new HashMap<>();
	}

	void updateNextAttack(Attack nextAttack, int ticksTillNextAttack)
	{
		this.idleTicks = 0;
		this.nextAttack = nextAttack;
		this.ticksTillNextAttack = ticksTillNextAttack;
	}

	private void updateNextAttack(Attack nextAttack)
	{
		this.nextAttack = nextAttack;
	}

	boolean canAttack(Client client, WorldPoint target)
	{
		if (safeSpotCache.containsKey(target))
		{
			return safeSpotCache.get(target) == 2;
		}

		boolean hasLos = new WorldArea(target, 1, 1).hasLineOfSightTo(client.getTopLevelWorldView(), this.getNpc().getWorldArea());
		boolean hasRange = this.getType().getDefaultAttack() == Attack.MELEE ? this.getNpc().getWorldArea().isInMeleeDistance(target)
			: this.getNpc().getWorldArea().distanceTo(target) <= this.getType().getRange();

		if (hasLos && hasRange)
		{
			safeSpotCache.put(target, 2);
		}

		return hasLos && hasRange;
	}

	/**
	 * Calculates the next area that will be occupied if this area attempts
	 * to move toward it by using the normal NPC travelling pattern.
	 *
	 * @param client the client to calculate with
	 * @param target the target area
	 * @param stopAtMeleeDistance whether to stop at melee distance to the target
	 * @return the next occupied area
	 */
	public WorldArea calculateNextTravellingPoint(Client client, WorldArea travelling, WorldArea target,
												  boolean stopAtMeleeDistance)
	{
		return calculateNextTravellingPoint(client, travelling, target, stopAtMeleeDistance, x -> true);
	}

	/**
	 * Calculates the next area that will be occupied if this area attempts
	 * to move toward it by using the normal NPC travelling pattern.
	 *
	 * @param client the client to calculate with
	 * @param target the target area
	 * @param stopAtMeleeDistance whether to stop at melee distance to the target
	 * @param extraCondition an additional condition to perform when checking valid tiles,
	 * 	                     such as performing a check for un-passable actors
	 * @return the next occupied area
	 */
	public WorldArea calculateNextTravellingPoint(Client client, WorldArea travelling, WorldArea target,
												  boolean stopAtMeleeDistance, Predicate<? super WorldPoint> extraCondition)
	{
		if (travelling.getPlane() != target.getPlane())
		{
			return null;
		}

		if (travelling.intersectsWith(target))
		{
			if (stopAtMeleeDistance)
			{
				// Movement is unpredictable when the NPC and actor stand on top of each other
				return null;
			}
			else
			{
				return travelling;
			}
		}

		int dx = target.getX() - travelling.getX();
		int dy = target.getY() - travelling.getY();
		Point axisDistances = getAxisDistances(travelling, target);
		if (stopAtMeleeDistance && axisDistances.getX() + axisDistances.getY() == 1)
		{
			// NPC is in melee distance of target, so no movement is done
			return travelling;
		}

		LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), travelling.getX(), travelling.getY());
		if (lp == null ||
				lp.getSceneX() + dx < 0 || lp.getSceneX() + dy >= Constants.SCENE_SIZE ||
				lp.getSceneY() + dx < 0 || lp.getSceneY() + dy >= Constants.SCENE_SIZE)
		{
			// NPC is travelling out of the scene, so collision data isn't available
			return null;
		}

		int dxSig = Integer.signum(dx);
		int dySig = Integer.signum(dy);
		if (stopAtMeleeDistance && axisDistances.getX() == 1 && axisDistances.getY() == 1)
		{
			// When it needs to stop at melee distance, it will only attempt
			// to travel along the x axis when it is standing diagonally
			// from the target
			if (travelling.canTravelInDirection(client.getTopLevelWorldView(), dxSig, 0, extraCondition))
			{
				return new WorldArea(travelling.getX() + dxSig, travelling.getY(), travelling.getWidth(), travelling.getHeight(), travelling.getPlane());
			}
		}
		else
		{
			if (travelling.canTravelInDirection(client.getTopLevelWorldView(), dxSig, dySig, extraCondition))
			{
				return new WorldArea(travelling.getX() + dxSig, travelling.getY() + dySig, travelling.getWidth(), travelling.getHeight(), travelling.getPlane());
			}
			else if (dx != 0 && travelling.canTravelInDirection(client.getTopLevelWorldView(), dxSig, 0, extraCondition))
			{
				return new WorldArea(travelling.getX() + dxSig, travelling.getY(), travelling.getWidth(), travelling.getHeight(), travelling.getPlane());
			}
			else if (dy != 0 && Math.max(Math.abs(dx), Math.abs(dy)) > 1 &&
					travelling.canTravelInDirection(client.getTopLevelWorldView(), 0, dy, extraCondition))
			{
				// Note that NPCs don't attempts to travel along the y-axis
				// if the target is <= 1 tile distance away
				return new WorldArea(travelling.getX(), travelling.getY() + dySig, travelling.getWidth(), travelling.getHeight(), travelling.getPlane());
			}
		}

		// The NPC is stuck
		return travelling;
	}

	private Point getAxisDistances(WorldArea first, WorldArea other)
	{
		Point p1 = Reachable.getComparisonPoint(first, other);
		Point p2 = Reachable.getComparisonPoint(other, first);
		return new Point(Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getY() - p2.getY()));
	}

	boolean canMoveToAttack(Client client, WorldPoint target, List<WorldPoint> obstacles)
	{
		if (safeSpotCache.containsKey(target))
		{
			return safeSpotCache.get(target) == 1 || safeSpotCache.get(target) == 2;
		}

		final List<WorldPoint> realObstacles = new ArrayList<>();
		for (WorldPoint obstacle : obstacles)
		{
			if (this.getNpc().getWorldArea().toWorldPointList().contains(obstacle))
			{
				continue;
			}

			realObstacles.add(obstacle);
		}

		final WorldArea targetArea = new WorldArea(target, 1, 1);
		WorldArea currentWorldArea = this.getNpc().getWorldArea();

		int steps = 0;
		while (true)
		{
			// Prevent infinite loop in case of pathfinding failure
			steps++;
			if (steps > 30)
			{
				return false;
			}

			final WorldArea predictedWorldArea = calculateNextTravellingPoint(client, currentWorldArea, targetArea, true, x ->
			{
				for (WorldPoint obstacle : realObstacles)
				{
					if (new WorldArea(x, 1, 1).intersectsWith(new WorldArea(obstacle, 1, 1)))
					{
						return false;
					}
				}
				return true;
			});

			// Will only happen when NPC is underneath player or moving out of scene (but this will never show on overlay)
			if (predictedWorldArea == null)
			{
				safeSpotCache.put(target, 1);
				return true;
			}

			if (predictedWorldArea == currentWorldArea)
			{
				safeSpotCache.put(target, 0);
				return false;
			}

			boolean hasLos = new WorldArea(target, 1, 1).hasLineOfSightTo(client.getTopLevelWorldView(), predictedWorldArea);
			boolean hasRange = this.getType().getDefaultAttack() == Attack.MELEE ? predictedWorldArea.isInMeleeDistance(target)
				: predictedWorldArea.distanceTo(target) <= this.getType().getRange();

			if (hasLos && hasRange)
			{
				safeSpotCache.put(target, 1);
				return true;
			}

			currentWorldArea = predictedWorldArea;
		}
	}

	private boolean couldAttackPrevTick(Client client, WorldPoint lastPlayerLocation)
	{
		return new WorldArea(lastPlayerLocation, 1, 1).hasLineOfSightTo(client.getTopLevelWorldView(), this.getNpc().getWorldArea());
	}

	void gameTick(Client client, WorldPoint lastPlayerLocation, boolean finalPhase, int ticksSinceFinalPhase)
	{
		safeSpotCache.clear();
		this.idleTicks += 1;

		if (ticksTillNextAttack > 0)
		{
			this.ticksTillNextAttack--;
		}
		//Jad animation detection
		if (this.getType() == Type.JAD && getAnimation() != -1 && getAnimation() != this.lastAnimation)
		{
			final Attack currentAttack = Attack.attackFromId(getAnimation());

			if (currentAttack != null && currentAttack != Attack.UNKNOWN)
			{
				this.updateNextAttack(currentAttack, this.getType().getTicksAfterAnimation());
			}
		}

		if (ticksTillNextAttack <= 0)
		{
			switch (this.getType())
			{
				case ZUK:
					if (getAnimation() == TZKAL_ZUK)
					{
						if (finalPhase)
						{
							//if on final phase, wait until at least 3 ticks since the final phase started to set the ticksTilNextAttack
							if (ticksSinceFinalPhase > 3)
							{
								this.updateNextAttack(this.getType().getDefaultAttack(), 7);
							}
						}
						else
						{
							this.updateNextAttack(this.getType().getDefaultAttack(), 10);
						}
					}
					break;
				case JAD:
					if (this.getNextAttack() != Attack.UNKNOWN)
					{
						// Jad's cycle continuous after his animation + attack but there's no animation to alert it
						this.updateNextAttack(this.getType().getDefaultAttack(), 8);
					}
					break;
				case BLOB:
					//RS pathfinding + LOS = hell, so if it can attack you the tick you were on previously, start attack cycle
					if (!this.lastCanAttack && this.couldAttackPrevTick(client, lastPlayerLocation))
					{
						this.updateNextAttack(Attack.UNKNOWN, 3);
					}
					//If there's no animation when coming out of the safespot, the blob is detecting prayer
					else if (!this.lastCanAttack && this.canAttack(client, client.getLocalPlayer().getWorldLocation()))
					{
						this.updateNextAttack(Attack.UNKNOWN, 4);
					}
					//This will activate another attack cycle
					else if (getAnimation() != -1)
					{
						this.updateNextAttack(this.getType().getDefaultAttack(), this.getType().getTicksAfterAnimation());
					}
					break;
				case BAT:
					// Range + LOS check for bat because it suffers from the defense animation bug, also dont activate on "stand" animation
					if (this.canAttack(client, client.getLocalPlayer().getWorldLocation())
						&& getAnimation() != JAL_MEJRAH_STAND && getAnimation() != -1)
					{
						this.updateNextAttack(this.getType().getDefaultAttack(), this.getType().getTicksAfterAnimation());
					}
					break;
				case MELEE:
				case RANGER:
				case MAGE:
					// For the meleer, ranger and mage the attack animation is always prioritized so only check for those
					// Normal attack animation, doesnt suffer from defense animation bug. Activate usual attack cycle
					if (getAnimation() == JAL_IMKOT
						|| getAnimation() == JAL_XIL_RANGE_ATTACK || getAnimation() == JAL_XIL_MELEE_ATTACK
						|| getAnimation() == JAL_ZEK_MAGE_ATTACK || getAnimation() == JAL_ZEK_MELEE_ATTACK)
					{
						this.updateNextAttack(this.getType().getDefaultAttack(), this.getType().getTicksAfterAnimation());
					}
					// Burrow into ground animation for meleer
					else if (getAnimation() == 7600)
					{
						this.updateNextAttack(this.getType().getDefaultAttack(), 12);
					}
					// Respawn enemy animation for mage
					else if (getAnimation() == 7611)
					{
						this.updateNextAttack(this.getType().getDefaultAttack(), 8);
					}
					break;
				default:
					if (getAnimation() != -1)
					{
						// This will activate another attack cycle
						this.updateNextAttack(this.getType().getDefaultAttack(), this.getType().getTicksAfterAnimation());
					}
					break;
			}
		}

		//Blob prayer detection
		if (this.getType() == Type.BLOB && this.getTicksTillNextAttack() == 3
			&& client.getLocalPlayer().getWorldLocation().distanceTo(this.getNpc().getWorldArea()) <= Type.BLOB.getRange())
		{
			Attack nextBlobAttack = Attack.UNKNOWN;
			if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES))
			{
				nextBlobAttack = Attack.MAGIC;
			}
			else if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC))
			{
				nextBlobAttack = Attack.RANGED;
			}

			this.updateNextAttack(nextBlobAttack);
		}

		// This is for jad (jad's animation lasts till after the attack is launched, which fucks up the attack cycle)
		lastAnimation = getAnimation();
		// This is for blob (to check if player just came out of safespot)
		lastCanAttack = this.canAttack(client, client.getLocalPlayer().getWorldLocation());
	}

	private int getAnimation()
	{
		return EthanApiPlugin.getAnimation(this.getNpc());
	}

	@Getter(AccessLevel.PACKAGE)
	enum Attack
	{
		MELEE(Prayer.PROTECT_FROM_MELEE,
			Color.ORANGE,
			Color.RED,
			new int[]{
				JAL_NIB,
				JAL_AK_MELEE_ATTACK,
				JAL_IMKOT,
				JAL_XIL_MELEE_ATTACK,
				JAL_ZEK_MELEE_ATTACK, //TODO: Yt-HurKot attack animation
			}),
		RANGED(Prayer.PROTECT_FROM_MISSILES,
			Color.GREEN,
			new Color(0, 128, 0),
			new int[]{
				JAL_MEJRAH,
				JAL_AK_RANGE_ATTACK,
				JAL_XIL_RANGE_ATTACK,
				JALTOK_JAD_RANGE_ATTACK,
			}),
		MAGIC(Prayer.PROTECT_FROM_MAGIC,
			Color.CYAN,
			Color.BLUE,
			new int[]{
				JAL_AK_MAGIC_ATTACK,
				JAL_ZEK_MAGE_ATTACK,
				JALTOK_JAD_MAGE_ATTACK
			}),
		UNKNOWN(null, Color.WHITE, Color.GRAY, new int[]{});

		private final Prayer prayer;
		private final Color normalColor;
		private final Color criticalColor;
		private final int[] animationIds;

		Attack(Prayer prayer, Color normalColor, Color criticalColor, int[] animationIds)
		{
			this.prayer = prayer;
			this.normalColor = normalColor;
			this.criticalColor = criticalColor;
			this.animationIds = animationIds;
		}

		static Attack attackFromId(int animationId)
		{
			for (Attack attack : Attack.values())
			{
				if (ArrayUtils.contains(attack.getAnimationIds(), animationId))
				{
					return attack;
				}
			}

			return null;
		}
	}

	@Getter(AccessLevel.PACKAGE)
	enum Type
	{
		NIBBLER(new int[]{NpcID.JALNIB}, Attack.MELEE, 4, 99, 100),
		BAT(new int[]{NpcID.JALMEJRAH}, Attack.RANGED, 3, 4, 7),
		BLOB(new int[]{NpcID.JALAK}, Attack.UNKNOWN, 6, 15, 4),
		MELEE(new int[]{NpcID.JALIMKOT}, Attack.MELEE, 4, 1, 3),
		RANGER(new int[]{NpcID.JALXIL, NpcID.JALXIL_7702}, Attack.RANGED, 4, 98, 2),
		MAGE(new int[]{NpcID.JALZEK, NpcID.JALZEK_7703}, Attack.MAGIC, 4, 98, 1),
		JAD(new int[]{NpcID.JALTOKJAD, NpcID.JALTOKJAD_7704, 10623}, Attack.UNKNOWN, 3, 99, 0),
		HEALER_JAD(new int[]{NpcID.YTHURKOT, NpcID.YTHURKOT_7701, NpcID.YTHURKOT_7705}, Attack.MELEE, 4, 1, 6),
		ZUK(new int[]{NpcID.TZKALZUK}, Attack.UNKNOWN, 10, 99, 99),
		HEALER_ZUK(new int[]{NpcID.JALMEJJAK, 10624}, Attack.UNKNOWN, -1, 99, 100);

		private final int[] npcIds;
		private final Attack defaultAttack;
		private final int ticksAfterAnimation;
		private final int range;
		private final int priority;

		Type(int[] npcIds, Attack defaultAttack, int ticksAfterAnimation, int range, int priority)
		{
			this.npcIds = npcIds;
			this.defaultAttack = defaultAttack;
			this.ticksAfterAnimation = ticksAfterAnimation;
			this.range = range;
			this.priority = priority;
		}

		static Type typeFromId(int npcId)
		{
			for (Type type : Type.values())
			{
				if (ArrayUtils.contains(type.getNpcIds(), npcId))
				{
					return type;
				}
			}

			return null;
		}
	}
}

