package tech.virtua.demo.expect.jakarta.messaging.service.expected;

import tech.virtua.demo.expect.jakarta.ApplicationException;

import static tech.virtua.demo.expect.jakarta.ErrorCode.MESSAGE_SEND_FAILED;

public class MessageFailureException extends ApplicationException {

    public MessageFailureException() {
        super(MESSAGE_SEND_FAILED);
    }

    public MessageFailureException(final String message) {
        super(MESSAGE_SEND_FAILED, message);
    }

    public MessageFailureException(final Throwable cause) {
        super(MESSAGE_SEND_FAILED, cause);
    }
}
