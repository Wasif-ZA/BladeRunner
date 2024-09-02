#include <WiFi.h>  // Include the WiFi library

char ssid[] = "yourSSID";      // Replace with your network SSID
char pass[] = "yourPassword";  // Replace with your network password
int WiFiport = 80;             // Port for the server
WiFiServer server(WiFiport);   // Create a server object

int ledPin = 13;  // Pin for status LED
int status = WL_IDLE_STATUS;  // Initial WiFi status

void initWiFi() {
    Serial.print("Connecting to ");
    Serial.println(ssid);

    // Start WiFi connection
    WiFi.begin(ssid, pass);

    // Wait for connection
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }

    // Once connected, print IP address
    Serial.println("");
    Serial.println("WiFi connected.");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());

    // Start the server
    server.begin();
    Serial.println("Server started");

    // Blink the LED to show successful connection
    digitalWrite(ledPin, HIGH);
    delay(1000);
    digitalWrite(ledPin, LOW);
}

bool connectionStatus() {
    return WiFi.status() == WL_CONNECTED;
}

void receiveRequest() {
    WiFiClient client = server.available();  // Listen for incoming clients

    if (client) {                             // If a new client connects,
        Serial.println("New Client.");        // print a message out in the serial port
        String currentLine = "";              // make a String to hold incoming data from the client
        while (client.connected()) {          // loop while the client's connected
            if (client.available()) {         // if there's bytes to read from the client,
                char c = client.read();       // read a byte, then
                Serial.write(c);              // print it out the serial monitor
                if (c == '\n') {              // if the byte is a newline character

                    // If the current line is blank, you got two newline characters in a row.
                    // that's the end of the client HTTP request, so send a response:
                    if (currentLine.length() == 0) {
                        sendResponse(client);
                        break;
                    } else { // if you got a newline, then clear currentLine
                        currentLine = "";
                    }
                } else if (c != '\r') {  // if you got anything else but a carriage return character,
                    currentLine += c;    // add it to the end of the currentLine
                }
            }
        }
        // close the connection:
        client.stop();
        Serial.println("Client Disconnected.");
    }
}

void sendResponse(WiFiClient client) {
    // Send the standard HTTP response
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/html");
    client.println("Connection: close");  // the connection will be closed after completion of the response
    client.println();

    // Send the HTML page content
    client.println("<!DOCTYPE HTML>");
    client.println("<html>");
    client.println("<h1>Hello from your Arduino Server!</h1>");
    client.println("</html>");
}

