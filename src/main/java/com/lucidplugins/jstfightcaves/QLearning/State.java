package com.lucidplugins.jstfightcaves.QLearning;

import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

public class State {
    private final int playerHealth;
    private final int prayerPoints;
    private final boolean isUnderAttack;
    private final int nearbyEnemyCount;
    private final double distanceToNearestEnemy;
    private final boolean isInSafeSpot;
    private final boolean isMoving;

    public State(Player player, NPC nearestEnemy, boolean underAttack, int enemyCount, boolean inSafeSpot, boolean moving) {
        this.playerHealth = player != null ? player.getHealthRatio() : 0;
        this.prayerPoints = player != null ? player.getPrayerPoints() : 0;
        this.isUnderAttack = underAttack;
        this.nearbyEnemyCount = enemyCount;
        this.distanceToNearestEnemy = nearestEnemy != null ? 
            player.getWorldLocation().distanceTo(nearestEnemy.getWorldLocation()) : 999;
        this.isInSafeSpot = inSafeSpot;
        this.isMoving = moving;
    }

    public String getKey() {
        // Discretize continuous values into buckets for Q-table lookup
        int healthBucket = playerHealth / 20; // 0-5 buckets
        int prayerBucket = prayerPoints / 20; // 0-5 buckets
        int distanceBucket = (int) Math.min(distanceToNearestEnemy / 2, 5); // 0-5 buckets
        int enemyBucket = Math.min(nearbyEnemyCount, 3); // 0-3 buckets

        return String.format("%d_%d_%b_%d_%d_%b_%b",
            healthBucket, prayerBucket, isUnderAttack, enemyBucket,
            distanceBucket, isInSafeSpot, isMoving);
    }

    // Getters
    public int getPlayerHealth() { return playerHealth; }
    public int getPrayerPoints() { return prayerPoints; }
    public boolean isUnderAttack() { return isUnderAttack; }
    public int getNearbyEnemyCount() { return nearbyEnemyCount; }
    public double getDistanceToNearestEnemy() { return distanceToNearestEnemy; }
    public boolean isInSafeSpot() { return isInSafeSpot; }
    public boolean isMoving() { return isMoving; }
}
