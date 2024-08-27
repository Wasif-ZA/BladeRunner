// Logger.ino

void logEvent(String event) {
    // Logic to log events, e.g., print to Serial monitor
    Serial.println("Event: " + event);
}

void logError(String error) {
    // Logic to log errors
    Serial.println("Error: " + error);
}
