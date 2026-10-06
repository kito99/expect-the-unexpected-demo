package tech.virtua.demo.expect.jakarta;

/**
 * Identifies the kind of failure reported to a caller. The REST layer turns each
 * value into an HTTP status, so nothing here depends on HTTP.
 */
public enum ErrorCode {

    /** Input failed Bean Validation. */
    VALIDATION_FAILED,

    /** The messaging service was reached outside its service window. */
    OUTSIDE_SERVICE_WINDOW,

    /** A message could not be handed to the messaging service. */
    MESSAGE_SEND_FAILED,

    /** Nothing more specific is known about the failure. */
    INTERNAL_ERROR
}
