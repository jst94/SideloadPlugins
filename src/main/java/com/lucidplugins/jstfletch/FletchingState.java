package com.lucidplugins.jstfletch;

public class FletchingState {
    public enum State {
        BANKING,
        FLETCHING,
        BUYING_SUPPLIES
    }

    private State currentState;

    public FletchingState() {
        this.currentState = State.BANKING;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }
}
