#include <WiFi.h>
#include <WiFiUdp.h>
#include <ArduinoJson.h>
#include <ESP32Servo.h>
#include <RunningAverage.h>

// WiFi credentials
const char* ssid = "ENGG2K3K";
const char* password = "";

//const int servoPin1 = 16;
//const int servoPin2 = 17;

// Static IP settings
IPAddress local_IP(10, 20, 30, 112);   // Set your desired static IP address
IPAddress gateway(10, 20, 30, 250);      // Set your network gateway
IPAddress subnet(255, 255, 255, 0);     // Subnet mask

// UDP server
WiFiUDP udp;
unsigned int localUdpPort = 3012; // Use the same port as CCP

// Buffer to hold incoming packets
char incomingPacket[512];  // Buffer size should be big enough to hold your JSON data
int packetSize;

int pin_redLED= 26;
int pin_yellowLED= 27;
int pin_greenLED= 32;
int pin_blueLED= 33;
int pin_trigU2= 19;
int pin_trigU3= 21;
int pin_echoU2= 22;
int pin_echoU3= 23;
int pin_sigU5= 25;
int pin_analogR1= 34; //analog input
int pin_analogR2= 35; //analog input
int pin_driver1A= 12; //drive motor
int pin_driver2A= 13; //drive motor
int pin_driver3A= 14; //door
int pin_driver4A= 15; //door
int pin_driver12EN= 16; //pwm output
int pin_driver34EN= 17; //pwm output
float duration_us, distance_cm;
unsigned long last_time = 0;
int pwm_12pins = 255;
int pwm_34pins = 20;

RunningAverage current_reading(100);

void setup() {
    Serial.begin(115200);

    // Configure static IP address
    if (!WiFi.config(local_IP, gateway, subnet)) {
        Serial.println("Failed to configure Static IP");
    }
    pinMode(pin_redLED, OUTPUT);
    pinMode(pin_yellowLED, OUTPUT);
    pinMode(pin_greenLED, OUTPUT);
    pinMode(pin_blueLED, OUTPUT);
    pinMode(pin_trigU2, OUTPUT);
    pinMode(pin_trigU3, OUTPUT);
    pinMode(pin_echoU2, INPUT);
    pinMode(pin_echoU3, INPUT);
    pinMode(pin_sigU5, INPUT);
    pinMode(pin_driver1A, OUTPUT);
    pinMode(pin_driver2A, OUTPUT);
    pinMode(pin_driver3A, OUTPUT);
    pinMode(pin_driver4A, OUTPUT);
    pinMode(pin_driver12EN, OUTPUT);
    pinMode(pin_driver34EN, OUTPUT);
    current_reading.clear();

    //pinMode(servoPin1, OUTPUT);
    //pinMode(servoPin2, OUTPUT);
    //pinMode(18, OUTPUT);
    //pinMode(19, OUTPUT);
    //analogWrite(servoPin1, 0);
    //analogWrite(servoPin2, 0);
    // Connect to WiFi
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.println("Connecting to WiFi...");
    }

    Serial.println("Connected to WiFi");

    // Print the assigned IP address
    Serial.print("ESP32 IP address: ");
    Serial.println(WiFi.localIP());

    // Start UDP
    udp.begin(localUdpPort);
    Serial.printf("UDP server started on port %d\n", localUdpPort);
}

void loop() {
    packetSize = udp.parsePacket();
    if (packetSize) {
        Serial.printf("Received %d bytes from %s, port %d\n", packetSize, udp.remoteIP().toString().c_str(), udp.remotePort());
        
        // Read the packet into the buffer
        int len = udp.read(incomingPacket, sizeof(incomingPacket) - 1);
        if (len > 0) {
            incomingPacket[len] = '\0';  // Null-terminate the string
        }
        
        // Print the entire received data for debugging
        Serial.print("Received full data: ");
        Serial.println(incomingPacket);

        // Now process the entire incoming JSON data
        String incomingData = String(incomingPacket);  // Convert char array to String

        if (incomingData.length() > 0) {
            // Trim any leading/trailing whitespace characters (optional)
            incomingData.trim(); 
            processCommand(incomingData);
        } else {
            Serial.println("No data received");
        }
    }
}

// Function to process received commands
void processCommand(String command) {
    Serial.print("Received command: ");
    Serial.println(command);

    // Parse the JSON command
    DynamicJsonDocument doc(256);
    DeserializationError error = deserializeJson(doc, command);

    if (error) {
        Serial.println("Failed to parse command");
        return;
    }

    // Extract action from the JSON command
    const char* action = doc["message"];

    if (!action) {
        Serial.println("No action found in the JSON message");
        return;
    }
    
    // Perform actions based on the command received
    if (strcmp(action, "STOPC") == 0) {
        stopCarriage();
    } else if (strcmp(action, "STOPO") == 0) {
        openDoors();
    } else if (strcmp(action, "FSLOWC") == 0) {
        moveForwardSlow();
    } else if (strcmp(action, "FFASTC") == 0) {
        moveForwardFast();
    } else if (strcmp(action, "RSLOWC") == 0) {
        moveBackwardSlow();
    } else if (strcmp(action, "DISCONNECT") == 0) {
        handleDisconnect();
    } else {
        Serial.println("Unknown action");
    }
}

// Define the actions
void stopCarriage() {
    Serial.println("Carriage stopping and closing doors...");
    digitalWrite(pin_driver12EN, HIGH);
    digitalWrite(pin_driver34EN, LOW);

    analogWrite(pin_driver1A, 0);
    analogWrite(pin_driver2A, 0);

    digitalWrite(pin_driver12EN, LOW);
    digitalWrite(pin_driver34EN, HIGH);

    for(int dutyCycle = 0; dutyCycle <= pwm_34pins; dutyCycle++){
      // changing the LED brightness with PWM
      analogWrite(pin_driver3A, 0);
      analogWrite(pin_driver4A, dutyCycle);
      delay(20);
    }
    Serial.println("Door close accel");

    for(int i=0;  i<= 300; i++){
      // changing the LED brightness with PWM
      analogWrite(pin_driver3A, 0);
      analogWrite(pin_driver4A, pwm_34pins);
      delay(10);
    }
    Serial.println("Door maintain closing speed");

    // decrease speed clockwise
    for(int dutyCycle = pwm_34pins; dutyCycle >= 0; dutyCycle--){
      // changing the LED brightness with PWM
      analogWrite(pin_driver3A, 0);
      analogWrite(pin_driver4A, dutyCycle);
      delay(20);
    }
    Serial.println("Door close deccel");
}

void openDoors() {
    Serial.println("Carriage stopping and opening doors...");
    digitalWrite(pin_driver12EN, HIGH);
    digitalWrite(pin_driver34EN, LOW);

    analogWrite(pin_driver1A, 0);
    analogWrite(pin_driver2A, 0);

    digitalWrite(pin_driver12EN, LOW);
    digitalWrite(pin_driver34EN, HIGH);

    for(int dutyCycle = 0; dutyCycle <= pwm_34pins; dutyCycle++){
      // changing the LED brightness with PWM
      analogWrite(pin_driver3A, dutyCycle);
      analogWrite(pin_driver4A, 0);
      delay(10);
    }
    Serial.println("Door open accel");

    for(int i=0;  i<= 300; i++){
      // changing the LED brightness with PWM
      analogWrite(pin_driver3A, pwm_34pins);
      analogWrite(pin_driver4A, 0);
      delay(10);
    }
    Serial.println("Door maintain opening speed");

    // decrease speed clockwise
    for(int dutyCycle = pwm_34pins; dutyCycle >= 0; dutyCycle--){
      // changing the LED brightness with PWM
      analogWrite(pin_driver3A, dutyCycle);
      analogWrite(pin_driver4A, 0);
      delay(10);
    }
    Serial.println("Door open deccel");
  }

void moveForwardSlow() {
    Serial.println("Carriage moving forward slowly...");
    digitalWrite(pin_driver12EN, HIGH);
    digitalWrite(pin_driver34EN, LOW);
    analogWrite(pin_driver1A, pwm_34pins);
    analogWrite(pin_driver2A, 0);

    //delay(10);
}

void moveForwardFast() {
    Serial.println("Carriage moving forward fast...");
    digitalWrite(pin_driver12EN, HIGH);
    digitalWrite(pin_driver34EN, LOW);
    for(int dutyCycle = 0; dutyCycle <= pwm_12pins; dutyCycle++){
    // changing the LED brightness with PWM
      analogWrite(pin_driver1A, dutyCycle);
      analogWrite(pin_driver2A, 0);
      delay(10);
    }
    analogWrite(pin_driver1A, pwm_12pins);
    analogWrite(pin_driver2A, 0);
}

void moveBackwardSlow() {
    Serial.println("Carriage moving backward slowly...");
    digitalWrite(pin_driver12EN, HIGH);
    digitalWrite(pin_driver34EN, LOW);
    analogWrite(pin_driver1A, 0);
    analogWrite(pin_driver2A, pwm_34pins);
}

void handleDisconnect() {
    Serial.println("Handling disconnect...");
}