package tech.virtua.demo.expect.jakarta.messaging.service;

import tech.virtua.demo.expect.jakarta.ApplicationException;

import static tech.virtua.demo.expect.jakarta.ErrorCode.OUTSIDE_SERVICE_WINDOW;

public class OutsideServiceWindowException extends ApplicationException {

    public OutsideServiceWindowException() {
        super(OUTSIDE_SERVICE_WINDOW);
    }

    public OutsideServiceWindowException(final String message) {
        super(OUTSIDE_SERVICE_WINDOW, message);
    }
}
