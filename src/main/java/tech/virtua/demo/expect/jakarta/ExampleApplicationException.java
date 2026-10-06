package tech.virtua.demo.expect.jakarta;

public class ExampleApplicationException extends RuntimeException {

    public ExampleApplicationException() {
        super();
    }

    public ExampleApplicationException(String message) {
        super(message);
    }

    public ExampleApplicationException(Throwable cause) {
        super(cause);
    }
}
