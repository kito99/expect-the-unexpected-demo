package tech.virtua.demo.expect.jakarta.messaging.service.unexpected;

/**
 * Sends messages by passing them through a chain of nested send attempts
 * before handing them to the underlying {@link MessageService}.
 */
public interface MessageWrapperService {

    /**
     * Sends the given message through the underlying {@link MessageService}.
     *
     * @param message the message to send
     */
    void sendMessage(String message);
}
