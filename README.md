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

Please refer to [Key features](#key-features) for more information

#### Why the word "stubby"?
It is a stub HTTP server after all, hence the "stubby". Fun fact: in Australian slang "stubby" means _beer bottle_

## User manual for stubby4j v7.5.2-SNAPSHOT

### Table of contents

* [Advantages of using stubby4j HTTP stub server](#advantages-of-using-stubby4j-http-stub-server)
* [In the Press](#in-the-press)
* [Key features](#key-features)
* [Minimal system requirements](#minimal-system-requirements)
* [Quick start example](#quick-start-example)
* [Running in Docker](#running-in-docker)
   * [Docker Compose](#docker-compose)
* [Building](#building)
* [Logging](#logging)
* [Third-party dependencies](#third-party-dependencies)
* [Adding stubby4j to your project](#adding-stubby4j-to-your-project)
   * [Adding stubby4j SNAPSHOT versions to your project](#adding-stubby4j-snapshot-versions-to-your-project)
   * [Installing stubby4j to local .m2 repository](#installing-stubby4j-to-local-m2-repository)
* [Command-line switches](#command-line-switches)
* [Making requests over TLS](#making-requests-over-tls)
   * [Supported protocol versions](#supported-protocol-versions)
      * [TLS v1.3 support](#tls-v13-support)
   * [Server-side TLS configuration](#server-side-tls-configuration)
   * [Client-side TLS configuration](#client-side-tls-configuration)
      * [Server hostname verification by the client](#server-hostname-verification-by-the-client)
* [Support for HTTP/2 on HTTPS URIs over TLS](#support-for-http2-on-https-uris-over-tls)
* [WebSockets configuration HOWTO](#websockets-configuration-howto)
* [HTTP endpoint configuration HOWTO](#http-endpoint-configuration-howto)
* [Performance optimization index](#performance-optimization-index)
   * [Regex pattern precompilation](#regex-pattern-pre-compilation)
* [The admin portal](#the-admin-portal)
* [The stubs portal](#the-stubs-portal)
* [Change log](#change-log)
* [Authors](#authors)
* [Contributors](#contributors)
* [Copyright](#copyright)
* [License](#license)

## Advantages of using stubby4j HTTP stub server

There are a number of use cases where you'd want to use `WebSockets`, `HTTP/1.1`, `HTTP/2` stub server in your development/QA environment. If you are a `Software Engineer`/`Test Engneer`/`QA`, then it should hit close to home with you. As an example, some of these use cases are outlined below (this is by no means an exhaustive list). Use `stubby4j` when you want to:

* Simulate responses from a real server and don't care (or cannot) to send requests over the network
* Stub out external web services (when developing & testing locally) in a Docker based micro-service architecture
* Avoid negative productivity impact when an external API you depend on doesn't exist or isn't complete
* Simulate edge cases and/or failure modes for your web service that the real remote API won't reliably produce
* Fault injection, where after X good responses on the same URI your web service gets a bad one
* Verify that your application behaves as expected upon WebSocket server pushes
* Verify that your code makes HTTP/1.1 or HTTP/2 (over TLS) requests with all the required parameters and/or headers
* Verify that your code correctly handles HTTP response error codes

[Back to top](#table-of-contents)

## In the Press

* [https://alexanderzagniotov.medium.com/testing-microservices-in-docker-and-docker-compose-4dd54b02bd1c](https://alexanderzagniotov.medium.com/testing-microservices-in-docker-and-docker-compose-4dd54b02bd1c)
* [https://blog.solutotlv.com/integration-tests-fake-it-till-you-make-it/](https://blog.solutotlv.com/integration-tests-fake-it-till-you-make-it/)

[Back to top](#table-of-contents)


## Key features

* Dockerzied. Stub out external services in a Docker based micro-service architecture
* Support for `TLS` protocol versions `1.0`, `1.1`, `1.2` and `1.3`
* Support for `HTTP/2` over TCP (`h2c`) and `HTTP/2` over TLS (`h2`) on TLS v1.2 or newer using ALPN extension
* Support for `WebSocket` protocol over `HTTP/1.1` with `TLS` or not
* Fault injection, where after X good responses on the same URI you get a bad one
* Dynamic flows. Multiple stubbed responses on the same stubbed URI to test multiple application flows
* Request proxying. Ability to configure a proxy/intercept where requests are proxied to another service
* Record & Replay. The HTTP response recorded on the first call, having the subsequent calls play back the recorded HTTP response, without actually connecting to the external server
* `HTTP/1.1` or `HTTP/2` request verification and HTTP response stubbing
* `WebSocket` request verification, response stubbing, server push, and more
* Regex support for dynamic matching on URI, query params, headers, POST payload (ie:. `mod_rewrite` in Apache)
* Dynamic token replacement in stubbed response, by leveraging regex capturing groups as token values during HTTP request verification
* Serve binary files as stubbed response content (images, PDFs. etc.)
* Support for delayed responses for performance and stability testing
* Support for HTTP `30x` redirects verification
* Support for different types of HTTP Authorizations: `Basic`, `Bearer Token` & others
* Embed stubby4j to create a web service SANDBOX for your integration test suite

[Back to top](#table-of-contents)

## Minimal system requirements

### Running stubby4j as a standalone JAR

To run stubby4j standalone JARs which are hosted on [Maven Central][maven-link], the following are the minimal JRE requirements. Please note, there is no vendor specific requirement, anyone of the Oracle, OpenJDK, AdoptOpenJDK or Azul Zulu OpenJDK, will do.

| stubby4j version  | Version status            | Minimal JRE version   | Notes                                              |
| :---------------- | :------------------------ | :-------------------- | :------------------------------------------------- |
| `11.x.x`          | Work in progress          | `11`                  | Coming soon...                                     |
| `7.x.x`           | __Current & recommended__ | `1.8`                 | [v7.x.x on Maven Central][stubby4j-7-x-maven-link] |
| `6.x.x`           | Legacy                    | `1.8`                 | [v6.x.x on Maven Central][stubby4j-6-x-maven-link] |
| `5.x.x`           | Legacy                    | `1.8`                 | [v5.x.x on Maven Central][stubby4j-5-x-maven-link] |
| `4.x.x`           | Legacy                    | `1.8`                 | [v4.x.x on Maven Central][stubby4j-4-x-maven-link] |


### Running stubby4j as a pre-built Docker container

See [Running in Docker](#running-in-docker) for more information.

## Quick start example

This section explains how to get stubby4j up and running using a very simple example `Hello, World`, without building `stubby4j` from locally using `Gradle`.

#### Setup

* Download the [latest stubby4j version][maven-link] (the JAR archive).
* Create the following local YAML file: 
```yaml
-  request:
      method: GET
      url: /hello-world
 
   response:
      status: 200
      headers:
         content-type: application/json
      body: Hello World!
```
* Execute the downloaded stubby JAR using command `java -jar stubby4j-x.x.xx.jar -d <PATH_TO_LOCAL_YAML_CONFIG>` (also see [Command-line switches](#command-line-switches) and [Splitting main YAML config in to sub-includes](docs/http_endpoint_configuration_howto.md#splitting-main-yaml-config))
* Navigate to `http://localhost:8882/hello-world` to get the stubbed response `Hello World!`
* Navigate to stubby4j admin portal at `http://localhost:8889/status` to see what has been stubbed & other useful data

That's it!

For more information and more complex examples, please dive into the rest of documentation, especially [docs/HTTP endpoint configuration HOWTO.html](docs/http_endpoint_configuration_howto.md)

[Back to top](#table-of-contents)

## Running in Docker

stubby4j Docker images hosted on [https://hub.docker.com/r/azagniotov/stubby4j](https://hub.docker.com/r/azagniotov/stubby4j).

Alternatively you can build your own image locally using one of the project's `Dockerfile` under:
* [https://github.com/azagniotov/stubby4j/tree/master/docker/jdk8/Dockerfile](https://github.com/azagniotov/stubby4j/tree/master/docker/jdk8/Dockerfile)
* [https://github.com/azagniotov/stubby4j/tree/master/docker/jdk11/Dockerfile](https://github.com/azagniotov/stubby4j/tree/master/docker/jdk11/Dockerfile)
* [https://github.com/azagniotov/stubby4j/tree/master/docker/jdk16/Dockerfile](https://github.com/azagniotov/stubby4j/tree/master/docker/jdk16/Dockerfile)

Navigate to one of the above desired directory and run the following command to build from the `master` branch, e.g.:

```bash
$ docker build --rm --no-cache -t stubby4j:latest .
```

or the following command to build from a specific tag, e.g.:

```bash
$ docker build --build-arg REVISION=v7.5.1 --rm --no-cache -t stubby4j:7.5.1 .
```

Refer to [https://hub.docker.com/r/azagniotov/stubby4j](https://hub.docker.com/r/azagniotov/stubby4j) README `How to use this image` section regarding how to use the built image

[Back to top](#table-of-contents)

### Docker Compose

stubby4j Docker images hosted on [https://hub.docker.com/r/azagniotov/stubby4j](https://hub.docker.com/r/azagniotov/stubby4j).

Alternatively you can add stubby4j image to your stack using Docker Compose:

```yaml
# This compose file adds stubby4j https://hub.docker.com/r/azagniotov/stubby4j to your stack
#
# See "Environment variables" section at https://hub.docker.com/r/azagniotov/stubby4j
version: '3.5'
services:
  stubby4j-jre11:
    # 'root' - so that stubby4j can write 'logs' into host machine's directory mapped to container volume
    user: root
    image: azagniotov/stubby4j:latest-jre11
    volumes:
      - "./yaml:/home/stubby4j/data"
    container_name: stubby4j_jre11
    ports:
      - 8884:8884
      - 8891:8891
      - 7445:7445
    environment:
      YAML_CONFIG: smoke-tests-stubs.yaml
      LOCATION: 0.0.0.0
      STUBS_PORT: 8884
      ADMIN_PORT: 8891
      STUBS_TLS_PORT: 7445
      # https://github.com/azagniotov/stubby4j#command-line-switches
      WITH_ARGS: "--enable_tls_with_alpn_and_http_2 --debug --watch"
```

... where the `<HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>` is the host machine directory with the `stubby4j` YAML config file (see the `YAML_CONFIG` env var under [Environment variables](https://hub.docker.com/r/azagniotov/stubby4j)) that you want to map to the container volume `/home/stubby4j/data`

See smoke test [https://github.com/azagniotov/stubby4j/tree/master/docker/smoke-test/docker-compose.yml](https://github.com/azagniotov/stubby4j/tree/master/docker/smoke-test/docker-compose.yml) as a working example.


[Back to top](#table-of-contents)

## Building
stubby4j is a multi source-set Gradle `v7.2.0` project

Run `./gradlew` command to:
* Clean
* Run `unit`, `integration` and `functional` tests without `JaCoCo` code coverage
* Build (the generated JAR artifacts will be located under `<PROJECT_ROOT>/build/libs/`)

Run `./gradlew clean jacocoTestReport` command to:
* Clean
* Generate JaCoCo report under the `<PROJECT_ROOT>/build/reports/jacoco/html/index.html`

[Back to top](#table-of-contents)

## Logging

The `stubby4j` app emits STDOUT output stream and file-based logs (i.e.: generated by the `log4j2` library).

#### Running stubby4j as a standalone JAR

Only STDOUT output stream logs are available, unless the `--mute` command line argument was provided upon startup. See [Command-line switches](#command-line-switches)

#### Running stubby4j as an embedded JAR

When a logging framework implementation on the classpath, in addition to the STDOUT output stream logs, `stubby4j` also emits file-based logs through the SLF4J logging facade.

#### Running stubby4j in Docker container

Currently, only in the Docker images tagged as `latest-jreXX` and `7.3.3-jreXX` and higher (i.e.: `7.5.1-jreXX`), the `stubby4j` service emits file-based logs (i.e.: generated by log4j2 library) as well as STDOUT output stream logs. In images tagged as <= `7.3.2-jreXX`, only STDOUT output stream logs are available.

Please refer to [https://hub.docker.com/r/azagniotov/stubby4j](https://hub.docker.com/r/azagniotov/stubby4j) `Container application logs` section for more information

[Back to top](#table-of-contents)

## Third-party dependencies

* See the [build.gradle](https://github.com/azagniotov/stubby4j/blob/master/build.gradle) on [https://github.com/azagniotov/stubby4j](https://github.com/azagniotov/stubby4j)

[Back to top](#table-of-contents)

## Adding stubby4j to your project

The following are the stubby4j artifacts that are hosted on [Maven Central][maven-link]:

* `stubby4j-x.x.x.jar` - an `uber/fat` JAR containing all the 3rd-party deps
* `stubby4j-x.x.x-no-dependencies.jar` - a `skinny` JAR containing no 3rd-party dependencies at all
* `stubby4j-x.x.x-no-jetty.jar` - an `uber-ish` JAR containing all stubby4j's 3rd-party deps __except__ the Jetty binaries
* `stubby4j-x.x.x-sources.jar`
* `stubby4j-x.x.x-javadoc.jar`

#### Gradle

```groovy
api("io.github.azagniotov:stubby4j:7.5.1")
```
additionally, by adding a `classifier` to the JAR name like `no-dependencies` or `no-jetty`, i.e.:

```groovy
api("io.github.azagniotov:stubby4j:7.5.1:no-jetty")
```

#### Maven
```xml
<dependency>
    <groupId>io.github.azagniotov</groupId>
    <artifactId>stubby4j</artifactId>
    <version>7.5.1</version>
</dependency>
```
additionally, by adding a `classifier` to the JAR name like `no-dependencies` or `no-jetty`, i.e.:

```xml
<dependency>
    <groupId>io.github.azagniotov</groupId>
    <artifactId>stubby4j</artifactId>
    <version>7.5.1</version>
    <classifier>no-dependencies</classifier>
</dependency>
```

### Adding stubby4j SNAPSHOT versions to your project

stubby4j `SNAPSHOT` version contains the latest changes from the `master` branch. A snapshot version is built from every commit to the `master` branch and published to the OSS Sonatype snapshots repository. 

In order to pull down a `SNAPSHOT` version from Maven Central, please make sure that Sonatype's snapshot repository is configured in your project project build tool, e.g.: Gradle configuration:

```groovy
repositories {
    maven {
        url 'https://oss.sonatype.org/content/repositories/snapshots/'
    }
}
```

Now you can include stubby4j `SNAPSHOT` artifacts in your project:

```groovy
api("io.github.azagniotov:stubby4j:7.5.2-SNAPSHOT")
```
additionally, by adding a `classifier` to the JAR name like `no-dependencies`s or `no-jetty`, i.e.:

```groovy
api("io.github.azagniotov:stubby4j:7.5.2-SNAPSHOT:no-jetty")
```



### Installing stubby4j to local .m2 repository

Run `./gradlew clean build publishToMavenLocal` command to:

* Install `stubby4j-7.5.2-SNAPSHOT*.jar` to local `~/.m2/repository`
* All the artifacts will be installed under `~/.m2/repository/{groupId}/{artifactId}/{version}/`, e.g.: `~/.m2/repository/io/github/azagniotov/stubby4j/7.5.2-SNAPSHOT/`

Now you can include locally installed stubby4j `SNAPSHOT` artifacts in your project:
```groovy
api("io.github.azagniotov:stubby4j:7.5.2-SNAPSHOT")
```

additionally, by adding a `classifier` to the JAR name like `no-dependencies`s or `no-jetty`, i.e.:

```groovy
api("io.github.azagniotov:stubby4j:7.5.2-SNAPSHOT:no-jetty")
```

[Back to top](#table-of-contents)

## Command-line switches
```
usage:
java -jar stubby4j-x.x.xx.jar [-a <arg>] [-d <arg>] [-da] [-dc] [-ds] [-h]
       [-k <arg>] [-l <arg>] [-m] [-o] [-p <arg>] [-s <arg>] [-t <arg>]
       [-ta] [-v] [-w <arg>]
 -a,--admin <arg>                        Port for admin portal. Defaults
                                         to 8889.
 -d,--data <arg>                         Data file to pre-load endpoints.
                                         Data file to pre-load endpoints.
                                         Optional valid YAML 1.1 is
                                         expected. If YAML is not
                                         provided, you will be expected to
                                         configure stubs via the stubby4j
                                         HTTP POST API.
 -da,--disable_admin_portal              Does not start Admin portal
 -dc,--disable_stub_caching              Since v7.2.0. Disables stubs
                                         in-memory caching when stubs are
                                         successfully matched to the
                                         incoming HTTP requests
 -ds,--disable_ssl                       Disables TLS support (enabled by
                                         default) and disables the
                                         '--enable_tls_with_alpn_and_http_
                                         2' flag, if the latter was
                                         provided
 -h,--help                               This help text.
 -k,--keystore <arg>                     Keystore file for custom TLS. By
                                         default TLS is enabled using
                                         internal self-signed certificate.
 -l,--location <arg>                     Hostname at which to bind stubby.
 -m,--mute                               Mute console output.
 -o,--debug                              Dumps raw HTTP request to the
                                         console (if console is not
                                         muted!).
 -p,--password <arg>                     Password for the provided
                                         keystore file.
 -s,--stubs <arg>                        Port for stub portal. Defaults to
                                         8882.
 -t,--tls <arg>                          Port for TLS connection. Defaults
                                         to 7443.
 -ta,--enable_tls_with_alpn_and_http_2   Since v7.4.0. Enables HTTP/2 over
                                         TCP (h2c) and HTTP/2 over TLS
                                         (h2) on TLS v1.2 or newer using
                                         ALPN extension
 -v,--version                            Prints out to console stubby
                                         version.
 -w,--watch <arg>                        Since v2.0.11. Periodically scans
                                         for changes in last modification
                                         date of the main YAML and
                                         referenced external files (if
                                         any). The flag can accept an
                                         optional arg value which is the
                                         watch scan time in milliseconds.
                                         If milliseconds is not provided,
                                         the watch scans every 100ms. If
                                         last modification date changed
                                         since the last scan period, the
                                         stub configuration is reloaded
```

[Back to top](#table-of-contents)

## Making requests over TLS

When this section was written, as of November 2021, there are still enough legacy applications out there that have not
(or not able to) upgraded to the more secure, recommended and industry-standard TLS protocol versions v1.2 and/or its
successor v1.3. Therefore, in order to acommodate a range of integration testing needs, `stubby4j` continues to support
the legacy versions of TLS protocol.

Furthermore, the reader should be aware of that as part of continuous improvement of Java security, the industry continues
discourage use of  aforementioned protocols and in March 2021, the [RFC8996](https://datatracker.ietf.org/doc/html/rfc8996)
deprecating `TLS 1.0` (introduced in 1999) and `TLS 1.1` (introduced in 2006) was approved.

### Supported protocol versions

`stubby4j` can accept requests over most available versions of the SSL (Secure Sockets Layer) and its successor TLS
(Transport Layer Security) protocols. Supported versions are the legacy `SSLv3`, `TLSv1.0` and `TLSv1.1`, as well as
the current `TLSv1.2` and `TLSv1.3` (the TLS 1.3 standard was released in August 2018 and is a successor to TLS 1.2).

#### TLS v1.3 support

When running `stubby4j` as a standalone JAR, if the underlying JDK version supports `TLSv1.3`, then this protocol version
will also be supported and enabled in `stubby4j`. When `stubby4j` is [run from one of the pre-built Docker images](#running-in-docker),
the `TLSv1.3` is supported by default.

Please note, if you are running on JDK 1.8, it does not mean that your JDK build version & vendor necessarily support `TLSv1.3`.
For example:
- Oracle JDK 8 [added implementation for TLSv1.3 only in build v8u261](https://www.oracle.com/java/technologies/javase/8u261-relnotes.html) (which was disabled by default anyways)
- OpenJDK [released TLSv1.3 only in build v8u272](https://mail.openjdk.java.net/pipermail/jdk8u-dev/2020-October/012817.html)
- Azul Zulu included [support for TLSv1.3 through its OpenJSSE provider in Septmeber 2019](https://docs.azul.com/openjsse/Title.htm) ([https://github.com/openjsse/openjsse/issues/13](https://github.com/openjsse/openjsse/issues/13))

### Server-side TLS configuration

During TLS configuration in `stubby4j`, the following happens:

1. The property `jdk.tls.disabledAlgorithms` (located in `java.security` configuration file) is modified
   at runtime where the following values `SSLv3`, `TLSv1` and `TLSv1.1` are removed, in order to workaround
   the [JDK-8254713: Disable TLS 1.0 and 1.1](https://bugs.openjdk.java.net/browse/JDK-8254713)

2. The TLS in `stubby4j` is enabled by default using an internal, multi-hostname/IP self-signed certificate in `PKCS12` format
   imported into the server's key-store. See [OpenSSL config file](https://github.com/azagniotov/stubby4j/blob/38ec50844689a539dcdbe059edd4f1f7364801c3/src/main/resources/ssl/stubby4j.self.signed.v3.conf) used for the certificate generation, i.e.,: `stubby4j` is behaving as its own certificate authority.

   The default self-signed certificate can be overridden by supplying your own keystore/certificate (e.g.: generated from
   your own certificate signed by a certificate authority) when configuring `stubby4j` command-line arguments. In other words,
   this allows you to load top-level certificates from a root certificate authority. When providing a keystore file to `stubby4j`,
   the keystore should have `.PKCS12` or `.JKS` file extension. See [command-line switches](#command-line-switches) for more information.

### Client-side TLS configuration

Since `stubby4j`'s TLS layer configured (by default) using a self-signed certificate, it is not going to be possible
for web clients to validate `stubby4j`'s default self-signed certificate against clients' own trust-store containing a
list of trusted Certificate Authority (CA) certificates.

When TLS/SSL handshake happens, clients and servers exchange SSL certificates, cipher suite requirements, and randomly generated
data for creating session keys. As part of its "hello" reply to the client's "hello" message, the server sends a message
containing the server's SSL certificate (among other things like cipher suite and random string of bytes).

In other words, somehow, a web client making a request to `stubby4j` server over TLS has to ensure that it can trust
`stubby4j`'s self-signed certificate. There are a number of options available for web clients to achieve the trust between
the two parties during TLS/SSL handshake:

1. Configuring web client's X.509 trust strategy/manager to trust all certificates

   This is analogous to supplying the `-k` (or `--insecure`) option to cURL, which turns off cURL's verification of the server's
   certificate, e.g.:

   ```shell script
   $ curl -X GET --tls-max 1.0  https://localhost:7443/hello -v -k
   ```

   When a web client configures its own `SSLSocketFactory` (or `SSLContext`), the client can also configure its own
   X.509 certificate trust strategy/manager. This trust strategy/manager must be a _trust all_ (or a strategy/manager that trusts
   self-signed certificates). In this case, even when server responds with a self-signed certificate, the server's identity will
   be verified as valid. Please note, trusting _any_ certificate is very insecure and should not be used in production environments.

2. Providing stubby4j self-signed certificate to the web client before making requests over TLS

   This is analogous to supplying the `--cacert` option to cURL, which tells cURL to use the specified certificate file to verify
   the peer, e.g.:

   ```shell script
   $ curl -X GET --tls-max 1.0 https://localhost:7443/hello -v \
     --cacert src/main/resources/ssl/openssl.downloaded.stubby4j.self.signed.v3.pem
   ```

   If you __do not want__ to configure a _trust all_ X.509 manager/strategy for your web client, as an alternative it is
   possible to ensure that your web client already has `stubby4j`'s default self-signed certificate before making requests. In order
   to make web client to be aware of the self-signed certificate, you need to download and save the certificate from the running
   `stubby4j` server and then load it to the trust-store of your client when building `SSLSocketFactory` (or `SSLContext`).

   Please see the following [code of the HttpClientUtils in functional tests](https://github.com/azagniotov/stubby4j/blob/3319577b486ac691bd66841f100e0cfeb5dc3956/src/functional-test/java/io/github/azagniotov/stubby4j/HttpClientUtils.java#L80-L107) for the `openssl`, `keytool` commands & Java code examples.

   If you use a non-Java web client, you can use an already downloaded (via the `openssl s_client` command) stubby4j self-signed certificate in [PEM](https://github.com/azagniotov/stubby4j/tree/master/src/main/resources/ssl/openssl.downloaded.stubby4j.self.signed.v3.pem) format to load into
   your web client trust store.

   If your web client is a Java-based app, then you can load the aforementioned PEM certificate which was already converted in to two [JKS](https://github.com/azagniotov/stubby4j/tree/master/src/main/resources/ssl/openssl.downloaded.stubby4j.self.signed.v3.jks) and [PKCS12](https://github.com/azagniotov/stubby4j/tree/master/src/main/resources/ssl/openssl.downloaded.stubby4j.self.signed.v3.pkcs12) formats. You
   can use `JKS` or `PKCS12` certificate to load into your Java web client trust store.

   Alternatively, you can check all the certificates on the GitHub [https://github.com/azagniotov/stubby4j/tree/master/src/main/resources/ssl](https://github.com/azagniotov/stubby4j/tree/master/src/main/resources/ssl)

   #### Server hostname verification by the client

   During an SSL handshake, hostname verification establishes that the hostname in the URL matches the hostname in the server's identification; this
   verification is necessary to prevent man-in-the-middle attacks.

   If __(1)__ you imported `stubby4j` self-signed certificate into your web client trust store as per above and __(2)__ `stubby4j` app is running on [one of the following IPs or a localhost](https://github.com/azagniotov/stubby4j/tree/master/src/main/resources/ssl/stubby4j.self.signed.v3.conf#L45-L81) (alternatively, you can check the SSL conf on the GitHub [https://github.com/azagniotov/stubby4j/tree/master/src/main/resources/ssl](https://github.com/azagniotov/stubby4j/tree/master/src/main/resources/ssl)), then your web client will be able to successfully verify URL hostname of the request against the imported `stubby4j` certificate's SAN (subject alternative names) list.

   If you are running the `stubby4j` app on some other hostname/IP, then the hostname verification by your client will fail because the imported `stubby4j` self-signed certificate does not contain that hostname/IP. There are a number of options available for web clients to workaround the the hostname verification:

    1. Skip the hostname verification check or relax it (please note, skipping the hostname verification check is very insecure and should not be used in production environments)
    2. Make a pull request or raise an issue with a request asking me to add the hostname/IP into the SAN list of the `stubby4j` self-signed certificate ;)


If you have any questions about the TLS configuration in `stubby4j`, please feel free to [raise an issue](https://github.com/azagniotov/stubby4j/issues/new/choose).

[Back to top](#table-of-contents)


## Support for HTTP/2 on HTTPS URIs over TLS

Support for HTTP/2 on HTTPS URIs can be enabled by providing `--enable_tls_with_alpn_and_http_2` flag to the `stubby4j` JAR. See [command-line switches](#command-line-switches) for more information. The HTTP/2 support will be enabled only for the the TLS layer in stubby4j. In other words, web clients will not be able to make HTTP/2 requests to `http://....` URLs.

When the aforementioned flag is provided, the TLS in `stubby4j` is enabled with the ALPN ([RFC 7301](https://datatracker.ietf.org/doc/html/rfc7301)) extension. ALPN is the TLS extension that HTTP/2 is expected to use and the ALPN helps to negotiate the HTTP/2 between client & server without losing valuable time or network packet round-trips.

Please note the following restrictions when enabling HTTP/2 via the aforementioned flag:

1. As per [HTTP/2 RFC](https://datatracker.ietf.org/doc/html/rfc7540#section-9.2), the HTTP/2 over TLS in `stubby4j` will be enabled only for `TLSv1.2` or higher.
2. Web clients making HTTP/2 requests over TLS to `stubby4j` should be using ALPN TLS extension in their configuration to negotiate HTTP/2.
3. In `stubby4j`, the HTTP/2 over TLS will be enabled for JDK 1.8 (versions from 1.8.0_252 included and later) and higher.

[Back to top](#table-of-contents)

## WebSockets configuration HOWTO

See [docs/websockets_configuration_howto.html](docs/websockets_configuration_howto.md) for details

## HTTP endpoint configuration HOWTO

See [docs/http_endpoint_configuration_howto.html](docs/http_endpoint_configuration_howto.md) for details

## Performance optimization index

stubby4j uses a number of techniques to optimize evaluation of stubs

#### Regex pattern pre-compilation

During parsing of stubs config, the `request.url`, `request.query`, `request.headers` & `request.post` (or `request.file`)
values are checked for presence of regex. If one of the aforementioned properties is a stubbed regex, then a regex pattern
will be compiled & cached in memory. This way, the pattern(s) are compiled during config parsing, not stub evaluation.


[Back to top](#table-of-contents)

## The admin portal

See [docs/admin_portal.html](docs/admin_portal.md) for details


[Back to top](#table-of-contents)


## The stubs portal

Requests sent to any url at `localhost:8882` (or wherever you told stubby to run) will search through the available endpoints and, if a match is found, respond with that endpoint's `response` data

#### How endpoints are matched

For a given endpoint, stubby only cares about matching the properties of the request that have been defined in the YAML. The exception to this rule is `method`; if it is omitted it is defaulted to `GET`.

For instance, the following will match any `POST` request to the root url:

```yaml
-  request:
      url: /
      method: POST
   response: {}
```

The request could have any headers and any post body it wants. It will match the above.

Pseudocode ([StubRepository#matchStub](https://github.com/azagniotov/stubby4j/blob/75ae8e8ad7e0a75cf87ff710cfeff2d46a154096/src/main/java/io/github/azagniotov/stubby4j/stubs/StubRepository.java#L184)):

```javascript
    if (<incoming request>.hashCode found in <local cache>) {
        get <cached stubbed endpoint> from <local cache> by <incoming request>.hashCode
        return <cached stubbed endpoint>
    } else {
        for each <stubbed endpoint> of stored endpoints {
            for each <property> of <stubbed endpoint> {
                if (<stubbed endpoint>.<property> != <incoming request>.<property>) {
                    next stubbed endpoint
                }
            }
            store in <local cache> the found <stubbed endpoint> by hashCode

            return <stubbed endpoint>
        }
    }
```

[Back to top](#table-of-contents)

## Change log

See [https://github.com/azagniotov/stubby4j/blob/master/CHANGELOG.md](https://github.com/azagniotov/stubby4j/blob/master/CHANGELOG.md)

## Authors

See [docs/authors.html](docs/authors.md)

## Contributors

See [docs/contributors.html](docs/contributors.md)

## See also
* **[stubby4net](https://github.com/mrak/stubby4net):** A .NET implementation of stubby
* **[stubby4node](https://github.com/mrak/stubby4node):** A node.js implementation of stubby


## Copyright

See [docs/copyright.html](docs/copyright.md) for details

## License
MIT. See [https://github.com/azagniotov/stubby4j/blob/master/LICENSE](https://github.com/azagniotov/stubby4j/blob/master/LICENSE) for details.


[Back to top](#table-of-contents)

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
