package CCP;

public interface CommunicationHandler {
    void sendMessage(String message) throws Exception;
    void listenForMessages();
}
