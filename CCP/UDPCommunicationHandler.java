package CCP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPCommunicationHandler implements CommunicationHandler {

    private DatagramSocket socket;
    private InetAddress mcpAddress;
    private int mcpPort;
    private MessageListener messageListener;

    // Constructor
    public UDPCommunicationHandler(String mcpAddress, int mcpPort,
                                   String ccpAddress, int ccpPort,
                                   MessageListener listener) throws Exception {
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
        System.out.println("Message sent to MCP: " + message);
    }

    // Listens for incoming messages and notifies the listener
    @Override
    public void listenForMessages() {
        try {
            while (true) {
                byte[] buffer = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                messageListener.onMessageReceived(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
