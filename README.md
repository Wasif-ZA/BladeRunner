# BladeRunner Project

This project simulates a carriage control system using an Arduino, part of the BladeRunner project. It includes multiple modules for handling different aspects of the system, such as controlling the carriage, processing commands, managing Wi-Fi connections, logging events, and running tests.

## Table of Contents

- [Project Structure](#project-structure)
- [Hardware Requirements](#hardware-requirements)
- [Software Requirements](#software-requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Running the Program](#running-the-program)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Project Structure

The project is split into several files for modularity:

- `main.ino`: The main Arduino sketch that initializes and runs the project.
- `Carriage.ino`: Handles the carriage's movement, sensors, and state.
- `Command.ino`: Processes input commands and updates the carriage's state.
- `Server.ino`: Manages Wi-Fi connectivity and handles incoming requests.
- `Logger.ino`: Logs events and errors to the Serial monitor.
- `Test.ino`: Provides test functionalities for the system.

## Hardware Requirements

- Arduino board (e.g., Arduino Uno, Mega, or similar)
- Wi-Fi module (e.g., ESP8266 or ESP32)
- LED for status indication (connected to pin 13)
- Sensors as required (for example, ultrasonic sensors)
- Breadboard and jumper wires

## Software Requirements

- Arduino IDE (version 1.8.13 or later)
- WiFi library (for ESP8266/ESP32)
- Serial monitor for debugging

## Installation

1. **Clone or Download the Project:**
   - Clone this repository using Git:
     ```bash
     git clone https://github.com/Wasif-ZA/BladeRunner.git
     ```
   - Or download the project as a ZIP file and extract it.

2. **Open the Project in Arduino IDE:**
   - Launch Arduino IDE.
   - Go to `File > Open` and navigate to the project folder.
   - Select the `main.ino` file to open the entire project.

3. **Connect Your Arduino:**
   - Plug your Arduino board into your computer via USB.
   - Select the correct board and port from `Tools > Board` and `Tools > Port`.

4. **Install Required Libraries:**
   - Make sure you have the `WiFi` library installed.
   - Go to `Sketch > Include Library > Manage Libraries...` and search for "WiFi". Install it if it’s not already installed.

## Usage

1. **Customize Wi-Fi Credentials:**
   - Open the `Server.ino` file.
   - Replace `yourSSID` and `yourPassword` with your Wi-Fi network's SSID and password.

2. **Upload the Code:**
   - Click the Upload button in the Arduino IDE to compile and upload the code to your Arduino board.

3. **Monitor Serial Output:**
   - Open the Serial Monitor (`Tools > Serial Monitor`) to view logs and debug information. Set the baud rate to `9600`.

## Running the Program

Once the code is uploaded to the Arduino, the program will:

1. Initialize the carriage system, setting up its state, speed, and sensors.
2. Attempt to connect to the specified Wi-Fi network.
3. Start a server that listens for HTTP requests.
4. Process commands and log events or errors as they occur.
5. Test the system using predefined test cases.

### Accessing the Server

- Once connected to Wi-Fi, the Arduino will display its IP address in the Serial Monitor.
- Open a web browser and enter the IP address in the address bar to access the Arduino's server.
- You should see a simple HTML page with a message like "Hello from your Arduino Server!"

## Troubleshooting

- **No Wi-Fi Connection:** Ensure that your SSID and password are correct. Check that your Wi-Fi module is correctly connected to the Arduino.
- **No Serial Output:** Ensure that the correct baud rate (9600) is selected in the Serial Monitor. Check the USB connection.
- **Compilation Errors:** Verify that all `.ino` files are correctly placed in the project folder. Ensure that the WiFi library is installed.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

