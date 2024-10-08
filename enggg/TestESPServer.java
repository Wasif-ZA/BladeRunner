import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.JSONObject;

public class TestESPServer {

    private DatagramSocket socket;
    private int port;

    public TestESPServer(int port) throws Exception {
        this.port = port;
        socket = new DatagramSocket(port);
        System.out.println("Test ESP Server listening on port: " + port);
    }

    // Listen for incoming JSON messages
    public void start() throws Exception {
        byte[] buffer = new byte[1024];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received JSON: " + received);

            // Parse JSON message
            JSONObject json = new JSONObject(received);
            String clientType = json.getString("client_type");
            String message = json.getString("message");
            String clientId = json.getString("client_id");

            // Respond with an ACK if the message is valid
            if (clientType.equals("ccp") && message != null) {
                System.out.println("Sending ACK to: " + clientId);
                sendAcknowledgment(packet.getAddress(), packet.getPort(), "ACK");
            }
        }
    }

    // Send an acknowledgment back to the sender
    private void sendAcknowledgment(InetAddress address, int port, String ackMessage) throws Exception {
        JSONObject ack = new JSONObject();
        ack.put("message", ackMessage);
        byte[] ackBytes = ack.toString().getBytes();
        DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, address, port);
        socket.send(ackPacket);
        System.out.println("ACK sent.");
    }

    public static void main(String[] args) {
        try {
            TestESPServer server = new TestESPServer(3001);  // Same port used in SimpleCCP
            server.start();  // Start the server and listen for incoming JSON
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
