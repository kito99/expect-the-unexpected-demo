package tech.virtua.demo.expect.jakarta.messaging.service.expected;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MessageWrapperServiceImpl implements MessageWrapperService {

    private final MessageService service;

    // CDI needs a non-private no-arg constructor to create the client proxy.
    protected MessageWrapperServiceImpl() {
        this.service = null;
    }

    @Inject
    public MessageWrapperServiceImpl(MessageService service) {
        this.service = service;
    }

    // tag::redundant-logging[]
    @Override
    public void sendMessage(String message) {
        try {
            sendRepeatedly(message, 0, 10);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MessageFailureException(e);
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
    // end::redundant-logging[]
}
