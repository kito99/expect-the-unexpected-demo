package tech.virtua.demo.expect.jakarta.messaging.service.unexpected;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ApplicationScoped
public class MessageServiceImpl implements MessageService {

    private static final int MINIMUM_TEXT_LENGTH = 5;

    private final Queue<Message> messages = new ConcurrentLinkedQueue<>();

    @Override
    public void sendMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        if (message.text() == null || message.text().isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        if (message.text().length() < MINIMUM_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("text must be at least %d characters", MINIMUM_TEXT_LENGTH));
        }
        if (message.text().toLowerCase().contains("failure")) {
            throw new RuntimeException("Message could not be sent");
        }
        messages.add(message);
        System.out.println("Message sent: " + message);
    }

    @Override
    public Message getMessage() {
        if (withinServiceWindow()) {
            return retrieveMessage();
        } else {
            throw new IllegalStateException("Service is not available now (outside service window)");
        }
    }

    private boolean withinServiceWindow() {
        return false;
    }

    private Message retrieveMessage() {
        return messages.remove();
    }
}
