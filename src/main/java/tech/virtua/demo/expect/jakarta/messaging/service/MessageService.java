package tech.virtua.demo.expect.jakarta.messaging.service;

import jakarta.validation.Valid;

/**
 * Sends and retrieves messages through a messaging service that is only
 * available during its service window.
 */
public interface MessageService {

    /**
     * Sends the given message text. The text must be at least five characters long.
     *
     * @param message the text to send
     */
    void sendMessage(@Valid Message message);

    /**
     * Retrieves the oldest message from the messaging service.
     *
     * @return the current message
     */
    Message getMessage();
}
