package tech.virtua.demo.expect.jakarta.messaging.service.expected;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.virtua.demo.expect.jakarta.ApplicationException;

// TODO: Remove interface and keep same functionality

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
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
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
            throw e;
        }
    }
}
