package tech.virtua.demo.expect.jakarta.messaging.service.expected;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.executable.ValidateOnExecution;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/// Sends and retrieves messages through a messaging service that is only
/// available during its service window.
@ApplicationScoped
@ValidateOnExecution
public class MessageService {

    private static final Logger LOGGER = Logger.getLogger(MessageService.class.getName());
    private final Queue<Message> messages = new ConcurrentLinkedQueue<>();

    /// Sends the given message text. The text must be at least five characters long.
    ///
    /// @param message the text to send
    /// @throws MessageFailureException if the service cannot accept the message
    public void sendMessage(@Valid Message message) throws MessageFailureException {
        if (message.text().toLowerCase().contains("failure")) {
            throw new MessageFailureException("Message could not be sent");
        }
        messages.add(message);
        LOGGER.info("Message sent: " + message);
    }

    /// Retrieves the oldest message from the messaging service.
    ///
    /// @return the current message
    /// @throws OutsideServiceWindowException if the service is outside its service window
    public Message getMessage() throws OutsideServiceWindowException {
        if (withinServiceWindow()) {
            return retrieveMessage();
        } else {
            throw new OutsideServiceWindowException("Service is not available now (outside service window)");
        }
    }

    private boolean withinServiceWindow() {
        return false;
    }

    private Message retrieveMessage() {
        return messages.remove();
    }
}
