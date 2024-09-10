#include <WiFi.h>
#include <WiFiUdp.h>
#include <ESP32Servo.h>
#define LED_BUILTIN 2
const int servoPin1 = 12;
const int servoPin2 = 14;
#define doorPin 27

Servo doorServo;


const char* ssid = "ESP32Test";
const char* password = "password";

WiFiUDP udp;
const int localUdpPort = 1234;
char incomingPacket[255];

IPAddress local_IP(192, 168, 1, 100);
IPAddress gateway(192, 168, 1, 10);
IPAddress subnet(255, 255, 255, 0);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
  //pinMode(servoPin1, OUTPUT);
  //pinMode(servoPin2, OUTPUT);
  pinMode(18, OUTPUT);
  pinMode(19, OUTPUT);
  analogWrite(servoPin1, 0);
  analogWrite(servoPin2, 0);

  delay(2000);

  Serial.println("Setting soft-AP configuration ... ");
  WiFi.disconnect();
  WiFi.mode(WIFI_AP);
  Serial.println(WiFi.softAPConfig(local_IP, gateway, subnet) ? "Ready" : "Failed!");
  Serial.println("Setting soft-AP ... ");
  boolean result = WiFi.softAP(ssid, password);
  if(result){
    Serial.println("Ready");
    Serial.println(String("Soft-AP IP address = ") + WiFi.softAPIP().toString());
    Serial.println(String("MAC address = ") + WiFi.softAPmacAddress().c_str());
  }else{
    Serial.println("Failed!");
  }
  Serial.println("Setup End");
  doorServo.attach(servoPin1);
  doorServo.write(90);
  udp.begin(localUdpPort);
  Serial.printf("Now listening on UDP port %d\n", localUdpPort);
}

void loop() {
  digitalWrite(18, HIGH);
  //analogWrite(servoPin1, 0);
  //analogWrite(servoPin2, 0);

  int packetSize = udp.parsePacket();
  if (packetSize) {
    // Receive incoming UDP packet
    int len = udp.read(incomingPacket, 255);
    if (len > 0) {
      // For raw byte data, you don't need to null-terminate
      // But for safety, we'll null-terminate it anyway
      incomingPacket[len] = 0;  
    }
    
    // Read the first byte of the packet
    if (len > 0) {
      char receivedByte = incomingPacket[0];
      Serial.printf("Received byte: %d\n", receivedByte);

      // Check the received byte and control the LED
      if (receivedByte == '1') { //forward engine
        digitalWrite(LED_BUILTIN, HIGH);  // Turn on the LED
        Serial.println("LED ON, forward");
        
        analogWrite(servoPin1, 255);
        analogWrite(servoPin2, 1);


        delay(10);
        
      } else if (receivedByte == '2') { //backward engine
        digitalWrite(LED_BUILTIN, LOW);   // Turn off the LED
        Serial.println("LED OFF, backward");
        //doorServo.attach(servoPin1);
        //doorServo.write(95);
        //delay(2000);
        //doorServo.detach();
        
        analogWrite(servoPin1, 1);
        analogWrite(servoPin2, 255);


        delay(10);
        
      } else if (receivedByte == '0') { //stop engine
        
        analogWrite(servoPin1, 0);
        analogWrite(servoPin2, 0);

        
        Serial.println("stop motor");
      } else if (receivedByte == '3') { //operate door, timing test needed
        doorServo.attach(doorPin);
        doorServo.write(80);
        delay(1000);
        doorServo.detach();

        delay(1000);

        doorServo.attach(doorPin);
        doorServo.write(100);
        delay(1000);
        doorServo.detach();
        
        Serial.println("operate servo");
      } 
  }
}
}
