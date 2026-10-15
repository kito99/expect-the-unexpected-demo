package tech.virtua.demo.expect.jakarta.messaging.service.unexpected;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/// Sends and retrieves messages through a messaging service that is only
/// available during its service window.
@ApplicationScoped
public class MessageServiceWrapper {

    private final MessageService service;

    protected MessageServiceWrapper() {
        this.service = null;
    }

    @Inject
    public MessageServiceWrapper(MessageService service) {
        this.service = service;
    }

    /// Sends the given message text. The text must be at least five characters long.
    ///
    /// @param message the text to send
    public void sendMessage(String message) {
        try {
            sendRepeatedly(message, 0, 10);
        } catch (Exception e) {
            System.err.println("Could not send message: " + message);
        }
    }

    private void sendRepeatedly(String message, int iteration, int maxLevel) {
        try {
            if (iteration == maxLevel) {
                service.sendMessage(new Message(message));
            } else {
                sendRepeatedly(message, iteration + 1, maxLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
