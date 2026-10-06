# Expect the Unexpected: Jakarta EE Demo

This is a small Jakarta EE 11 web application that goes along with the "Expect the Unexpected" talk about
exception handling. It takes the examples from the slides and runs them as real Contexts and Dependency
Injection (CDI) services behind a Jakarta REST endpoint, so you can watch each exception happen from a
browser or with curl.

A lot of the failures here are intentional. The goal is to show how different exception handling choices
look from the outside, so most requests end in an HTTP 500 Internal Server Error on purpose.

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
  and fails. Try something with at least five characters to see a successful send.
- **Get a message** asks the server for the current message. The service is always outside its service
  window, so this always fails.

Both cards show the HTTP status and response body right on the page. To see the full story, keep an eye on
the server log too, because that's where the stack traces show up.

## REST API

The REST application is mounted at `/rest`.

| Method | Path             | Request             | Result                                                                        |
|--------|------------------|---------------------|-------------------------------------------------------------------------------|
| `POST` | `/rest/messages` | `text/plain` body   | 204 No Content when the message has at least five characters, otherwise a 400 |
| `GET`  | `/rest/messages` | none                | Returns the message as JSON, but right now always responds with a 500         |

You can also try the endpoints with curl:

```
curl -i -X POST -H "Content-Type: text/plain" -d 'H!' http://localhost:9080/rest/messages
curl -i -X POST -H "Content-Type: text/plain" -d 'Hello there' http://localhost:9080/rest/messages
curl -i http://localhost:9080/rest/messages
```

## Project layout

All of the code lives under `src/main/java/tech/virtua/demo/expect/jakarta`.

Some of the service methods are wrapped in `tag::` and `end::` comments. They're the same AsciiDoc tags used
in `src/examples/Examples.java`, which the slides include, so the code here stays easy to match up with the
slides.
