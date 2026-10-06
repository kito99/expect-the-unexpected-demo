package tech.virtua.demo.expect.jakarta.messaging;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.virtua.demo.expect.jakarta.messaging.service.unexpected.Message;
import tech.virtua.demo.expect.jakarta.messaging.service.unexpected.MessageService;
import tech.virtua.demo.expect.jakarta.messaging.service.unexpected.MessageWrapperService;

@Path("messages/unexpected")
public class UnexpectedMessageResource {

    @Inject
    MessageWrapperService repetitiveService;

    @Inject
    MessageService messageService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message getMessage() {
        return messageService.getMessage();
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void sendMessage(String message) {
        repetitiveService.sendMessage(message);
    }
}
