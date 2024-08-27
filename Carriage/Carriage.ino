class Carriage {
private:
    bool isMoving;
    double speed;
    int LEDState;
    String carriageName;
    int sensorOne;
    int sensorTwo;
    bool isSomethingAhead;
    String cpuID;

public:
    void Accelerate() {
        // Increase speed logic
        speed += 1.0;
    }

    void Decelerate() {
        // Decrease speed logic
        speed -= 1.0;
    }

    bool OpenDoor() {
        // Logic to open the door
        return true; // Assuming success
    }

    void init() {
        // Initialization logic
        isMoving = false;
        speed = 0.0;
        LEDState = LOW;
        isSomethingAhead = false;
    }

    // Additional methods for setting/getting the properties can be added here
};
