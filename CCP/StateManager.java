package CCP;

public class StateManager {

    public enum CCPState {
        STARTED, CONNECTED, STOPPED, FULL_SPEED,
        MAINTAINING_PACE, SLOW_FORWARD, SLOW_BACKWARD
    }

    private CCPState currentState;

    public StateManager() {
        this.currentState = CCPState.STARTED;
    }

    public void updateState(CCPState newState) {
        this.currentState = newState;
        System.out.println("State updated to: " + newState);
    }

    public CCPState getCurrentState() {
        return currentState;
    }
}
