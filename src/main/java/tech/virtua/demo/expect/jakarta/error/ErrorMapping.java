package tech.virtua.demo.expect.jakarta.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path.Node;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import tech.virtua.demo.expect.jakarta.ApplicationException;
import tech.virtua.demo.expect.jakarta.ErrorCode;
import tech.virtua.demo.expect.jakarta.error.ErrorResponse.Violation;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import static jakarta.validation.ElementKind.CONSTRUCTOR;
import static jakarta.validation.ElementKind.CROSS_PARAMETER;
import static jakarta.validation.ElementKind.METHOD;
import static jakarta.validation.ElementKind.PARAMETER;
import static jakarta.validation.ElementKind.RETURN_VALUE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.Family.SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.Family.familyOf;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static java.util.Objects.requireNonNullElse;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.joining;
import static tech.virtua.demo.expect.jakarta.ErrorCode.VALIDATION_FAILED;

/**
 * Turns a failure into the standardized JSON error payload and logs it once
 * against the error ID the caller receives.
 *
 * Jakarta REST matches a provider on its exception type, and one class can't
 * implement {@code ExceptionMapper} twice, so each mapper is a nested class that
 * shares the payload and logging code here.
 */
public final class ErrorMapping {

    private static final Logger LOGGER = Logger.getLogger(ErrorMapping.class.getName());
    private static final String VALIDATION_SUMMARY = "Request validation failed";

    /** Property path nodes that describe the call rather than the rejected input. */
    private static final Set<ElementKind> CALL_NODES =
            EnumSet.of(METHOD, CONSTRUCTOR, PARAMETER, CROSS_PARAMETER, RETURN_VALUE);

    private ErrorMapping() {
    }

    /** Reports an application failure with the status its error code calls for. */
    @Provider
    public static class ApplicationExceptionMapper implements ExceptionMapper<ApplicationException> {

        @Context
        private UriInfo uriInfo;

        @Override
        public Response toResponse(final ApplicationException exception) {
            return respond(uriInfo, exception.errorCode(), exception.getMessage(), List.of(), exception);
        }
    }

    /**
     * Reports a Bean Validation failure as a 400 naming every rejected input. This
     * has to match on {@link ConstraintViolationException} rather than a broader
     * type, because the one Open Liberty ships already covers
     * {@code ValidationException} and the closer match wins.
     */
    @Provider
    public static class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

        @Context
        private UriInfo uriInfo;

        @Override
        public Response toResponse(final ConstraintViolationException exception) {
            return respond(uriInfo, VALIDATION_FAILED, VALIDATION_SUMMARY, violationsOf(exception), exception);
        }
    }

    private static Response respond(final UriInfo uriInfo,
                                    final ErrorCode code,
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

    /** An exception thrown without a message of its own still needs something to report. */
    private static String summarize(final String message, final Status status) {
        return message == null || message.isBlank() ? status.getReasonPhrase() : message;
    }

    private static void log(final ErrorResponse error, final Throwable cause) {
        final var summary = String.format("[%s] %s %s -> %d: %s",
                error.errorId(), error.code(), error.path(), error.status(), error.message());
        if (familyOf(error.status()) == SERVER_ERROR) {
            LOGGER.log(SEVERE, summary, cause);
        } else {
            // A rejected request is the caller's problem, so its stack trace is noise.
            LOGGER.log(WARNING, summary);
        }
    }

    private static List<Violation> violationsOf(final ConstraintViolationException exception) {
        return requireNonNullElse(exception.getConstraintViolations(), Set.<ConstraintViolation<?>>of())
                .stream()
                .map(violation -> new Violation(fieldOf(violation), violation.getMessage()))
                .toList();
    }

    private static String fieldOf(final ConstraintViolation<?> violation) {
        final var path = violation.getPropertyPath();
        final var field = StreamSupport.stream(path.spliterator(), false)
                .filter(node -> !CALL_NODES.contains(node.getKind()))
                .map(Node::getName)
                .collect(joining("."));
        return field.isBlank() ? path.toString() : field;
    }
}
