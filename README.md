# BladeRunner Project

This project simulates a Carriage Control System using an ESP32 microcontroller, MCP (Master Control Processor), and CCP (Carriage Control Processor). It includes multiple modules for handling different aspects of the system, such as controlling the carriage, processing commands, managing network connections, and logging events.

## Table of Contents

- [Project Structure](#project-structure)
- [Hardware Requirements](#hardware-requirements)
- [Software Requirements](#software-requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Running the Program](#running-the-program)
- [Troubleshooting](#troubleshooting)
- [Java Integration for JSON Processing](#java-integration-for-json-processing)
- [License](#license)

## Project Structure

The project is split into several files for modularity:

- **Java Components**:
  - `CCP.java`: A Java program that simulates the Carriage Control Processor (CCP) interacting with the MCP and ESP32 devices over a network.
  - `UDPCommunicationHandler.java`: Handles communication between the CCP and MCP via UDP.
  - `StateManager.java`: Manages the state of the Carriage Control Processor.
  - `JSONProcessor.java`: Encodes and decodes messages in JSON format.
  - `CommunicationHandler.java`: Interface defining methods for sending and receiving messages.
  - `MessageListener.java`: Interface for handling incoming messages.

## Hardware Requirements

- **ESP32**: The primary hardware device for carriage control and networking.
- **Sensors**: As required (e.g., ultrasonic sensors or IR photodiodes for alignment).

## Software Requirements

- **Java Development Kit (JDK)** version 11 or later.
- **Visual Studio Code (VS Code)** with Java Extension Pack for Java development.
- **ESP32 development tools** (if programming the ESP32).
- **Wi-Fi network** for communication between the ESP32 and the MCP/CCP.
- **`org.json` library** for JSON processing.

## Installation

### Step 1: Clone the Repository

1. Open your terminal and run the following command to clone the repository:

   ```bash
   git clone https://github.com/Wasif-ZA/BladeRunner.git
   ```

2. Change into the `CCP` directory:

   ```bash
   cd BladeRunner/CCP
   ```

### Step 2: Open the Project in VS Code

1. Launch **Visual Studio Code**.
2. Go to **File > Open Folder** and select the `BladeRunner/CCP` directory.

### Step 3: Install Java Extensions

1. Go to the **Extensions** tab on the left (`Ctrl + Shift + X`).
2. Search for **Java Extension Pack** and install it if not already installed. This pack includes support for Java projects, builds, debugging, etc.

### Step 4: Add the JSON Library to the Classpath

1. Ensure that `json-20220320.jar` (or the appropriate version of the JSON library) is located in the `lib` folder inside your project directory.
2. In the **Java Projects** view on the left side of VS Code, click the `+` icon under **Referenced Libraries**.
3. Select the `lib/json-20220320.jar` file to add it to the classpath.

### Step 5: Customize Network Addresses (if needed)

- Open `CCP.java` and ensure the network addresses (e.g., for the ESP32 and MCP) are configured properly according to your network setup.

### Step 6: Build and Run the Java Program

1. To build and run the program, open `CCP.java` and press `F5` or use the Run button in VS Code.
2. The program will simulate the interaction between the CCP and MCP, processing commands like starting, stopping, and moving the carriage.

## Usage

1. **Initialize the CCP**:
   - The `CCP` class is the entry point. You can instantiate it by passing the appropriate parameters for your MCP and ESP32 addresses.

2. **Customize Commands**:
   - The `processMCPAction()` method handles commands such as `STOPC`, `FSLOWC`, `FFASTC`, and forwards them to the ESP32 for execution.

3. **Monitor Outputs**:
   - You can monitor the status of the CCP and its interactions with the MCP and ESP32 in the console output.

### Example Command Processing

When a message is received from the MCP, such as a `STOPC` command, the following actions take place:

```java
case "STOPC":
    stopAndCloseDoors();  // Stop the carriage and close its doors.
    sendStatus("STOPC");  // Send status back to the MCP.
    break;
```

Similarly, messages are sent to the ESP32 for hardware-level control of the carriage.

## Running the Program

### Example Workflow

1. The CCP connects to the MCP by sending an initialization message.
2. The MCP sends a command to move the carriage forward slowly (`FSLOWC`).
3. The CCP processes the command and forwards it to the ESP32.
4. Based on the ESP32's feedback, the CCP adjusts its status and sends updates back to the MCP.

## Troubleshooting

- **No JSON Recognition**: Ensure that the `json-20220320.jar` is properly added to the classpath in VS Code. You can verify this by checking the **Java Projects** section.
- **Network Issues**: Make sure your network configuration (IP addresses, ports) is correct for the ESP32 and MCP. You might need to adjust firewall settings if using multiple devices on a Wi-Fi network.
- **Build Errors**: Verify that the **Java SDK** is set correctly in VS Code by going to **Settings > Java: Configuration**.

## Java Integration for JSON Processing

To verify that JSON processing works correctly in your project:

1. Create a new Java class named `TestJSON.java` in the `CCP` package:

   ```java
   package CCP;

   import org.json.JSONObject;

   public class TestJSON {
       public static void main(String[] args) {
           JSONObject jsonObj = new JSONObject();
           jsonObj.put("name", "Blade Runner");
           jsonObj.put("status", "active");
           System.out.println(jsonObj.toString());
       }
   }
   ```

2. Run the program to ensure the JSON object is printed correctly in the terminal.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
