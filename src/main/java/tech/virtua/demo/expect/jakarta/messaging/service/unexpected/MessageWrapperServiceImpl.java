package tech.virtua.demo.expect.jakarta.messaging.service.unexpected;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MessageWrapperServiceImpl implements MessageWrapperService {

    private final MessageService service;

    protected MessageWrapperServiceImpl() {
        this.service = null;
    }

    @Inject
    public MessageWrapperServiceImpl(MessageService service) {
        this.service = service;
    }

    @Override
    public void sendMessage(String message) {
        try {
            sendRepeatedly(message, 0, 10);
        } catch (Exception e) {
            System.err.println("Could not send message: " + message);
            throw new RuntimeException(e);
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
            throw e;
        }
    }
}
