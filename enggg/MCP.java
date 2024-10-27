import java.io.IOException;
import java.net.*;
import org.json.JSONObject;
import java.util.Random;

public class MCP {

    private InetAddress ccpAddress;
    private int ccpPort;
    private DatagramSocket mcpSocket;
    private int sequenceNumber = new Random().nextInt(29001) + 1000; // Sequence number between 1000 and 30000

    public MCP(String ccpIp, int ccpPort) throws SocketException, UnknownHostException {
        this.ccpAddress = InetAddress.getByName(ccpIp);
        this.ccpPort = ccpPort;
        this.mcpSocket = new DatagramSocket(2000); // MCP listens on port 2000
    }

    // Send initiation response to CCP initiation request
    public void sendInitiationAck(String ccpId) throws IOException {
        JSONObject message = new JSONObject();
        message.put("client_type", "CCP");
        message.put("message", "AKIN");
        message.put("client_id", ccpId);
        message.put("sequence_number", sequenceNumber++);
        
        sendMessage(message);
    }

    // Send status request (heartbeat) to CCP
    public void sendStatusRequest(String ccpId) throws IOException {
        JSONObject message = new JSONObject();
        message.put("client_type", "CCP");
        message.put("message", "STRQ");
        message.put("client_id", ccpId);
        message.put("sequence_number", sequenceNumber++);
        
        sendMessage(message);
    }

    // Generic method to send an EXEC command to CCP
    private void sendCommand(String action, String ccpId) throws IOException {
        JSONObject message = new JSONObject();
        message.put("client_type", "CCP");
        message.put("message", "EXEC");
        message.put("client_id", ccpId);
        message.put("sequence_number", sequenceNumber++);
        message.put("action", action);
        
        sendMessage(message);
    }

    // Command methods to cover CCP functionalities
    public void sendStopAndCloseCommand(String ccpId) throws IOException {
        sendCommand("STOPC", ccpId);
    }

    public void sendStopAndOpenCommand(String ccpId) throws IOException {
        sendCommand("STOPO", ccpId);
    }

    public void sendMoveForwardSlowCommand(String ccpId) throws IOException {
        sendCommand("FSLOWC", ccpId);
    }

    public void sendMoveForwardFastCommand(String ccpId) throws IOException {
        sendCommand("FFASTC", ccpId);
    }

    public void sendMoveBackwardSlowCommand(String ccpId) throws IOException {
        sendCommand("RSLOWC", ccpId);
    }

    public void sendDisconnectCommand(String ccpId) throws IOException {
        sendCommand("DISCONNECT", ccpId);
    }

    public void sendHazardCommand(String ccpId) throws IOException {
        sendCommand("HAZARD_DETECTED", ccpId);
    }

    // Send specific commands for LED and IR controls
    public void sendFlashLedCommand(String ccpId) throws IOException {
        sendCommand("FLASH_LED", ccpId);
    }

    public void sendIRLedOnCommand(String ccpId) throws IOException {
        JSONObject data = new JSONObject();
        data.put("status", "ON");
        sendCommandWithData("IRLD", ccpId, data);
    }

    public void sendIRLedOffCommand(String ccpId) throws IOException {
        JSONObject data = new JSONObject();
        data.put("status", "OFF");
        sendCommandWithData("IRLD", ccpId, data);
    }

    // Command with additional data
    private void sendCommandWithData(String action, String ccpId, JSONObject additionalData) throws IOException {
        JSONObject message = new JSONObject();
        message.put("client_type", "CCP");
        message.put("message", "EXEC");
        message.put("client_id", ccpId);
        message.put("sequence_number", sequenceNumber++);
        message.put("action", action);
        
        if (additionalData != null) {
            for (String key : additionalData.keySet()) {
                message.put(key, additionalData.get(key));
            }
        }

        sendMessage(message);
    }

    // Send status acknowledgment for CCP status updates
    public void sendStatusAck(String ccpId) throws IOException {
        JSONObject message = new JSONObject();
        message.put("client_type", "CCP");
        message.put("message", "AKST");
        message.put("client_id", ccpId);
        message.put("sequence_number", sequenceNumber++);
        
        sendMessage(message);
    }

    // Generic method to send messages
    private void sendMessage(JSONObject message) throws IOException {
        byte[] buffer = message.toString().getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ccpAddress, ccpPort);
        mcpSocket.send(packet);
        System.out.println("Sent message: " + message.toString());
    }

    // Receive incoming messages from CCP
    public void listenForMessages() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while (true) {
            try {
                mcpSocket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received message: " + received);
                
                JSONObject message = new JSONObject(received);
                processReceivedMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Process received message based on its type
    private void processReceivedMessage(JSONObject message) throws IOException {
        String msgType = message.getString("message");
        String ccpId = message.getString("client_id");

        switch (msgType) {
            case "CCIN": // Initiation from CCP
                sendInitiationAck(ccpId);
                break;
            case "STAT": // CCP Status Update
                sendStatusAck(ccpId);
                break;
            default:
                System.out.println("Unknown message type received: " + msgType);
        }
    }

    // Main method for testing all MCP commands
    public static void main(String[] args) {
        try {
            String ccpIp = "10.20.30.101"; // Replace with actual CCP IP
            int ccpPort = 3001;            // Replace with actual CCP Port
            String ccpId = "BR01";         // Blade Runner ID

            MCP mcp = new MCP(ccpIp, ccpPort);

            // Send all commands to test CCP functionalities
            mcp.sendStatusRequest(ccpId);               // Heartbeat check
            mcp.sendMoveForwardSlowCommand(ccpId);      // Move forward slowly
            mcp.sendMoveForwardFastCommand(ccpId);      // Move forward fast
            mcp.sendMoveBackwardSlowCommand(ccpId);     // Move backward slowly
            mcp.sendStopAndCloseCommand(ccpId);         // Stop and close doors
            mcp.sendStopAndOpenCommand(ccpId);          // Stop and open doors
            mcp.sendFlashLedCommand(ccpId);             // Flash LED
            mcp.sendIRLedOnCommand(ccpId);              // Turn IR LED on
            mcp.sendIRLedOffCommand(ccpId);             // Turn IR LED off
            mcp.sendHazardCommand(ccpId);               // Simulate hazard detected
            mcp.sendDisconnectCommand(ccpId);           // Disconnect command
            
            // Start listening for responses from CCP
            mcp.listenForMessages();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
