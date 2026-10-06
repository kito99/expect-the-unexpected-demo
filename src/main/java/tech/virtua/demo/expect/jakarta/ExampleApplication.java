package tech.virtua.demo.expect.jakarta;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("rest")
public class ExampleApplication extends Application {
    // Needed to enable Jakarta REST and specify path.
}
