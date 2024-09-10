// Command.ino

String currentState;

void processInput(String input) {
    // Logic to process input commands
    currentState = input;
}

String getState() {
    return currentState;
}

void updateState() {
    // Update state logic here
}

String DoorStatus(String status) {
    return status;
}

String CarriageStatus(String status) {
    return status;
}

String Speed(int speed) {
    return String(speed);
}
