import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.json.JSONObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleCCP {

    private String bladeRunnerId;
    private StateManager stateManager;
    private CommunicationHandler commHandler;
    private boolean isCarriageInFront;
    private boolean isCarriageBehind;
    private boolean isAlignedWithPhotodiode; // Simulates alignment with IR photodiode
    private boolean isLEDFlashing; // Simulates LED flashing state

    public SimpleCCP(String bladeRunnerId, String mcpAddress, int mcpPort, String ccpAddress, int ccpPort) throws Exception {
        this.bladeRunnerId = bladeRunnerId;
        this.stateManager = new StateManager();
        this.commHandler = new UDPCommunicationHandler(mcpAddress, mcpPort, ccpAddress, ccpPort, this::onMessageReceived);

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
        processMCPAction(message);

        // Update the state based on received messages
        updateCarriageDetection(message);
        checkCarriagePositions(); // Check for nearby carriages after receiving a message
    }

    // Method to process MCP actions
    private void processMCPAction(String message) {
        JSONObject jsonMessage = new JSONObject(message);
        String action = jsonMessage.getString("action");

        switch (action) {
            case "STOPC":
                stopAndCloseDoors();
                sendStatus("STOPC");
                break;
            case "STOPO":
                stopAndOpenDoors();
                sendStatus("STOPO");
                break;
            case "FSLOWC":
                if (checkAlignmentWithPhotodiode()) {
                    stopAndCloseDoors();
                } else {
                    moveForwardSlowly();
                }
                sendStatus("STOPC");
                break;
            case "FFASTC":
                moveForwardFast();
                sendStatus("FFASTC");
                break;
            case "RSLOWC":
                if (checkAlignmentWithPhotodiode()) {
                    stopAndCloseDoors();
                } else {
                    moveBackwardSlowly();
                }
                sendStatus("STOPC");
                break;
            case "DISCONNECT":
                flashStatusLED();
                break;
            default:
                System.out.println("Unknown action: " + action);
        }
    }

    private boolean areDoorsOpen = false; // Track if doors are open

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
        // For example, this could be updated based on real-time sensor input
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
        // You can add delays or specific movement commands if needed
        System.out.println("Moving forward slowly to align with checkpoint...");
    }

    // Method to move the BR forward fast
    private void moveForwardFast() {
        System.out.println("BR moving forward fast.");
        // Simulate fast movement, adjusting speed and state
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

    // Method to flash the BR status LED
    // Method to control LED flashing for each state
    private void updateLEDState() {
        switch (stateManager.getCurrentState()) {
            case STARTED:
                flashLEDPattern(0); // LED 0 flashes to indicate STARTED
                break;
            case CONNECTED:
                flashLEDPattern(1); // LED 1 flashes to indicate CONNECTED
                break;
            case FULL_SPEED:
                turnOnLED(2); // LED 2 stays ON to indicate full speed
                break;
            case MAINTAINING_PACE:
                flashLEDPattern(3); // LED 3 flashes to indicate maintaining pace
                break;
            case STOPPED:
                turnOnLED(0); // LED 0 stays ON to indicate stopped state
                break;
            case SLOW_FORWARD:
            case SLOW_BACKWARD:
                flashLEDPattern(2); // LED 2 flashes for slow movement
                break;
        }
    }

// Helper method to flash specific LED in a pattern 
    private void flashLEDPattern(int ledIndex) {
        System.out.println("Flashing LED " + ledIndex);
        isLEDFlashing = true;

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            if (isLEDFlashing) {
                System.out.println("LED " + ledIndex + " ON");
                sleep(500); // LED ON for 0.5 seconds
                System.out.println("LED " + ledIndex + " OFF");
            } else {
                executor.shutdown();
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
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
// Method to process hazard detection from ESP32

    public void onHazardDetected() {
        System.out.println("Hazard detected by ESP32! Stopping carriage.");
        stateManager.updateState(StateManager.CCPState.STOPPED); // Update state to STOPPED
        stopLEDFlashing(); // Stop any LED flashing if needed
        sendStatus("HAZARD_STOPPED"); // Send status back
    }

// Simulate receiving a message from ESP32 about a hazard
    private void onMessageReceivedFromESP32(String message) {
        JSONObject jsonMessage = new JSONObject(message);
        String action = jsonMessage.getString("action");

        if (action.equals("HAZARD_DETECTED")) {
            onHazardDetected(); // Stop the carriage on hazard detection
        }
    }

    public static void main(String[] args) {
        try {
            SimpleCCP ccp = new SimpleCCP("BR01", "127.0.0.1", 2000, "127.0.0.1", 3000);
            ccp.connect(); // Connect to MCP
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
