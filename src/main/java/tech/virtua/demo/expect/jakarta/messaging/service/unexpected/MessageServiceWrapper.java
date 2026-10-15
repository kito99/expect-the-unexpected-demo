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
            nestedSend(message, 0, 10);
        } catch (Exception e) {
            throw e;
        }
    }

    /// Simulates sending the message after going down through {@maxLevel} layers of code.
    /// 
    /// @param message the text to send
    /// @param iteration the current iteration level
    /// @param maxLevel the maximum number of iterations
    private void nestedSend(String message, int iteration, int maxLevel) {
        try {
            if (iteration == maxLevel) {
                service.sendMessage(new Message(message));
            } else {
                nestedSend(message, iteration + 1, maxLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
