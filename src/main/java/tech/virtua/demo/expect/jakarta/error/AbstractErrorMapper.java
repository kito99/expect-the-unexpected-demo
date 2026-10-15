package tech.virtua.demo.expect.jakarta.error;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import tech.virtua.demo.expect.jakarta.ErrorCode;
import tech.virtua.demo.expect.jakarta.error.ErrorResponse.Violation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.Family.SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.Family.familyOf;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.joining;
import static tech.virtua.demo.expect.jakarta.ErrorCode.VALIDATION_FAILED;

/// Turns an [ErrorCode] into the standardized JSON error payload and writes
/// one log entry per failure, keyed by the error ID the caller receives.
abstract class AbstractErrorMapper {

    private static final Logger LOGGER = Logger.getLogger(AbstractErrorMapper.class.getName());

    @Context
    protected UriInfo uriInfo;

    protected final Response respond(final ErrorCode code,
                                     final String message,
                                     final List<Violation> violations,
                                     final Throwable cause) {
        final var status = statusOf(code);
        final var error = new ErrorResponse(code,
                status.getStatusCode(),
                summarize(message, status),
                uriInfo.getRequestUri().getPath(),
                UUID.randomUUID().toString(),
                Instant.now(),
                violations);
        log(error, cause);
        return Response.status(status).type(APPLICATION_JSON).entity(error).build();
    }

    private static Status statusOf(final ErrorCode code) {
        return switch (code) {
            case VALIDATION_FAILED -> BAD_REQUEST;
            case OUTSIDE_SERVICE_WINDOW -> SERVICE_UNAVAILABLE;
            case MESSAGE_SEND_FAILED, INTERNAL_ERROR -> INTERNAL_SERVER_ERROR;
        };
    }

    /// An exception thrown without a message of its own still needs something to report.
    private static String summarize(final String message, final Status status) {
        return message == null || message.isBlank() ? status.getReasonPhrase() : message;
    }

    private static void log(final ErrorResponse error, final Throwable cause) {
        final var summary = String.format("[%s] %s %s -> %d: %s%s",
                error.errorId(), error.code(), error.path(), error.status(), error.message(),
                describe(error.violations()));
        final var level = familyOf(error.status()) == SERVER_ERROR ? SEVERE : WARNING;
        if (error.code() == VALIDATION_FAILED) {
            // A rejected request is the caller's problem, and the trace only runs back
            // through the validation interceptor, so it points at nothing actionable.
            LOGGER.log(level, summary);
        } else {
            LOGGER.log(level, summary, cause);
        }
    }

    /// Names the rejected inputs so a validation entry stands on its own without the payload.
    private static String describe(final List<Violation> violations) {
        return violations.isEmpty() ? "" : violations.stream()
                .map(violation -> String.format("%s: %s", violation.field(), violation.message()))
                .collect(joining("; ", " [", "]"));
    }
}
