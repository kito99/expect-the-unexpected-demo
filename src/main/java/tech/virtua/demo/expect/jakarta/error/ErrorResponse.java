package tech.virtua.demo.expect.jakarta.error;

import tech.virtua.demo.expect.jakarta.ErrorCode;

import java.time.Instant;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * Carries the details of a failed request back to the caller as JSON.
 *
 * @param code       the kind of failure
 * @param status     the HTTP status that carries this payload
 * @param message    a human-readable summary of the failure
 * @param path       the request path that failed
 * @param errorId    ties this response to the matching server log entry
 * @param timestamp  when the failure was reported
 * @param violations the rejected inputs, empty unless validation failed
 */
public record ErrorResponse(ErrorCode code,
                            int status,
                            String message,
                            String path,
                            String errorId,
                            Instant timestamp,
                            List<Violation> violations) {

    public ErrorResponse {
        requireNonNull(code);
        requireNonNull(message);
        requireNonNull(path);
        requireNonNull(errorId);
        requireNonNull(timestamp);
        violations = List.copyOf(requireNonNullElse(violations, List.of()));
    }

    /**
     * Names one rejected input and the rule it broke.
     *
     * @param field   the property path of the rejected input
     * @param message the constraint message
     */
    public record Violation(String field, String message) { }
}
