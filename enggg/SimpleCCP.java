import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

public class SimpleCCP {

    private String bladeRunnerId;
    private StateManager stateManager;
    private CommunicationHandler commHandler;
    private boolean isCarriageInFront;
    private boolean isCarriageBehind;
    private boolean isAlignedWithPhotodiode; // Simulates alignment with IR photodiode
    private boolean isLEDFlashing; // Simulates LED flashing state
    private InetAddress espAddress;  // Set the correct IP address for ESP32
    private int espPort;  // Set the correct port for ESP32
    private int sequenceNumbers;  // Sequence number for messages
    private boolean areDoorsOpen = false; // Track if doors are open

    public SimpleCCP(String bladeRunnerId, String mcpAddress, int mcpPort, String ccpAddress, int ccpPort, String espIp, int espPort, int sequenceNumber) throws Exception {
        this.bladeRunnerId = bladeRunnerId;
        this.stateManager = new StateManager();
        this.commHandler = new UDPCommunicationHandler(mcpAddress, mcpPort, ccpAddress, ccpPort, this::onMessageReceived);
        this.sequenceNumbers = sequenceNumber;
        this.espAddress = InetAddress.getByName(espIp);
        this.espPort = espPort;

        // Initially, CCP is in the STARTED state
        this.stateManager.updateState(StateManager.CCPState.STARTED);
        this.isAlignedWithPhotodiode = false; // Initialize photodiode alignment
        this.isLEDFlashing = false; // LED is not flashing initially
    }

    // Method to connect to MCP
    public void connect() throws Exception {
        if (stateManager.getCurrentState() == StateManager.CCPState.STARTED) {
            String message = JSONProcessor.encodeMessage("ccp", "CCIN", bladeRunnerId);
            commHandler.sendMessage(message);
            stateManager.updateState(StateManager.CCPState.CONNECTED);
        } else {
            throw new IllegalStateException("Cannot connect in the current state: " + stateManager.getCurrentState());
        }
    }

    // Callback for receiving messages
    private void onMessageReceived(String message) {
        System.out.println("Message received by " + bladeRunnerId + ":");
        System.out.println(JSONProcessor.decodeMessage(message));

        // Process the received action from MCP
        try {
            processMCPAction(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update the state based on received messages
        updateCarriageDetection(message);
        checkCarriagePositions(); // Check for nearby carriages after receiving a message
    }

    // Method to process MCP actions
    private void processMCPAction(String message) throws Exception {
        JSONObject jsonMessage = new JSONObject(message);
        String action = jsonMessage.getString("action");

        // Forward the command to ESP32 first
        forwardCommandToESP(action, jsonMessage);

        // Handle local CCP logic (if any) based on action
        switch (action) {
            case "STOPC":
                stopAndCloseDoors();
                updateLEDState();
                sendStatus("STOPC");  // Send status back to MCP after sending to ESP32
                break;
            case "STOPO":
                stopAndOpenDoors();
                updateLEDState();
                sendStatus("STOPO");
                break;
            case "FSLOWC":
                if (checkAlignmentWithPhotodiode()) {
                    stopAndCloseDoors();
                } else {
                    moveForwardSlowly();
                }
                updateLEDState();
                sendStatus("FSLOWC");
                break;
            case "FFASTC":
                moveForwardFast();
                updateLEDState();
                sendStatus("FFASTC");
                break;
            case "RSLOWC":
                if (checkAlignmentWithPhotodiode()) {
                    stopAndCloseDoors();
                } else {
                    moveBackwardSlowly();
                }
                updateLEDState();
                sendStatus("RSLOWC");
                break;
            case "DISCONNECT":
                flashStatusLED();
                updateLEDState();
                sendStatus("DISCONNECT");
                break;
            case "HAZARD_DETECTED":
                onHazardDetected();
                updateLEDState();
                sendStatus("HAZARD_DETECTED");
                break;
            case "FLASH_LED":
                flashStatusLED();
                updateLEDState();
                sendStatus("FLASH_LED");
                break;
            case "IRLD":
                String status = jsonMessage.getString("status");
                if ("ON".equalsIgnoreCase(status)) {
                    turnOnIRLED();
                } else if ("OFF".equalsIgnoreCase(status)) {
                    turnOffIRLED();
                }
                updateLEDState();
                sendStatus("IRLD");
                break;
            default:
                System.out.println("Unknown action: " + action);
        }
    }

    private void forwardCommandToESP(String command, JSONObject jsonMessage) {
        // Encode the command into a JSON message to send to ESP32
        JSONObject espMessage = new JSONObject();
        for (String key : jsonMessage.keySet()) {
            espMessage.put(key, jsonMessage.get(key));
        }

        // Update client_type to "esp" if needed
        espMessage.put("client_type", "esp");

        // Send the command to ESP32
        try {
            sendMessageToESP(espMessage.toString());  // Use the sendMessageToESP function to forward the command to ESP32
            System.out.println("Command forwarded to ESP32: " + espMessage.toString());
        } catch (Exception e) {
            System.out.println("Failed to send command to ESP32: " + e.getMessage());
        }
    }

    // Method to stop and close doors
    private void stopAndCloseDoors() {
        System.out.println("BR stopping and closing doors.");
        if (!areDoorsOpen) {
            System.out.println("Doors are already closed.");
            return; // Exit if the doors are already closed
        }
        if (stateManager.getCurrentState() != StateManager.CCPState.STOPPED) {
            System.out.println("Blade Runner is now stopped.");
            stateManager.updateState(StateManager.CCPState.STOPPED);
        }
        System.out.println("Doors are now closed.");
        areDoorsOpen = false; // Update door status
    }

    // Method to flash the BR status LED
    void flashStatusLED() {
        // Simulate flashing the status LED
        System.out.println("Flashing status LED.");
    }

    // Method to stop and open doors
    private void stopAndOpenDoors() {
        System.out.println("BR stopping and opening doors.");
        if (areDoorsOpen) {
            System.out.println("Doors are already open.");
            return; // Exit if the doors are already open
        }
        if (stateManager.getCurrentState() != StateManager.CCPState.STOPPED) {
            System.out.println("Blade Runner is now stopped.");
            stateManager.updateState(StateManager.CCPState.STOPPED);
        }
        System.out.println("Doors are now open.");
        areDoorsOpen = true; // Update door status
    }

    // Method to check alignment with IR photodiode
    private boolean checkAlignmentWithPhotodiode() {
        // Simulating checking alignment with an IR photodiode
        System.out.println("Checking alignment with IR photodiode...");
        return isAlignedWithPhotodiode;
    }

    // Method to simulate setting alignment state with photodiode
    public void setAlignedWithPhotodiode(boolean isAligned) {
        this.isAlignedWithPhotodiode = isAligned;
    }

    // Method to move the BR forward slowly
    private void moveForwardSlowly() {
        System.out.println("BR moving forward slowly.");
        stateManager.updateState(StateManager.CCPState.SLOW_FORWARD);
        // Simulate slow forward movement here
        System.out.println("Moving forward slowly to align with checkpoint...");
    }

    // Method to move the BR forward fast
    private void moveForwardFast() {
        System.out.println("BR moving forward fast.");
        stateManager.updateState(StateManager.CCPState.FULL_SPEED);
        System.out.println("Blade Runner is now moving at full speed.");
    }

    // Method to move the BR backward slowly
    private void moveBackwardSlowly() {
        System.out.println("BR moving backward slowly.");
        stateManager.updateState(StateManager.CCPState.SLOW_BACKWARD);
        // Simulate slow backward movement here
        System.out.println("Moving backward slowly to align with checkpoint...");
    }

    // Method to control LED flashing for each state
    private void updateLEDState() {
        // Stop any ongoing flashing before starting a new LED pattern
        stopLEDFlashing();

        switch (stateManager.getCurrentState()) {
            case STARTED:
                flashLEDPattern(0, 5);  // LED 0 flashes 5 times to indicate STARTED
                break;
            case CONNECTED:
                flashLEDPattern(1, 3);  // LED 1 flashes 3 times to indicate CONNECTED
                break;
            case FULL_SPEED:
                turnOnLED(2);  // LED 2 stays ON to indicate full speed
                break;
            case MAINTAINING_PACE:
                flashLEDPattern(3, 4);  // LED 3 flashes 4 times to indicate maintaining pace
                break;
            case STOPPED:
                turnOnLED(0);  // LED 0 stays ON to indicate stopped state
                break;
            case SLOW_FORWARD:
            case SLOW_BACKWARD:
                flashLEDPattern(2, 6);  // LED 2 flashes 6 times for slow movement
                break;
        }
    }

    // Helper method to flash specific LED in a pattern
    private void flashLEDPattern(int ledIndex, int flashCount) {
        System.out.println("Flashing LED " + ledIndex);
        isLEDFlashing = true;
        final int[] counter = {0};  // Counter to limit flashes

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            if (isLEDFlashing && counter[0] < flashCount) {
                System.out.println("LED " + ledIndex + " ON");
                sleep(500); // LED ON for 0.5 seconds
                System.out.println("LED " + ledIndex + " OFF");
                counter[0]++;  // Increment counter after each cycle
            } else {
                isLEDFlashing = false;  // Stop flashing after flashCount
                executor.shutdown();    // Shutdown the executor
            }
        }, 0, 1000, TimeUnit.MILLISECONDS); // Flash every 1 second (ON/OFF cycle takes 1 second)
    }

    // Helper method to turn on specific LED
    private void turnOnLED(int ledIndex) {
        System.out.println("LED " + ledIndex + " ON");
        isLEDFlashing = false; // Stop flashing if applicable
    }

    // Method to stop LED flashing
    private void stopLEDFlashing() {
        isLEDFlashing = false;
    }

    // Helper method to simulate sleeping
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Method to send status back to MCP
    private void sendStatus(String status) {
        String statusMessage = JSONProcessor.encodeMessage("ccp", status, bladeRunnerId);
        try {
            commHandler.sendMessage(statusMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to update carriage detection status based on received message
    private void updateCarriageDetection(String message) {
        JSONObject jsonMessage = new JSONObject(message);
        String detectedCarriageId = jsonMessage.getString("client_id");

        if (!detectedCarriageId.equals(bladeRunnerId)) {
            int currentId = Integer.parseInt(bladeRunnerId.replace("BR", ""));
            int detectedId = Integer.parseInt(detectedCarriageId.replace("BR", ""));

            if (detectedId > currentId) {
                isCarriageInFront = true;
                isCarriageBehind = false;
            } else {
                isCarriageInFront = false;
                isCarriageBehind = true;
            }
        }
    }

    // Method to check the positions of other carriages
    private void checkCarriagePositions() {
        adjustSpeed(isCarriageInFront, isCarriageBehind);
    }

    // Method to adjust the carriage's speed based on other carriages' positions
    private void adjustSpeed(boolean isCarriageInFront, boolean isCarriageBehind) {
        if (!isCarriageInFront && !isCarriageBehind) {
            stateManager.updateState(StateManager.CCPState.FULL_SPEED);
            System.out.println("Moving at full speed.");
        } else {
            stateManager.updateState(StateManager.CCPState.MAINTAINING_PACE);
            System.out.println("Maintaining pace.");
        }
    }

    // Send message to ESP32
    public void sendMessageToESP(String message) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, espAddress, espPort);
        socket.send(packet);
        socket.close();
        System.out.println("Message sent to ESP32: " + message);
    }

    // Receive message from ESP32 (could be adapted for continuous listening)
    public void onMessageReceivedFromESP32(String message) {
        JSONObject jsonMessage = new JSONObject(message);
        String action = jsonMessage.getString("action");

        if (action.equals("HAZARD_DETECTED")) {
            onHazardDetected();  // Trigger action on hazard detection
        } else {
            System.out.println("Unknown action from ESP32: " + action);
        }
    }

    // Handle hazard detected by ESP32
    public void onHazardDetected() {
        System.out.println("Hazard detected by ESP32! Stopping carriage.");
        stateManager.updateState(StateManager.CCPState.STOPPED);
        stopLEDFlashing(); // Stop any LED flashing
        try {
            sendStatusToESP("HAZARD_STOPPED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send status to ESP32 and wait for acknowledgment
    private void sendStatusToESP(String status) {
        try {
            JSONObject statusMessage = new JSONObject();
            statusMessage.put("client_type", "ccp");
            statusMessage.put("message", status);
            statusMessage.put("client_id", bladeRunnerId);
            statusMessage.put("sequence", sequenceNumbers++);  // Increment sequence number

            // Log the status message before sending
            System.out.println("Sending status to ESP32: " + statusMessage.toString());

            // Send status to ESP32
            sendMessageToESP(statusMessage.toString());

            // Wait for acknowledgment from ESP32 with a timeout
            String ack = waitForAcknowledgment();
            if (ack.equals("ACK")) {
                System.out.println("ESP32 acknowledged the status.");
            } else {
                System.err.println("No acknowledgment received from ESP32.");
            }
        } catch (Exception e) {
            System.err.println("Error sending status to ESP32: " + e.getMessage());
        }
    }

    // Method to wait for acknowledgment from ESP32
    private String waitForAcknowledgment() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramSocket socket = new DatagramSocket(espPort);  // Port used to listen for acknowledgment
        socket.setSoTimeout(5000);  // Timeout after 5 seconds (5000 ms)

        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);  // This will block until a message is received or timeout occurs
            String message = new String(packet.getData(), 0, packet.getLength());

            // Assume acknowledgment message is "ACK"
            if (message.equals("ACK")) {
                return "ACK";
            } else {
                System.err.println("Unexpected acknowledgment message: " + message);
                return "NO_ACK";
            }
        } catch (SocketTimeoutException e) {
            System.err.println("No acknowledgment received, timeout occurred.");
            return "NO_ACK";  // Return "NO_ACK" if timeout occurs
        } finally {
            socket.close();
        }
    }

    // Method to turn on IR LED
    private void turnOnIRLED() {
        System.out.println("Turning ON IR LED.");
        // Simulate turning on IR LED
    }

    // Method to turn off IR LED
    private void turnOffIRLED() {
        System.out.println("Turning OFF IR LED.");
        // Simulate turning off IR LED
    }

    // Main method for testing
    public static void main(String[] args) {
        try {
            SimpleCCP ccp = new SimpleCCP("BR01", "127.0.0.1", 2000, "127.0.0.1", 3000, "127.0.0.1", 3012, 0);
            ccp.connect(); // Connect to MCP

            // Test STOPC command
            {
                System.out.println("\n--- Testing STOPC Command ---");
                // Simulate receiving STOPC command from MCP
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 1);
                message.put("action", "STOPC");
                ccp.onMessageReceived(message.toString());
            }

            // Test STOPO command
            {
                System.out.println("\n--- Testing STOPO Command ---");
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 2);
                message.put("action", "STOPO");
                ccp.onMessageReceived(message.toString());
            }

            // Test FSLOWC command
            {
                System.out.println("\n--- Testing FSLOWC Command ---");
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 3);
                message.put("action", "FSLOWC");
                ccp.onMessageReceived(message.toString());
            }

            // Test FFASTC command
            {
                System.out.println("\n--- Testing FFASTC Command ---");
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 4);
                message.put("action", "FFASTC");
                ccp.onMessageReceived(message.toString());
            }

            // Test RSLOWC command
            {
                System.out.println("\n--- Testing RSLOWC Command ---");
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 5);
                message.put("action", "RSLOWC");
                ccp.onMessageReceived(message.toString());
            }

            // Test DISCONNECT command
            {
                System.out.println("\n--- Testing DISCONNECT Command ---");
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 6);
                message.put("action", "DISCONNECT");
                ccp.onMessageReceived(message.toString());
            }

            // Test HAZARD_DETECTED command
            {
                System.out.println("\n--- Testing HAZARD_DETECTED Command ---");
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 7);
                message.put("action", "HAZARD_DETECTED");
                ccp.onMessageReceived(message.toString());
            }

            // Test FLASH_LED command
            {
                System.out.println("\n--- Testing FLASH_LED Command ---");
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 8);
                message.put("action", "FLASH_LED");
                ccp.onMessageReceived(message.toString());
            }

            // Test IRLD command with status ON
            {
                System.out.println("\n--- Testing IRLD Command with status ON ---");
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 9);
                message.put("action", "IRLD");
                message.put("status", "ON");
                ccp.onMessageReceived(message.toString());
            }

            // Test IRLD command with status OFF
            {
                System.out.println("\n--- Testing IRLD Command with status OFF ---");
                JSONObject message = new JSONObject();
                message.put("client_type", "CCP");
                message.put("message", "EXEC");
                message.put("client_id", "BR01");
                message.put("sequence_number", 10);
                message.put("action", "IRLD");
                message.put("status", "OFF");
                ccp.onMessageReceived(message.toString());
            }

            // You can comment out any of the above blocks to test individual components

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Interface CommunicationHandler
    interface CommunicationHandler {

        void sendMessage(String message) throws Exception;

        void listenForMessages() throws IOException;
    }

    // Class UDPCommunicationHandler
    class UDPCommunicationHandler implements CommunicationHandler {

        private DatagramSocket socket;
        private InetAddress mcpAddress;
        private int mcpPort;
        private MessageListener messageListener;

        // Constructor
        public UDPCommunicationHandler(String mcpAddress, int mcpPort, String ccpAddress, int ccpPort, MessageListener listener) throws Exception {
            this.mcpAddress = InetAddress.getByName(mcpAddress);
            this.mcpPort = mcpPort;
            this.socket = new DatagramSocket(ccpPort, InetAddress.getByName(ccpAddress));
            this.messageListener = listener;

            // Start listening for incoming messages in a new thread
            new Thread(this::listenForMessages).start();
        }

        // Sends a message to the MCP
        @Override
        public void sendMessage(String message) throws Exception {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, mcpAddress, mcpPort);
            socket.send(packet);
            System.out.println("Message sent: " + message);
        }

        // Listens for incoming messages and notifies the listener
        @Override
        public void listenForMessages() {
            try {
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());
                    messageListener.onMessageReceived(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Functional interface for message receiving
        public interface MessageListener {

            void onMessageReceived(String message);
        }
    }

    // Class StateManager
    class StateManager {

        public enum CCPState {
            STARTED, CONNECTED, STOPPED, FULL_SPEED, MAINTAINING_PACE, SLOW_FORWARD, SLOW_BACKWARD
        }

        private CCPState currentState;

        public StateManager() {
            this.currentState = CCPState.STARTED;
        }

        public void updateState(CCPState newState) {
            this.currentState = newState;
            System.out.println("State updated to: " + newState);
            updateLEDState(); // Update LEDs according to the new state
        }

        public CCPState getCurrentState() {
            return currentState;
        }
    }

    // Class JSONProcessor
    static class JSONProcessor {

        // Encodes a message into JSON format
        public static String encodeMessage(String clientType, String messageType, String clientId) {
            JSONObject message = new JSONObject();
            message.put("client_type", clientType);
            message.put("message", messageType);
            message.put("client_id", clientId);
            message.put("timestamp", getTimestamp());
            return message.toString();
        }

        // Decodes a JSON message and returns it in pretty format
        public static String decodeMessage(String message) {
            JSONObject jsonMessage = new JSONObject(message);
            return jsonMessage.toString(4); // Pretty print the JSON
        }

        // Helper function to get the current timestamp
        private static String getTimestamp() {
            return java.time.LocalDateTime.now().toString();
        }
    }
}
