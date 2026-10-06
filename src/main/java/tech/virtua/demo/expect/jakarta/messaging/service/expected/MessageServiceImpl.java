package tech.virtua.demo.expect.jakarta.messaging.service.expected;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.executable.ValidateOnExecution;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

@ApplicationScoped
@ValidateOnExecution
public class MessageServiceImpl implements MessageService {

    private static final Logger LOGGER = Logger.getLogger(MessageServiceImpl.class.getName());
    private final Queue<Message> messages = new ConcurrentLinkedQueue<>();

    @Override
    public void sendMessage(Message message) {
        if (message.text().toLowerCase().contains("failure")) {
            throw new MessageFailureException("Message could not be sent");
        }
        messages.add(message);
        LOGGER.info("Message sent: " + message);
    }
    
    @Override
    public Message getMessage() {
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
