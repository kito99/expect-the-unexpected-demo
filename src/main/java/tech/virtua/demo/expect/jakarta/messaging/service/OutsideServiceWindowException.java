package tech.virtua.demo.expect.jakarta.messaging.service;

import tech.virtua.demo.expect.jakarta.ExampleApplicationException;

public class OutsideServiceWindowException extends ExampleApplicationException {

    public OutsideServiceWindowException() {
        super();
    }

    public OutsideServiceWindowException(String message) {
        super(message);
    }
}
