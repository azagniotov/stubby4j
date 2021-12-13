HTTP/1.1, HTTP/2 and WebSockets API stub server for stubbing external systems in Docker and non-containerized environment for integration and contract testing

[![CircleCI build master branch][circleci-badge]][circleci-link]
[![DockerHub][docker-hub-badge]][docker-hub-link]
[![GitHubStars][stars-badge]][stars-link]
[![GitHubForks][forks-badge]][forks-link]
[![codecov][codecov-badge]][codecov-link]
[![Maven Central][maven-badge]][maven-link]
[![Stackoverflow stubby4j][stackoverflow-badge]][stackoverflow-link]

[![stubb4j][logo-badge]][logo-link]

It is a highly flexible and configurable tool for testing interactions of service-oriented (SoA) or/and micro-services architectures (REST, SOAP, WSDL, etc.) over `HTTP/1.1`, `HTTP/2` and `WebSockets` protocols.

## Full documentation

Full documentation hosted at [https://stubby4j.com](https://stubby4j.com), which includes a comprehensive user guide, various HOWTOs and usage examples.

## Key features

There are a number of use cases where you'd want to use `WebSockets`, `HTTP/1.1`, `HTTP/2` stub server in your development/QA environment. If you are a `Software Engineer`/`Test Engneer`/`QA`, then it should hit close to home with you. As an example, some of these use cases are outlined below (this is by no means an exhaustive list). Use `stubby4j` when you want to:

* Dockerzied. Stub out external services in a Docker based micro-service architecture
* Support for `TLS` protocol versions `1.0`, `1.1`, `1.2` and `1.3`
* Support for `HTTP/2` over TCP (`h2c`) and `HTTP/2` over TLS (`h2`) on TLS v1.2 or newer using ALPN extension
* Verify that your code makes `HTTP/1.1` or `HTTP/2` (over TLS) requests with all the required parameters and/or headers
* Support for `WebSocket` protocol over `HTTP/1.1` (with `TLS` and without) for request verification, response stubbing, server push, and more
* Fault injection, where after X good responses on the same URI you get a bad one
* Dynamic flows. Multiple stubbed responses on the same stubbed URI to test multiple application flows
* Request proxying. Ability to configure a proxy/intercept where requests are proxied to another service
* Record & Replay. The HTTP response recorded on the first call, having the subsequent calls play back the recorded HTTP response, without actually connecting to the external server
* Regex support for dynamic matching on URI, query params, headers, POST payload (ie:. `mod_rewrite` in Apache)
* Dynamic token replacement in stubbed response, by leveraging regex capturing groups as token values during HTTP request verification
* Serve binary files as stubbed response content (images, PDFs. etc.)
* Support for delayed responses for performance and stability testing
* Support for HTTP `30x` redirects verification
* Support for different types of HTTP Authorizations: `Basic`, `Bearer Token` & others
* Embed stubby4j to create a web service SANDBOX for your integration test suite
* Verify that your code correctly handles HTTP response error codes

Full documentation hosted at [https://stubby4j.com](https://stubby4j.com), which includes a comprehensive user guide, various HOWTOs and usage examples.

## Minimal system requirements

### Running stubby4j as a standalone JAR

See [https://stubby4j.com/#minimal-system-requirements](https://stubby4j.com/#minimal-system-requirements) for more information.

### Running stubby4j as a pre-built Docker container

See [https://stubby4j.com/#running-in-docker](https://stubby4j.com/#running-in-docker) for more information.

### Docker Compose

See [https://stubby4j.com/#docker-compose](https://stubby4j.com/#docker-compose) for more information.

Full documentation hosted at [https://stubby4j.com](https://stubby4j.com), which includes a comprehensive user guide, various HOWTOs and usage examples.


<!-- references -->

[circleci-badge]: https://circleci.com/gh/azagniotov/stubby4j.svg?style=shield
[circleci-link]: https://circleci.com/gh/azagniotov/stubby4j

[codecov-badge]: https://codecov.io/gh/azagniotov/stubby4j/branch/master/graph/badge.svg
[codecov-link]: https://codecov.io/gh/azagniotov/stubby4j

[maven-badge]: https://img.shields.io/maven-central/v/io.github.azagniotov/stubby4j.svg?style=flat&label=maven-central
[maven-link]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.azagniotov%22%20AND%20a%3A%22stubby4j%22
  
[stubby4j-7-x-maven-link]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.azagniotov%22%20AND%20a%3A%22stubby4j%20AND%20v%3A%227.*
[stubby4j-6-x-maven-link]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.azagniotov%22%20AND%20a%3A%22stubby4j%20AND%20v%3A%226.*
[stubby4j-5-x-maven-link]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.azagniotov%22%20AND%20a%3A%22stubby4j%20AND%20v%3A%225.*
[stubby4j-4-x-maven-link]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.azagniotov%22%20AND%20a%3A%22stubby4j%20AND%20v%3A%224.*

[stackoverflow-badge]: https://img.shields.io/badge/stackoverflow-stubby4j-brightgreen.svg?style=flat
[stackoverflow-link]: http://stackoverflow.com/questions/tagged/stubby4j

[chat-badge]: https://badges.gitter.im/Join%20Chat.svg
[chat-link]: https://gitter.im/stubby4j/Lobby

[license-badge]: https://img.shields.io/badge/license-MIT-blue.svg?style=flat
[license-link]: http://badges.mit-license.org

[docker-hub-badge]: https://img.shields.io/docker/pulls/azagniotov/stubby4j.svg?style=flat
[docker-hub-link]: https://hub.docker.com/r/azagniotov/stubby4j

[stars-badge]: https://img.shields.io/github/stars/azagniotov/stubby4j.svg?color=success
[stars-link]: https://github.com/azagniotov/stubby4j

[logo-badge]: https://cdn.rawgit.com/azagniotov/stubby4j/master/assets/stubby-logo-duke-hiding.svg
[logo-link]: https://github.com/azagniotov/stubby4j

[forks-badge]: https://img.shields.io/github/forks/azagniotov/stubby4j.svg
[forks-link]: https://github.com/azagniotov/stubby4j
