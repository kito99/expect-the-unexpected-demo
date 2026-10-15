package tech.virtua.demo.expect.jakarta;

import tech.virtua.demo.expect.jakarta.messaging.service.expected.MessageFailureException;
import tech.virtua.demo.expect.jakarta.messaging.service.expected.OutsideServiceWindowException;

import static java.util.Objects.requireNonNull;

/// Base type for failures raised by this application. Every instance carries the
/// [ErrorCode] that the REST layer reports to the caller.
public sealed class ApplicationException extends RuntimeException
        permits MessageFailureException, OutsideServiceWindowException {

    private final ErrorCode errorCode;

    public ApplicationException(final ErrorCode errorCode) {
        this.errorCode = requireNonNull(errorCode);
    }

    public ApplicationException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = requireNonNull(errorCode);
    }

    public ApplicationException(final ErrorCode errorCode, final Throwable cause) {
        super(cause);
        this.errorCode = requireNonNull(errorCode);
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
