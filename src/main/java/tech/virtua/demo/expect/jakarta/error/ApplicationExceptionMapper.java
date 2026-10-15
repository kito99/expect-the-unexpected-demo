package tech.virtua.demo.expect.jakarta.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import tech.virtua.demo.expect.jakarta.ApplicationException;

import java.util.List;

/// Reports an application failure with the status its error code calls for.
@Provider
public class ApplicationExceptionMapper extends AbstractErrorMapper
        implements ExceptionMapper<ApplicationException> {

    @Override
    public Response toResponse(final ApplicationException exception) {
        return respond(exception.errorCode(), exception.getMessage(), List.of(), exception);
    }
}
