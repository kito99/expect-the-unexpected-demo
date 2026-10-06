package tech.virtua.demo.expect.jakarta.messaging.service;

import tech.virtua.demo.expect.jakarta.ExampleApplicationException;

public class MessageFailureException extends ExampleApplicationException {

    public MessageFailureException() {
        super();
    }

    public MessageFailureException(String message) {
        super(message);
    }

    public MessageFailureException(Throwable cause) {
        super(cause);
    }
}
