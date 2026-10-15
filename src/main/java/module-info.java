/// Groups the demo's packages into one module so that [tech.virtua.demo.expect.jakarta.ApplicationException]
/// can be sealed over subclasses that live in another package.
module tech.virtua.demo.expect.jakarta {

    requires jakarta.cdi;
    requires jakarta.inject;
    requires jakarta.validation;
    requires jakarta.ws.rs;
    requires java.logging;

    exports tech.virtua.demo.expect.jakarta;
    exports tech.virtua.demo.expect.jakarta.error;
    exports tech.virtua.demo.expect.jakarta.messaging.service.expected;
    exports tech.virtua.demo.expect.jakarta.messaging.service.unexpected;

    // CDI builds proxies and JSON-B reads records by reflection.
    opens tech.virtua.demo.expect.jakarta;
    opens tech.virtua.demo.expect.jakarta.error;
    opens tech.virtua.demo.expect.jakarta.messaging.service.expected;
    opens tech.virtua.demo.expect.jakarta.messaging.service.unexpected;
}
