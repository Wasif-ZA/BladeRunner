#include "Carriage.ino"
#include "Command.ino"
#include "Server.ino"
#include "Logger.ino"
#include "Test.ino"

void setup() {
    Serial.begin(9600);

    // Initialize Carriage
    initCarriage();
    Accelerate();
    Decelerate();
    logEvent("Carriage Initialized");

    // Initialize Wi-Fi server
    initWiFi();

    // Log test events
    TestInit();
    TestConnectionStatus();
}

void loop() {
    processInput("Start");
    updateState();
    String state = getState();
    logEvent("Command State: " + state);

    if (!connectionStatus()) {
        logError("Connection Lost");
    }
}
