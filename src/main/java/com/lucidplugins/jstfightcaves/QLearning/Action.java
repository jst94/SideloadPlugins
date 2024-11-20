package com.lucidplugins.jstfightcaves.QLearning;

public enum Action {
    ATTACK_NEAREST,      // Attack the nearest enemy
    MOVE_TO_SAFESPOT,   // Move to a safe spot
    HEAL,               // Eat food or drink potions
    PRAYER_FLICK,       // Change prayers
    KITE,              // Kite an enemy
    STEP_UNDER,        // Step under an enemy
    WAIT;              // Do nothing this tick

    public static Action[] getAllActions() {
        return Action.values();
    }

    public String getKey() {
        return this.name();
    }
}
