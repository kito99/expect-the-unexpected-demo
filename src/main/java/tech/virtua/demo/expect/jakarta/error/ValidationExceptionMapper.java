package tech.virtua.demo.expect.jakarta.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path.Node;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import tech.virtua.demo.expect.jakarta.error.ErrorResponse.Violation;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static jakarta.validation.ElementKind.CONSTRUCTOR;
import static jakarta.validation.ElementKind.CROSS_PARAMETER;
import static jakarta.validation.ElementKind.METHOD;
import static jakarta.validation.ElementKind.PARAMETER;
import static jakarta.validation.ElementKind.RETURN_VALUE;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.joining;
import static tech.virtua.demo.expect.jakarta.ErrorCode.VALIDATION_FAILED;

/// Reports a Bean Validation failure as a 400 naming every rejected input. This has
/// to match on [ConstraintViolationException] rather than a broader type,
/// because the one Open Liberty ships already covers `ValidationException`
/// and the closer match wins.
@Provider
public class ValidationExceptionMapper extends AbstractErrorMapper
        implements ExceptionMapper<ConstraintViolationException> {

    private static final String SUMMARY = "Request validation failed";

    /// Property path nodes that describe the call rather than the rejected input.
    private static final Set<ElementKind> CALL_NODES =
            EnumSet.of(METHOD, CONSTRUCTOR, PARAMETER, CROSS_PARAMETER, RETURN_VALUE);

    @Override
    public Response toResponse(final ConstraintViolationException exception) {
        return respond(VALIDATION_FAILED, SUMMARY, violationsOf(exception), exception);
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
