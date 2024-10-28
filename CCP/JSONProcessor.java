package CCP;

import org.json.JSONObject;

public class JSONProcessor {

    // Encodes a message into JSON format
    public static String encodeMessage(String clientType,
                                       String messageType,
                                       String clientId) {
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
