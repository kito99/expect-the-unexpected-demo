# Expect the Unexpected: Jakarta EE Demo

This is a small Jakarta EE 11 web application that goes along with the "Expect the Unexpected" talk about
exception handling. It takes the examples from the slides and runs them as real Contexts and Dependency
Injection (CDI) services behind a Jakarta REST endpoint, so you can watch each exception happen from a
browser or with curl.

A lot of the failures here are intentional. The goal is to show how different exception handling choices
look from the outside, so most requests end in an error on purpose. The failures this app raises itself go
through an exception mapper, so the caller gets the same JSON shape and a status that actually describes what
went wrong.

## Running the app

You'll need Java SE 21 or later. A [Java SE implementation](https://adoptium.net) from Adoptium works fine.
The project includes the [Maven Wrapper](https://maven.apache.org/wrapper/), so you don't need to install
Maven yourself.

Build the app and start it on Open Liberty with this command:

```
./mvnw clean package liberty:run
```

If you'd like code changes to reload automatically while you work, use Liberty's dev mode instead:

```
./mvnw liberty:dev
```

If you get a "Permission denied" error, either run `chmod +x mvnw` once or start the wrapper with
`sh ./mvnw`.

Once the server is ready, open [http://localhost:9080](http://localhost:9080).

## Using the app

The home page has two cards:

- **Send a message** posts whatever you type to the server. The field starts with `H!`, which is too short
  and fails. Try five or more characters of actual text to see a successful send. Blanks don't count,
  because the message is annotated `@NotBlank` as well as `@Size(min = 5)`.
- **Get a message** asks the server for the current message. The service is always outside its service
  window, so this always fails.

Both cards show the HTTP status and response body right on the page. To see the full story, keep an eye on
the server log too, because that's where the stack traces show up.

## REST API

The REST application is mounted at `/rest`.

| Method | Path             | Request           | Result                                                                                  |
|--------|------------------|-------------------|-----------------------------------------------------------------------------------------|
| `POST` | `/rest/messages` | `text/plain` body | 204 No Content when the message is non-blank and at least five characters, otherwise a 400 Bad Request |
| `GET`  | `/rest/messages` | none              | Returns the message as JSON, but right now always responds with a 503 Service Unavailable |

You can also try the endpoints with curl:

```
curl -i -X POST -H "Content-Type: text/plain" -d 'H!' http://localhost:9080/rest/messages
curl -i -X POST -H "Content-Type: text/plain" -d 'Hello there' http://localhost:9080/rest/messages
curl -i http://localhost:9080/rest/messages
```

## Error responses

Failures raised by this application come back in the same shape, so a client only has to learn one payload.

```json
{
  "code": "VALIDATION_FAILED",
  "status": 400,
  "message": "Request validation failed",
  "path": "/rest/messages",
  "errorId": "f10acf5b-61fe-4db2-a4de-578780b0d4f6",
  "timestamp": "2026-10-06T02:21:58.947036Z",
  "violations": [
    { "field": "text", "message": "size must be between 5 and 2147483647" }
  ]
}
```

`code` is an `ErrorCode`, an enum that names the kind of failure without knowing anything about HTTP. The
REST layer is the only place that turns a code into a status, so the service classes stay free of transport
details. `errorId` is a fresh identifier on every failure, and it shows up in both the response and the
matching server log entry, which makes it easy to tie a complaint from a caller back to a stack trace.
`violations` is empty unless Bean Validation rejected the request.

Errors the runtime produces before your code is reached, like a 404 for an unknown path or a 415 for the
wrong content type, don't pass through these mappers and keep whatever Liberty sends.

`ErrorMapping` in `tech.virtua.demo.expect.jakarta.error` holds both providers as nested classes:

- `ApplicationExceptionMapper` handles anything that extends `ApplicationException`. Each of those
  exceptions carries its own `ErrorCode`, so `OutsideServiceWindowException` becomes a 503 and
  `MessageFailureException` becomes a 500.
- `ValidationExceptionMapper` handles `ConstraintViolationException` and turns every constraint violation
  into a 400 with the offending field named.

They sit in one class because Jakarta REST matches a provider on its exception type, and a single class
can't implement `ExceptionMapper` twice with different type arguments. Nesting them keeps both mappers in
one file and lets them share the enclosing class's payload and logging code without a base class in the
middle.

### Picking the type a mapper matches

Jakarta REST chooses between mappers by how close the registered type is to the exception. Section 4.4 of
the specification says the runtime "MUST use the provider whose generic type is the nearest superclass of
the exception," and consults priority only when two candidates are equally close.

That matters here, because a rejected request doesn't produce a plain `ConstraintViolationException`. Open
Liberty hands you RESTEasy's subclass of it:

```
ResteasyViolationExceptionImpl -> ResteasyViolationException -> ConstraintViolationException
    -> ValidationException -> RuntimeException -> Exception -> Throwable
```

Open Liberty 26.0.0.9 running `webProfile-11.0` ships exactly two mappers of its own, both at the default
priority:

| Liberty's mapper                                                    | Registered for      |
|---------------------------------------------------------------------|---------------------|
| `org.jboss.resteasy.core.providerfactory.DefaultExceptionMapper`    | `Throwable`         |
| `org.jboss.resteasy.plugins.validation.ResteasyViolationExceptionMapper` | `ValidationException` |

Line those up against the hierarchy above and the winner is whichever one sits closest:

| Mapper                                               | Steps away | Result     |
|------------------------------------------------------|------------|------------|
| `ValidationExceptionMapper` on `ConstraintViolationException` | 2  | wins       |
| Liberty's, on `ValidationException`                  | 3          | loses      |
| anything on `Exception`                              | 5          | never runs |
| Liberty's, on `Throwable`                            | 6          | never runs |

`@Priority` is no way around this. A mapper registered for `Exception` at `@Priority(1)`, the highest value
there is, still loses to Liberty's `ValidationException` mapper, because distance is settled before priority
is looked at. Priority only breaks ties between mappers that are already equally close.

You can still override a built-in mapper. Register for the same type it uses and yours wins with no priority
annotation at all, because section 4.1.4 says "an application-supplied provider MUST always be preferred
over a pre-packaged one if a single one is required."

This app doesn't take that route, for two reasons. Matching `ConstraintViolationException` wins on distance
by itself, so it never leans on that tie-break. And `ValidationException` is also the parent of
`ConstraintDeclarationException`, `ConstraintDefinitionException`, and `NoProviderFoundException`, which all
mean the validation setup is broken rather than the request. Mapping those to a 400 would blame the caller
for a server bug.

Worth remembering that the count of two covers this feature set. Turn on other features and they can
contribute mappers of their own.

### Why not one mapper for everything

Registering a single mapper for `Exception` is tempting and costs more than it saves. `toResponse` would
receive an `Exception`, so `errorCode()` becomes unreachable without a cast and every failure collapses to
one status.

It also swallows `WebApplicationException`, which is what the runtime raises for an unknown path, an
unsupported media type, or a method that isn't allowed. Those stop being 404, 415, and 405, the 405 loses
its `Allow` header, and the bodies start handing implementation detail to whoever asked:

```
RESTEASY003210: Could not find resource for full path: http://localhost:9080/rest/nope
```

Branching back out of that takes a `WebApplicationException` passthrough and an `instanceof` check, which
only moves the runtime's own type dispatch into code you have to maintain.

`ErrorMapping` builds the payload and writes one log entry per failure. Server errors get logged at
`SEVERE` with the full stack trace. Client errors get logged at `WARNING` without one, since a rejected
request is the caller's mistake and its stack trace is just noise. That one entry is the deliberate contrast
to the `printStackTrace()` pile-up in `MessageWrapperServiceImpl`. Liberty's validator logs a warning of its
own alongside it, so the mapper is the only thing writing once, not the server as a whole.

One thing worth knowing if you adapt this code: `MessageWrapperServiceImpl` catches the validation failure
and wraps it in a `MessageFailureException`, so a 500 is what you'd expect to come back. You still get a 400
because RESTEasy, the Jakarta REST implementation in Open Liberty, inspects the cause chain of the wrapper
and re-raises the constraint violation before mapper selection ever happens. `ApplicationExceptionMapper`
never unwraps anything itself. That's a nice save here, but it comes from RESTEasy's validation integration
rather than anything the specification promises, so unwrap explicitly if you need the 400 to be portable.

## Logging

Open Liberty builds its logging on `java.util.logging`, so a plain `Logger.getLogger(...)` in application
code is all you need and there's no dependency to add. Liberty routes those records into `messages.log`
under `target/liberty/wlp/usr/servers/defaultServer/logs`.

The catch is that the console shows only `AUDIT` and above by default, so application `INFO` messages land
in `messages.log` and never reach the terminal. That's why `src/main/liberty/config/server.xml` sets the
level explicitly:

```xml
<logging consoleLogLevel="INFO" />
```

Liberty maps `SEVERE` to `E`, `WARNING` to `W`, and `INFO` to `I`. Everything below `INFO` is treated as
trace, `CONFIG` included, so it won't show up in `messages.log` at all. To see those levels, add a trace
specification and read `trace.log`:

```xml
<logging consoleLogLevel="INFO" traceSpecification="tech.virtua.demo.expect.*=finest" />
```

## Project layout

All of the code lives under `src/main/java/tech/virtua/demo/expect/jakarta`.

Some of the service methods are wrapped in `tag::` and `end::` comments. They're the same AsciiDoc tags used
in `src/examples/Examples.java`, which the slides include, so the code here stays easy to match up with the
slides.
