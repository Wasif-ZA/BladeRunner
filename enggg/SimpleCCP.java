import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.json.JSONObject;

public class SimpleCCP {

    private String bladeRunnerId;
    private StateManager stateManager;
    private CommunicationHandler commHandler;

    public SimpleCCP(String bladeRunnerId, String mcpAddress, int mcpPort, String ccpAddress, int ccpPort) throws Exception {
        this.bladeRunnerId = bladeRunnerId;
        this.stateManager = new StateManager();
        this.commHandler = new UDPCommunicationHandler(mcpAddress, mcpPort, ccpAddress, ccpPort, this::onMessageReceived);

        // Initially, CCP is in the STARTED state
        this.stateManager.updateState(StateManager.CCPState.STARTED);
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
            STARTED, CONNECTED
        }

        private CCPState currentState;

        public StateManager() {
            this.currentState = CCPState.STARTED;
        }

        public void updateState(CCPState newState) {
            this.currentState = newState;
            System.out.println("State updated to: " + newState);
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
            return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm+10:00'Z'").format(new java.util.Date());
        }
    }
}