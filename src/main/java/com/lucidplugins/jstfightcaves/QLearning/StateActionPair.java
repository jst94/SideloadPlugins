package com.lucidplugins.jstfightcaves.QLearning;

import lombok.Getter;
import java.util.Objects;

@Getter
public class StateActionPair {
    private final State state;
    private final Action action;

    public StateActionPair(State state, Action action) {
        this.state = state;
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateActionPair that = (StateActionPair) o;
        return Objects.equals(state, that.state) && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, action);
    }
}
