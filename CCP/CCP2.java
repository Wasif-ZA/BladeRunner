package CCP;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import org.json.JSONObject;

public class CCP2 {

    private static final int MCP_PORT = 2000; // MCP command listening port
    private static final int BR_PORT = 3012;  // BR command listening port
    private static final int CCP_PORT = 3012; // CCP update listening port
    private static final Random RANDOM = new Random();
    String mcpAddress = "10.20.30.1";
    String brAddress = "10.20.30.112";
    private volatile String currentBRStatus = "STOPC";
    private volatile boolean startupAckReceived = false;
    private int s_ccp; // Sequence number for packets sent to MCP
    private DatagramSocket udpSocket;

    public CCP2() {
        // Initialize the sequence number randomly between 1000 and 30000
        this.s_ccp = RANDOM.nextInt(29001) + 1000; // 1000 to 30000
    }

    public static void main(String[] args) {
        CCP2 ccp = new CCP2();
        ccp.start();
    }

    public void start() {
        try {
            udpSocket = new DatagramSocket(); // Create UDP socket
            // Send startup status to MCP with retry
            new Thread(this::sendStartupStatusToMCP).start();

            // Start listener for MCP commands
            new Thread(this::listenForMCPCommands).start();

            // Start listener for BR status updates
            new Thread(this::listenForBRUpdates).start();
        } catch (SocketException e) {
            System.err.println("Failed to create UDP socket: " + e.getMessage());
        }
    }

    private void sendStartupStatusToMCP() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (!startupAckReceived) {
                JSONObject startupStatus = new JSONObject();
                startupStatus.put("client_type", "CCP");
                startupStatus.put("message", "CCIN");
                startupStatus.put("client_id", "BR12");
                startupStatus.put("sequence_number", s_ccp);
                startupStatus.put("status", currentBRStatus);

                forwardToMCP(startupStatus);
            } else {
                scheduler.shutdown(); // Stop scheduling once AKIN is received
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void listenForMCPCommands() {
        try (DatagramSocket serverSocket = new DatagramSocket(CCP_PORT)) {
            System.out.println("Listening for MCP commands on port " + CCP_PORT);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);

                // Parse the MCP command
                String request = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                JSONObject mcpCommand = new JSONObject(request);
                System.out.println("Received MCP Command: " + mcpCommand);

                switch (mcpCommand.getString("message")) {
                    case "AKIN":
                        startupAckReceived = true;
                        System.out.println("Received AKIN acknowledgment from MCP. Stopping startup status messages.");
                        break;
                    case "STRQ":
                        sendCurrentStatusToMCP(mcpCommand.getString("client_id"));
                        break;
                    case "EXEC":
                        forwardToBR(mcpCommand);
                        break;
                    case "AKST":
                        break;
                    case "NOIP":
                        break;
                    default:
                        sendNoipToMCP(mcpCommand.getString("client_id"), mcpCommand.getInt("sequence_number"));
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on MCP port: " + e.getMessage());
        }
    }

    private void sendNoipToMCP(String clientId, int seq) {
        JSONObject noipResponse = new JSONObject();
        noipResponse.put("client_type", "CCP");
        noipResponse.put("message", "NOIP");
        noipResponse.put("client_id", clientId);
        noipResponse.put("sequence_number", seq + 1);

        forwardToMCP(noipResponse);
        System.out.println("Sent NOIP response to MCP for unknown message type.");
    }

    private void forwardToBR(JSONObject mcpCommand) {
        try {
            byte[] commandBytes = mcpCommand.toString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(commandBytes, commandBytes.length, InetAddress.getByName(brAddress), BR_PORT);
            udpSocket.send(packet);
            System.out.println("Forwarded command to BR: " + mcpCommand);
        } catch (IOException e) {
            System.err.println("Failed to forward command to BR: " + e.getMessage());
        }
    }

    private void listenForBRUpdates() {
        try (DatagramSocket serverSocket = new DatagramSocket(CCP_PORT)) {
            System.out.println("Listening for BR updates on port " + CCP_PORT);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);

                // Parse the BR status update
                String request = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                JSONObject brStatus = new JSONObject(request);
                System.out.println("Received BR Update: " + brStatus);

                // Update current BR status and forward it to MCP
                currentBRStatus = brStatus.getString("status");
                forwardToMCP(brStatus);
            }
        } catch (IOException e) {
            System.err.println("Could not listen on CCP port: " + e.getMessage());
        }
    }

    private void sendCurrentStatusToMCP(String clientId) {
        // Create a JSON response using the current BR status
        JSONObject statusResponse = new JSONObject();
        statusResponse.put("client_type", "CCP");
        statusResponse.put("message", "STAT");
        statusResponse.put("client_id", clientId);
        statusResponse.put("sequence_number", s_ccp);
        statusResponse.put("status", currentBRStatus);

        forwardToMCP(statusResponse);
    }

    private void forwardToMCP(JSONObject brStatus) {
        try {
            byte[] statusBytes = brStatus.toString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(statusBytes, statusBytes.length, InetAddress.getByName(mcpAddress), MCP_PORT);
            udpSocket.send(packet);
            System.out.println("Forwarded status to MCP: " + brStatus);
            s_ccp++; // Increment the sequence number after sending
        } catch (IOException e) {
            System.err.println("Failed to forward status to MCP: " + e.getMessage());
        }
    }
}