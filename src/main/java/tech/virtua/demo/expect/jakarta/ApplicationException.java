package tech.virtua.demo.expect.jakarta;

import static java.util.Objects.requireNonNull;

/// Base type for failures raised by this application. Every instance carries the
/// [ErrorCode] that the REST layer reports to the caller.
public class ApplicationException extends RuntimeException {

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
