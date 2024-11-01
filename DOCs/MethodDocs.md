# CCP (Carriage Control Program) Documentation

## 1. **Overview of the CCP**

The CCP is responsible for controlling the movement and behavior of the Blade Runner (BR). It communicates with the MCP (Master Control Program) over a network using the UDP protocol. The CCP listens for commands from the MCP, processes them, and adjusts the BR’s actions accordingly.

### **CCP Responsibilities:**
- Managing communication with the MCP.
- Executing commands like stopping, moving, and adjusting the BR’s speed.
- Simulating sensors like a photodiode for alignment and an LED for status indication.
- Keeping track of the BR's state (e.g., stopped, moving at full speed).

---

## 2. **Core Components of the CCP**

### **a) Attributes:**
- **bladeRunnerId**: The unique identifier for the BR (e.g., "BR01").
- **stateManager**: Manages the current state of the BR, including its movement, whether it's stopped, moving slowly, or at full speed.
- **commHandler**: Handles all communication between the BR and the MCP. It sends and receives messages using UDP.
- **isCarriageInFront, isCarriageBehind**: Boolean flags to track the position of other BRs relative to this one.
- **isAlignedWithPhotodiode**: Simulates alignment with an infrared (IR) photodiode, which could be used for checkpoints or alignment markers on the track.
- **isLEDFlashing**: A flag to simulate whether the BR's LED is flashing, used to indicate a status or an issue (e.g., during disconnection).

### **b) Communication System (UDPCommunicationHandler):**
The **UDPCommunicationHandler** is responsible for actual communication with the MCP using the UDP protocol.

- **sendMessage(String message)**: Sends a message in JSON format to the MCP.
- **listenForMessages()**: Continuously listens for messages from the MCP in a separate thread. Once a message is received, the `onMessageReceived` callback in `CCP` processes it.

### **c) State Management (StateManager):**
The **StateManager** tracks the CCP’s state using an enumerated type `CCPState`, which includes:

- **STARTED**: The initial state when the BR starts.
- **CONNECTED**: The BR is connected to the MCP.
- **STOPPED**: The BR is stopped.
- **FULL_SPEED**: The BR is moving at full speed.
- **SLOW_FORWARD/SLOW_BACKWARD**: The BR is moving slowly in either direction.
- **MAINTAINING_PACE**: The BR is moving at a regulated pace, perhaps due to other carriages in front or behind.

State transitions occur when actions are received from the MCP or based on sensor checks.

### **d) Simulated Sensors:**
- **Photodiode Alignment**: Simulates checking whether the BR is aligned with an IR photodiode, which could indicate a checkpoint or a specific location on the track.
- **LED Flashing**: Simulates a flashing LED to indicate a particular status (e.g., during disconnection).

### **e) Communication with MCP:**
The CCP sends and receives messages in JSON format to interact with the MCP:
- **Sending messages**: The `sendMessage()` method constructs a JSON object using the `JSONProcessor.encodeMessage()` function and sends it to the MCP.
- **Receiving messages**: When a message is received, the `onMessageReceived()` method decodes the message using `JSONProcessor.decodeMessage()` and further processes it by calling `processMCPAction()`.

---

## 3. **Key Methods and Functionalities**

### **a) `connect()`**
This method connects the CCP to the MCP. When called, it sends a message with the `"CCIN"` action (Carriage Control In) to the MCP to initiate the connection. If successful, the CCP transitions from the `STARTED` to `CONNECTED` state.

### **b) `processMCPAction()`**
This method processes actions received from the MCP. Each action corresponds to a specific behavior for the BR:

- **STOPC**: Stop and close the doors.
- **STOPO**: Stop and open the doors.
- **FSLOWC**: Move forward slowly, or stop and close the doors if aligned with the photodiode.
- **FFASTC**: Move forward fast.
- **RSLOWC**: Move backward slowly, or stop if aligned with the photodiode.
- **DISCONNECT**: Flash the BR's LED to indicate disconnection.

### **c) Movement and Control Methods**
Several methods control the BR's movement:

- **stopAndCloseDoors()**: Stops the BR and closes its doors.
- **stopAndOpenDoors()**: Stops the BR and opens its doors.
- **moveForwardSlowly()**: Moves the BR forward at a slow pace.
- **moveForwardFast()**: Moves the BR forward at full speed.
- **moveBackwardSlowly()**: Moves the BR backward slowly.

### **d) `forwardCommandToESP()`**
This method forwards commands received from the MCP to the ESP32, which may control hardware sensors or motors.

- **Why it was implemented**: Many actions from the MCP, such as movement commands, need to be forwarded to the ESP32 to control the physical hardware.
- **What it does**: Encodes the command in JSON format and sends it to the ESP32 over UDP.

### **e) LED Control**
The BR uses an LED to indicate its status visually.

- **`flashLEDPattern(int ledIndex, int flashCount)`**: Flashes the specified LED a certain number of times.
- **`turnOnLED(int ledIndex)`**: Turns on a specific LED without flashing.
- **`stopLEDFlashing()`**: Stops any ongoing LED flashing.

### **f) Communication with ESP32**
In addition to communicating with the MCP, the CCP also communicates with the ESP32:

- **`sendMessageToESP()`**: Sends commands to the ESP32.
- **`waitForAcknowledgment()`**: Waits for an acknowledgment from the ESP32 after sending a command.
- **`onMessageReceivedFromESP32()`**: Processes messages received from the ESP32, such as hazard detection alerts.

### **g) Hazard Detection**
The ESP32 can send hazard detection messages to the CCP. Upon receiving such a message, the CCP takes appropriate action (e.g., stopping the BR).

- **`onHazardDetected()`**: Stops the BR and sends a status update to the MCP when a hazard is detected by the ESP32.

---

## 4. **Change Log and Justifications**

### **1. Addition of ESP32 Command Forwarding**
- **Rationale**: To ensure hardware control and coordination between the MCP and the ESP32, commands received from the MCP must also be forwarded to the ESP32.
- **Impact**: Allows for real-time communication with the ESP32, enabling the control of physical hardware such as motors and sensors.

### **2. Acknowledgment Mechanism with ESP32**
- **Rationale**: To ensure reliability in communication, the CCP now waits for an acknowledgment from the ESP32 after sending commands.
- **Impact**: Improves communication reliability by ensuring commands are received and acted upon by the ESP32.

### **3. LED Control Enhancements**
- **Rationale**: The original code lacked control over how long LEDs flashed. The updated implementation introduces a flash counter and proper stop mechanisms.
- **Impact**: Provides more precise control over LED behavior, which can be used to indicate different statuses or issues.

### **4. State Management and Communication Improvements**
- **Rationale**: The BR’s state management system was enhanced to respond appropriately to commands from the MCP and ESP32. 
- **Impact**: Ensures proper state transitions and accurate status reporting to the MCP and ESP32.