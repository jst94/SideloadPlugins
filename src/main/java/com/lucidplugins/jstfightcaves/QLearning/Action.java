package com.lucidplugins.jstfightcaves.QLearning;

import java.util.Arrays;
import java.util.List;

public enum Action {
    ATTACK_NEAREST,
    MOVE_TO_SAFESPOT,
    PRAYER_FLICK,
    KITE,
    STEP_UNDER,
    WAIT;

    public static List<Action> getAllActions() {
        return Arrays.asList(Action.values());
    }
}
