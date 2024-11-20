package com.lucidplugins.jstfightcaves.QLearning;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.runelite.api.coords.WorldPoint;

public class QLearningAgent {
    private final Map<String, Map<String, Double>> qTable;
    private final double learningRate;
    private final double discountFactor;
    private final double explorationRate;
    private final Random random;

    public QLearningAgent(double learningRate, double discountFactor, double explorationRate) {
        this.qTable = new HashMap<>();
        this.learningRate = learningRate;
        this.discountFactor = discountFactor;
        this.explorationRate = explorationRate;
        this.random = new Random();
    }

    public Action chooseAction(State state) {
        // Exploration: randomly choose an action
        if (random.nextDouble() < explorationRate) {
            return Action.values()[random.nextInt(Action.values().length)];
        }

        // Exploitation: choose the best action based on Q-values
        return getBestAction(state);
    }

    public void update(State state, Action action, State nextState, double reward) {
        String stateKey = getStateKey(state);
        String actionKey = action.name();
        String nextStateKey = getStateKey(nextState);

        // Initialize Q-values if not exist
        qTable.putIfAbsent(stateKey, new HashMap<>());
        qTable.putIfAbsent(nextStateKey, new HashMap<>());

        // Get current Q-value
        double currentQ = qTable.get(stateKey).getOrDefault(actionKey, 0.0);

        // Get max Q-value for next state
        double maxNextQ = getMaxQValue(nextState);

        // Q-learning update formula
        double newQ = currentQ + learningRate * (reward + discountFactor * maxNextQ - currentQ);

        // Update Q-table
        qTable.get(stateKey).put(actionKey, newQ);
    }

    private Action getBestAction(State state) {
        String stateKey = getStateKey(state);
        qTable.putIfAbsent(stateKey, new HashMap<>());
        Map<String, Double> actionValues = qTable.get(stateKey);

        // Initialize with first action if no values exist
        Action bestAction = Action.values()[0];
        double bestValue = actionValues.getOrDefault(bestAction.name(), 0.0);

        // Find action with highest Q-value
        for (Action action : Action.values()) {
            double value = actionValues.getOrDefault(action.name(), 0.0);
            if (value > bestValue) {
                bestValue = value;
                bestAction = action;
            }
        }

        return bestAction;
    }

    private double getMaxQValue(State state) {
        String stateKey = getStateKey(state);
        if (!qTable.containsKey(stateKey)) {
            return 0.0;
        }

        Map<String, Double> actionValues = qTable.get(stateKey);
        if (actionValues.isEmpty()) {
            return 0.0;
        }

        return actionValues.values().stream()
                .mapToDouble(v -> v)
                .max()
                .orElse(0.0);
    }

    private String getStateKey(State state) {
        return String.format("%d_%d_%b_%b_%b",
            state.getPlayerHealth(),
            state.getPrayerPoints(),
            state.isInCombat(),
            state.isUnderAttack(),
            state.isInSafeSpot()
        );
    }

    private double getDistanceToNearestEnemy(State state) {
        if (state.getNearestNPC() == null || state.getLocation() == null) {
            return Double.MAX_VALUE;
        }
        return state.getLocation().distanceTo(state.getNearestNPC().getWorldLocation());
    }

    // Calculate reward based on state transition
    public double calculateReward(State oldState, State newState, Action action) {
        double reward = 0.0;

        // Base rewards/penalties
        if (newState.getPlayerHealth() < oldState.getPlayerHealth()) {
            reward -= 10.0; // Penalty for taking damage
        }
        if (newState.getPrayerPoints() < oldState.getPrayerPoints()) {
            reward -= 5.0; // Small penalty for using prayer
        }

        // Action-specific rewards
        switch (action) {
            case ATTACK_NEAREST:
                if (getDistanceToNearestEnemy(newState) <= 1) {
                    reward += 5.0; // Reward for successful attack position
                }
                break;
            case MOVE_TO_SAFESPOT:
                if (newState.isInSafeSpot() && !oldState.isInSafeSpot()) {
                    reward += 10.0; // Reward for reaching safe spot
                }
                break;
            case PRAYER_FLICK:
                if (!newState.isUnderAttack() && oldState.isUnderAttack()) {
                    reward += 15.0; // Big reward for successful prayer flick
                }
                break;
            case KITE:
                double oldDistance = getDistanceToNearestEnemy(oldState);
                double newDistance = getDistanceToNearestEnemy(newState);
                if (newDistance > oldDistance && !newState.isUnderAttack()) {
                    reward += 12.0; // Reward for successful kiting
                }
                break;
            case STEP_UNDER:
                if (getDistanceToNearestEnemy(newState) == 0 && !newState.isUnderAttack()) {
                    reward += 10.0; // Reward for successful stepping under
                }
                break;
            case WAIT:
                reward -= 1.0; // Small penalty for waiting to encourage action
                break;
        }

        // Additional situational rewards
        if (!newState.isUnderAttack() && newState.isInSafeSpot()) {
            reward += 5.0; // Reward for handling enemies safely
        }

        return reward;
    }
}
