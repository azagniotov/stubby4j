An HTTP server for stubbing external systems in both Docker and non-containerized environments for integration, contract and behavior testing.

[![CircleCI build master branch][circleci-badge]][circleci-link]
[![DockerHub][docker-hub-badge]][docker-hub-link]
[![GitHubStars][stars-badge]][stars-link]
[![GitHubForks][forks-badge]][forks-link]
[![codecov][codecov-badge]][codecov-link]
[![Maven Central][maven-badge]][maven-link]
[![Stackoverflow stubby4j][stackoverflow-badge]][stackoverflow-link]

[![stubb4j][logo-badge]][logo-link]

It is a highly flexible and configurable tool for testing interactions of service-oriented (SoA) or/and micro-services architectures (REST, SOAP, WSDL, etc.) over HTTP(s) protocol.

Please refer to [Key features](#key-features) for more information

#### Why the word "stubby"?
It is a stub HTTP server after all, hence the "stubby". Fun fact: in Australian slang "stubby" means _beer bottle_

## User manual for stubby4j v7.3.4-SNAPSHOT
### Table of contents

* [Advantages of using stubby4j HTTP stub server](#advantages-of-using-stubby4j-http-stub-server)
* [In the Press](#in-the-press)
* [Key features](#key-features)
* [Quick start example](#quick-start-example)
* [Running in Docker](#running-in-docker)
   * [Docker Compose](#docker-compose)
* [Building](#building)
* [Logging](#logging)
* [Third-party dependencies](#third-party-dependencies)
* [Adding stubby4j to your project](#adding-stubby4j-to-your-project)
   * [Installing stubby4j to local .m2 repository](#installing-stubby4j-to-local-m2-repository)
* [Command-line switches](#command-line-switches)
* [Endpoint configuration HOWTO](#endpoint-configuration-howto)
   * [Request](#request)
      * [Request object properties](#request-object-properties)
      * [Request proxying](#request-proxying)
      * [Regex stubbing for dynamic matching](#regex-stubbing-for-dynamic-matching)
      * [Regex stubbing for XML content](#regex-stubbing-for-xml-content)
      * [Authorization Header](#authorization-header)
   * [Response](#response)
      * [Response object properties](#response-object-properties)
      * [Dynamic token replacement in stubbed response](#dynamic-token-replacement-in-stubbed-response)
      * [Stubbing HTTP 30x redirects](#stubbing-http-30x-redirects)
      * [Record and Replay](#record-and-replay)
   * [Supplying stubbed endpoints to stubby](#supplying-stubbed-endpoints-to-stubby)
      * [Splitting main YAML config](#splitting-main-yaml-config)
* [Performance optimization index](#performance-optimization-index)
   * [Regex pattern precompilation](#regex-pattern-pre-compilation)
   * [Local caching of returning matched requests](#local-caching-of-returning-matched-requests)
* [The admin portal](#the-admin-portal)
   * The status page
   * Available REST API summary
      * Creating new/overwriting existing stubs & proxy configs
      * Listing existing stubs & proxy configs as YAML string
      * Updating existing stubs & proxy configs
      * Deleting existing stubs & proxy configs
      * POST / PUT request body format
   * Client programmatic API
* [The stubs portal](#the-stubs-portal)
* [Change log](#change-log)
* [Authors](#authors)
* [Contributors](#contributors)
* [Copyright](#copyright)
* [License](#license)

## Advantages of using stubby4j HTTP stub server

There are a number of use cases where you'd want to use HTTP stub server in your development/QA environment. If you are a `Software Engineer`/`Test Engneer`/`QA`, then it should hit close to home with you. As an example, some of these use cases are outlined below (this is by no means an exhaustive list). Use `stubby4j` when you want to:

* Simulate responses from a real server and don't care (or cannot) to send requests over the network
* Stub out external web services (when developing & testing locally) in a Docker based micro-service architecture
* Avoid negative productivity impact when an external API you depend on doesn't exist or isn't complete
* Simulate edge cases and/or failure modes for your web service that the real remote API won't reliably produce
* Fault injection, where after X good responses on the same URI your web service gets a bad one
* Verify that your code makes HTTP requests with all the required parameters and/or headers
* Verify that your code correctly handles HTTP response error codes

[Back to top](#table-of-contents)

## In the Press

* https://alexanderzagniotov.medium.com/testing-microservices-in-docker-and-docker-compose-4dd54b02bd1c
* https://blog.solutotlv.com/integration-tests-fake-it-till-you-make-it/

[Back to top](#table-of-contents)


## Key features

* Dockerzied. Stub out external services in a Docker based micro-service architecture
* Fault injection, where after X good responses on the same URI you get a bad one
* Dynamic flows. Multiple stubbed responses on the same stubbed URI to test multiple application flows
* Request proxying. Ability to configure a proxy/intercept where requests are proxied to another service
* Record & Replay. The HTTP response is recorded on the first call, having the subsequent calls play back the recorded HTTP response, without actually connecting to the external server
* HTTP request verification and HTTP response stubbing
* Regex support for dynamic matching on URI, query params, headers, POST payload (ie:. `mod_rewrite` in Apache)
* Dynamic token replacement in stubbed response, by leveraging regex capturing groups as token values during HTTP request verification
* Serve binary files as stubbed response content (images, PDFs. etc.)
* Support for delayed responses for performance and stability testing
* Support for HTTP 30x redirects verification
* Support for different types of HTTP Authorizations: Basic, Bearer Token & others
* Embed stubby4j to create a web service SANDBOX for your integration test suite

[Back to top](#table-of-contents)


## Quick start example

This section explains how to get stubby4j up and running using a very simple example `Hello, World`, without building `stubby4j` from locally using `Gradle`.

#### Minimum system requirements to run stubby4j archives hosted on [Maven Central][maven-link]

* version >= `4.0.0`: JRE >= `v1.8`
* version >= `3.0.0`: JRE >= `v1.7`

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
* Execute the downloaded stubby JAR using command `java -jar stubby4j-x.x.xx.jar -d <PATH_TO_LOCAL_YAML_CONFIG>` (also see [Command-line switches](#command-line-switches) and [Splitting main YAML config in to sub-includes](#splitting-main-yaml-config))
* Navigate to `http://localhost:8882/hello-world` to get the stubbed response `Hello World!`
* Navigate to stubby4j admin portal at `http://localhost:8889/status` to see what has been stubbed & other useful data

That's it!

For more information and more complex examples, please dive into the rest of documentation, especially [Endpoint configuration HOWTO](#endpoint-configuration-howto)

[Back to top](#table-of-contents)

## Running in Docker

stubby4j Docker images are hosted on https://hub.docker.com/r/azagniotov/stubby4j.

Alternatively you can build your own image locally using one of the project's `Dockerfile` under:
* [docker/jdk8](docker/jdk8)
* [docker/jdk11](docker/jdk11)
* [docker/jdk15](docker/jdk15)

Navigate to one of the above desired directory and run the following command to build from the `master` branch, e.g.:

```shell script
$ docker build --rm --no-cache -t stubby4j:latest .
```

or the following command to build from a specific tag, e.g.:

```shell script
$ docker build --build-arg REVISION=v7.3.3 --rm --no-cache -t stubby4j:latest .
```

Refer to https://hub.docker.com/r/azagniotov/stubby4j README `How to use this image` section regarding how to use the built image

[Back to top](#table-of-contents)

### Docker Compose

stubby4j Docker images are hosted on https://hub.docker.com/r/azagniotov/stubby4j.

Alternatively you can add stubby4j image to your stack using Docker Compose:

```yaml
# This compose file adds stubby4j https://hub.docker.com/r/azagniotov/stubby4j to your stack
#
# See "Environment variables" section at https://hub.docker.com/r/azagniotov/stubby4j
version: '3.5'
services:
  stubby4j:
    image: azagniotov/stubby4j:latest-jre8 # you can also use other tags: latest-jre11, latest-jre15
    volumes:
      - "<HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>:/home/stubby4j/data"
    container_name: stubby4j
    ports:
      - 8882:8882
      - 8889:8889
      - 7443:7443
    environment:
      YAML_CONFIG: stubs.yaml
      STUBS_PORT: 8882
      ADMIN_PORT: 8889
      STUBS_TLS_PORT: 7443
      WITH_DEBUG: --debug
      WITH_WATCH: --watch
```

... where the `<HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>` is the host machine directory with the `stubby4j` YAML config file (see the `YAML_CONFIG` env var under [Environment variables](https://hub.docker.com/r/azagniotov/stubby4j)) that you want to map to the container volume `/home/stubby4j/data`

See [docker-compose.yml](docker/docker-compose.yml)


[Back to top](#table-of-contents)

## Building
stubby4j is a multi source-set Gradle `v6.8.3` project

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

Currently, only in the Docker images tagged as `latest-jreXX`, the `stubby4j` service emits file-based logs (i.e.: generated by log4j2 library) as well as STDOUT output stream logs. In images tagged as `7.x.x-jreXX`, only STDOUT output stream logs are available.

Please refer to https://hub.docker.com/r/azagniotov/stubby4j `Container application logs` section for more information

[Back to top](#table-of-contents)

## Third-party dependencies

* See the [build.gradle](build.gradle)

[Back to top](#table-of-contents)

## Adding stubby4j to your project
The following are the stubby4j artifacts that are hosted on [Maven Central][maven-link]:

* `stubby4j-x.x.x.jar` - an `uber/fat` JAR containing all the 3rd-party deps
* `stubby4j-x.x.x-no-dependencies.jar` - a `skinny` JAR containing no 3rd-party dependencies at all
* `stubby4j-x.x.x-no-jetty.jar` - an `uber-ish` JAR containing all the 3rd-party deps __except__ Jetty binaries
* `stubby4j-x.x.x-sources.jar`
* `stubby4j-x.x.x-javadoc.jar`

#### Gradle
```xml
compile("io.github.azagniotov:stubby4j:7.3.3")
```
or by adding a `classifier` to the JAR name like `no-dependencies` or `no-jetty`, i.e.:

```xml
compile("io.github.azagniotov:stubby4j:7.3.3:no-jetty")
```

#### Maven
```xml
<dependency>
    <groupId>io.github.azagniotov</groupId>
    <artifactId>stubby4j</artifactId>
    <version>7.3.3</version>
</dependency>
```
or by adding a `classifier` to the JAR name like `no-dependencies` or `no-jetty`, i.e.:

```xml
<dependency>
    <groupId>io.github.azagniotov</groupId>
    <artifactId>stubby4j</artifactId>
    <version>7.3.3</version>
    <classifier>no-dependencies</classifier>
</dependency>
```

### Installing stubby4j to local .m2 repository

Run `./gradlew installToMavenLocal` command to:

* Install `stubby4j-7.3.4-SNAPSHOT*.jar` to local `~/.m2/repository`
* All the artifacts will be installed under `~/.m2/repository/{groupId}/{artifactId}/{version}/`, e.g.: `~/.m2/repository/io/github/azagniotov/stubby4j/7.3.3-SNAPSHOT/`

Now you can include locally installed stubby4j `SNAPSHOT` artifacts in your project:
```xml
compile("io.github.azagniotov:stubby4j:7.3.3-SNAPSHOT")
```
or by adding a `classifier` to the JAR name like `no-dependencie`s or `no-jetty`, i.e.:

```xml
compile("io.github.azagniotov:stubby4j:7.3.3-SNAPSHOT:no-jetty")
```

[Back to top](#table-of-contents)

## Command-line switches
```
usage:
java -jar stubby4j-x.x.xx.jar [-a <arg>] [-d <arg>] [-da] [-dc] [-ds] [-h]
       [-k <arg>] [-l <arg>] [-m] [-o] [-p <arg>] [-s <arg>] [-t <arg>]
       [-v] [-w <arg>]
 -a,--admin <arg>             Port for admin portal. Defaults to 8889.
 -d,--data <arg>              Data file to pre-load endpoints. Data file
                              to pre-load endpoints. Optional valid YAML
                              1.1 is expected. If YAML is not provided,
                              you will be expected to configure stubs via
                              the stubby4j HTTP POST API.
 -da,--disable_admin_portal   Does not start Admin portal
 -dc,--disable_stub_caching   Disables stubs in-memory caching when stubs
                              are successfully matched to the incoming
                              HTTP requests
 -ds,--disable_ssl            Does not enable SSL connections
 -h,--help                    This help text.
 -k,--keystore <arg>          Keystore file for custom TLS. By default TLS
                              is enabled using internal keystore.
 -l,--location <arg>          Hostname at which to bind stubby.
 -m,--mute                    Mute console output.
 -o,--debug                   Dumps raw HTTP request to the console (if
                              console is not muted!).
 -p,--password <arg>          Password for the provided keystore file.
 -s,--stubs <arg>             Port for stub portal. Defaults to 8882.
 -t,--tls <arg>               Port for TLS connection. Defaults to 7443.
 -v,--version                 Prints out to console stubby version.
 -w,--watch <arg>             Periodically scans for changes in last
                              modification date of the main YAML and
                              referenced external files (if any). The flag
                              can accept an optional arg value which is
                              the watch scan time in milliseconds. If
                              milliseconds is not provided, the watch
                              scans every 100ms. If last modification date
                              changed since the last scan period, the stub
                              configuration is reloaded
```

[Back to top](#table-of-contents)


## Endpoint configuration HOWTO

This section explains the usage, intent and behavior of each property on the `request` and `response` objects.

Also, you will learn about request proxying to other hosts, regex stubbing for dynamic matching, stubbing HTTP 30x redirects, record-and-replay and more.

<details>
  <summary><code>Click to expand</code></summary>
 <br />
  
Here is a fully-populated, unrealistic endpoint:
```yaml
-  description: Optional description shown in logs
   uuid: fdkfsd8f8ds7f
   request:
      url: ^/your/awesome/endpoint$
      method: POST
      query:
         exclamation: post requests can have query strings!
      headers:
         content-type: application/xml
      post: >
         <!xml blah="blah blah blah">
         <envelope>
            <unaryTag/>
         </envelope>
      file: tryMyFirst.xml

   response:
      status: 200
      latency: 5000
      headers:
         content-type: application/xml
         server: stubbedServer/4.2
      body: >
         <!xml blah="blah blah blah">
         <responseXML>
            <content></content>
         </responseXML>
      file: responseData.xml
```

[Back to top](#table-of-contents)

## Stub/Feature

#### description (`optional`)

* Description field which can be used to show optional descriptions in the logs
* Useful when you have a number of stubs loaded for the same endpoint and it starts to get confusing as to which is being matched

```yaml
-  description: Stub one
   request:
      url: ^/one$
      method: GET

   response:
      status: 200
      latency: 100
      body: 'One!'

-  description: Stub two
   request:
      url: ^/two$
      method: GET

   response:
      status: 200
      latency: 100
      body: 'Two!'

-  request:
      url: ^/three$
      method: GET

   response:
      status: 200
      latency: 100
      body: 'Three!'
```

#### uuid (`optional`)

* Useful when you want to specify unique identifier so it would be easier to update/delete it at runtime

```yaml
-  uuid: 9136d8b7-f7a7-478d-97a5-53292484aaf6
   request:
      method: GET
      url: /with/configured/uuid/property

   response:
      headers:
         content-type: application/json
      status: 200
      body: >
         {"status" : "OK"}
```

</details>

[Back to top](#table-of-contents)


## Request

This object is used to match an incoming request to stubby against the available endpoints that have been configured.

### Summary

In YAML config, the `request` object supports the following properties:

`url`, `method`, `query`, `headers`, `post`, `file`

Keep on reading to understand their usage, intent and behavior.

### Request object properties

<details>
  <summary><code>Click to expand</code></summary>

#### url (`required`)

* is a full-fledged __regular expression__
* This is the only required property of an endpoint.
* signify the url after the base host and port (i.e. after `localhost:8882`).
* must begin with ` / `.
* any query paramters are stripped (so don't include them, that's what `query` is for).
    * `/url?some=value&another=value` becomes `/url`
* no checking is done for URI-encoding compliance.
    * If it's invalid, it won't ever trigger a match.

This is the simplest you can get:
```yaml
-  request:
      url: /
```

A demonstration when not using regular expressions:
```yaml
-  request:
      url: /some/resource/that/will/be/fully/matched
```

A demonstration using regular expressions:
```yaml
-  request:
      url: ^/has/to/begin/with/this/

-  request:
      url: /has/to/end/with/this/$

-  request:
      url: ^/must/be/this/exactly/with/optional/trailing/slash/?$

-  request:
      url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$
```

#### method (`required`)

* defaults to `GET`.
* case-insensitive.
* can be any of the following:
    * HEAD
    * GET
    * POST
    * PUT
    * POST
    * DELETE
    * PATCH
    * etc.

```yaml
-  request:
      url: /anything
      method: GET
```

* it can also be an array of values.

```yaml
-  request:
      url: /anything
      method: [GET, HEAD]

-  request:
      url: /anything
      method:
         -  GET
         -  HEAD
```

#### query (`optional`)

* can be a full-fledged __regular expression__
* if not stubbed, stubby ignores query parameters on incoming request and will match only request URL
* stubby accommodates for HTTP requests that contain query string params with no values
* query params can be specified regardless of their order in incoming request. In other words - order agnostic
* query params can also be an array with double/single quoted/un-quoted elements: ```attributes=["id","uuid"]``` or ```attributes=[id,uuid]```. Please note no spaces between the CSV

```yaml
-  request:
      method: GET
      url: ^/with/parameters$
      query:
         type_name: user
         client_id: id
         client_secret: secret
         random_id: "^sequence/-/\\d/"
         session_id: "^user_\\d{32}_local"
         attributes: '["id","uuid","created","lastUpdated","displayName","email","givenName","familyName"]'

```

* The following will match either of these:
    * `/with/parameters?search=search+terms&filter=month`
    * `/with/parameters?filter=month&search=search+terms`

```yaml
-  request:
      url: ^/with/parameters$
      query:
         search: search terms
         filter: month
```

* The following will match either of these:
    * `/with/parameters?search&filter=month`
    * `/with/parameters?search=&filter=month`

```yaml
-  request:
      url: ^/with/parameters$
      query:
         search:
         filter: month
```

* The following will match:
    * From the browser: `http://localhost:8882/with/parameters?term=boo+and+foo`
    * From the browser: `http://localhost:8882/with/parameters?term=boo%2Band%2Bfoo`
    * From the browser: `http://localhost:8882/with/parameters?term=boo  and   foo`
    * From the browser: `http://localhost:8882/with/parameters?term=boo%20and%20foo`
    * From the code: `String request = "http://localhost:8882/with/parameters?term=boo+and+foo"`
    * From the code: `String request = "http://localhost:8882/with/parameters?term=boo%2Band%2Bfoo"`
    * From the code: `String request = "http://localhost:8882/with/parameters?term=boo and foo"`
    * From the code: `String request = "http://localhost:8882/with/parameters?term=boo%20and%20foo"`

```yaml
-  request:
      url: ^/with/parameters$
      query:
         term: "boo and foo"
```

* The following will match:
    * From the browser: `http://localhost:8882/with/parameters?term=['stalin+and+truman']`
    * From the browser: `http://localhost:8882/with/parameters?term=['stalin+and+++++++++++truman']`
    * From the browser: `http://localhost:8882/with/parameters?term=['stalin%2Band%2Btruman']`
    * From the browser: `http://localhost:8882/with/parameters?term=['stalin and    truman']`
    * From the browser: `http://localhost:8882/with/parameters?term=['stalin%20and%20truman']`
    * From the code: `String request = "http://localhost:8882/with/parameters?term=%5B%27stalin%2Band%2Btruman%27%5D"`
    * From the code: `String request = "http://localhost:8882/with/parameters?term=%5B%27stalin+++++and+truman%27%5D"`
    * From the code: `String request = "http://localhost:8882/with/parameters?term=%5B%27stalin and   truman%27%5D"`
    * From the code: `String request = "http://localhost:8882/with/parameters?term=%5B%27stalin%20and%20truman%27%5D"`

```yaml
-  request:
      url: ^/with/parameters$
      query:
         term: "['stalin and truman']"
```

#### post (`optional`)

* Represents the body POST of incoming request, ie.: form data
* can be a full-fledged __regular expression__
* if not stubbed, any POSTed data on incoming request is ignored

```yaml
-  request:
      url: ^/post/form/data$
      post: name=John&email=john@example.com
```

```yaml
-  request:
      method: [POST]
      url: /uri/with/post/regex
      post: "^[\\.,'a-zA-Z\\s+]*$"
```

```yaml
-  request:
      url: ^/post/form/data$
      post: "^this/is/\\d/post/body"
```

```yaml
-  request:
      method: POST
      url: /post-body-as-json
      headers:
         content-type: application/json
      post: >
         {"userId":"19","requestId":"(.*)","transactionDate":"(.*)","transactionTime":"(.*)"}

   response:
      headers:
         content-type: application/json
      status: 200
      body: >
         {"requestId": "<%post.1%>", "transactionDate": "<%post.2%>", "transactionTime": "<%post.3%>"}
```

```yaml
-  request:
      method: POST
      url: /post-body-as-json-2
      headers:
         content-type: application/json
      post: >
         {"objects": [{"key": "value"}, {"key": "value"}, {"key": {"key": "(.*)"}}]}

   response:
      headers:
         content-type: application/json
      status: 200
      body: >
         {"internalKey": "<%post.1%>"}
```

#### file (`optional`)

* holds a path to a local file (it can be an `absolute` or `relative` path to the main YAML specified in `-d` or `--data`). This property allows you to split up stubby data across multiple files instead of making one huge bloated main config YAML. For example, let's say you want to stub a big POST payload, so instead of dumping a lot of text under the `post` property, you could specify a local file with the payload using the `file` property:

```yaml
-  request:
      method: POST
      headers:
         content-type: application/json
      file: ../json/post.payload.json
```

* please note, if both `file` & `post` properties are supplied, the `file` takes precedence & replaces `post` with the contents from the provided file. 
* if `--watch` command-line argument was supplied during startup, then any modifications to the supplied local file in `file` (e.g. `file: ../json/post.payload.json`) will cause the whole configuration to be reloaded.
* if the local file could not be loaded for whatever reason (ie.: not found), stubby falls back to `post` for matching.
* please keep in mind: ```SnakeYAML``` library (used by stubby4j) parser ruins multi-line strings by not preserving system line breaks. If `file` property is stubbed, the file content is loaded as-is, in other words - it does not go through SnakeYAML parser. Therefore it's better to load big POST content for request using `file` property. Keep in mind, stubby4j stub server is dumb and does not use smart matching mechanism (i.e.: don't match line separators or don't match any white space characters) - whatever you stubbed, must be POSTed exactly for successful match. Alternatively you can consider using regular expression in `post`

```yaml
-  request:
      url: ^/match/against/file$
      file: postedData.json
      post: '{"fallback":"data"}'
```

postedData.json
```json
{"fileContents":"match against this if the file is here"}
```

* if `postedData.json` doesn't exist on the filesystem when `/match/against/file` is matched in incoming request, stubby will match post contents against `{"fallback":"data"}` (from `post`) instead.

#### headers (`optional`)

* can be a full-fledged __regular expression__
* if not stubbed, stubby ignores headers on incoming request and will match only request URL
* if stubbed, stubby will try to match __only__ the supplied headers and will ignore other headers of incoming request. In other words, the incoming request __must__ contain stubbed header values
* headers are case-insensitive during matching
* a hashmap of header/value pairs similar to `query`.

The following endpoint only accepts requests with `application/json` post values:

```yaml
-  request:
      url: /post/json
      method: post
      headers:
         content-type: application/json
         x-custom-header: "^this/is/\d/test"
         x-custom-header-2: "^[a-z]{4}_\\d{32}_(local|remote)"
```


</details>

[Back to top](#table-of-contents)


### Request proxying

See [docs/REQUEST_PROXYING.md](docs/REQUEST_PROXYING.md) for details

### Regex stubbing for dynamic matching

stubby supports regex stubbing for dynamic matching on the following properties:
- `request` `url`
- `request` `query` param values
- `request` `header` name values
- `request` `post` payloads
- `request` `file` names & payloads.

Under the hood, stubby first attempts to compile the stubbed pattern into an instance of `java.util.regex.Pattern` class using the `Pattern.MULTILINE` flag. If the pattern compilation fails and `PatternSyntaxException` exception is thrown, stubby compiles the stubbed pattern into an instance of `java.util.regex.Pattern` class using the `Pattern.LITERAL | Pattern.MULTILINE` flags.

__Please note__, before using regex patterns in stubs, first it is best to ensure that the desired regex pattern "works" outside of stubby. One of the safest (and easiest) ways to test the desired pattern would be to check if the following condition is met: `Pattern.compile("YOUR_PATTERN").matcher("YOUR_TEST_STRING").matches() == true`. 

The latter would ensure that the stubbed regex pattern actually works, also it is easier to debug a simple unit test case instead of trying to figure out why stub matching failed

[Back to top](#table-of-contents)

### Regex stubbing for XML content

XML is not a regular language, it can be tricky to parse it using a regular expression (well, sometimes it is not as tricky when XML regex snippet is simple. But, most of the times this will cause you tears), especially when dealing with large XML `POST` payloads. XML is very complex: nested tags, XML comments, CDATA sections, preprocessor directives, namespaces, etc. make it very difficult to create a parse-able & working regular expression.

<details>
 <summary><code>Click to expand</code></summary>
 <br />

Therefore, `stubby4j` uses under the hood a full-fledged 3rd party XML parser - [XMLUnit](https://github.com/xmlunit/xmlunit).

XMLUnit enables stubbing of XML content with regular expressions by leveraging XMLUnit-specific Regex match placeholders. Placeholders are used to specify exceptional requirements in the control XML document for use during equality comparison (i.e.: regex matching).

#### How to stub XML containing regular expressions?

1. Using vanilla regular expressions
2. Using [XMLUnit](https://github.com/xmlunit/xmlunit) regular expression matcher placeholders

##### Using vanilla regular expressions

Consider the following examples of stubbed `request` that have XML regex snippets under `post`:

```yaml
- description: rule_1
  request:
    url: /some/resource/uri
    method: POST
    headers:
      content-type: application/xml
    post: >
      <\?xml version="1.0" encoding="UTF-8" standalone="yes"\?><idex:type xmlns:idex="http://idex.bbc.co.uk/v1"><idex:authority>(.*)</idex:authority><idex:name>(.*)</idex:name><idex:startsWith>(.*)</idex:startsWith></idex:type>
```

In the above example, do note that the `?` in `<?xml .. ?>` are escaped (i.e.: `<\?xml .. \?>`) as these are regex specific characters.

```yaml
- description: rule_1
  request:
    url: /some/resource/uri
    method: POST
    headers:
      content-type: application/xml
    post: >
      <\?xml version="1.0" encoding="UTF-8"\?>
      <person xmlns="http://www.your.example.com/xml/person">
          <VocabularyElement id="urn:epc:idpat:sgtin:(.*)">
              <attribute id="urn:epcglobal:product:drugName">(.*)</attribute>
              <attribute id="urn:epcglobal:product:manufacturer">(.*)</attribute>
              <attribute id="urn:epcglobal:product:dosageForm">(.*)</attribute>
              <attribute id="urn:epcglobal:product:strength">(.*)</attribute>
              <attribute id="urn:epcglobal:product:containerSize">(.*)</attribute>
          </VocabularyElement>
          <name>(.*)</name>
          <age>(.*)</age>
          <!--
            Hello,
               I am a multi-line XML comment
               <staticText>
                  <reportElement x="180" y="0" width="200" height="20"/>
                  <text><!\[CDATA\[(.*)\]\]></text>
                </staticText>
            -->
          <homecity xmlns="(.*)cities">
              <long>(.*)</long>
              <lat>(.*)</lat>
              <name>(.*)</name>
          </homecity>
          <one name="(.*)" id="urn:company:namespace:type:id:one">(.*)</one>
          <two id="urn:company:namespace:type:id:two" name="(.*)">(.*)</two>
          <three name="(.*)" id="urn:company:namespace:type:id:(.*)">(.*)</three>
      </person>
```

In the above example, do note that the:
1. `?` in `<?xml .. ?>` are escaped (i.e.: `<\?xml .. \?>`) as these are regex specific characters, and
2. `[` and `]` in `<![CDATA[ .. ]]>` are escaped (i.e.: `<!\[CDATA\[(.*)\]\]>`) as these are regex specific characters

##### Using XMLUnit regular expression matcher placeholders

XMLUnit placeholder `${xmlunit.matchesRegex( ... )}` to the rescue. Consider the following example of stubbed `request`:

```yaml
- description: rule_1
  request:
    url: /some/resource/uri
    method: POST
    headers:
      content-type: application/xml
    post: >
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <idex:type xmlns:idex="http://idex.bbc.co.uk/v1">
          <idex:authority>${xmlunit.matchesRegex(.*)}</idex:authority>
          <idex:name>${xmlunit.matchesRegex(.*)}</idex:name>
          <idex:startsWith>${xmlunit.matchesRegex(.*)}</idex:startsWith>
      </idex:type>
```
In the above example, the regular expressions defined in `post` XML will match any values inside `idex:authority`, `idex:name` and `idex:startsWith` elements.

Please refer to the following XMLUnit [Placeholders](https://github.com/xmlunit/user-guide/wiki/Placeholders) guide or/and [their unit tests](https://github.com/xmlunit/xmlunit/blob/1c25e0171123b1a1fc543c87c5a9039d850d9b73/xmlunit-placeholders/src/test/java/org/xmlunit/placeholder/PlaceholderDifferenceEvaluatorTest.java) for more information.

</details>

[Back to top](#table-of-contents)

### Authorization Header

```yaml
-  request:
      url: ^/path/to/basic$
      method: GET
      headers:
         # no "Basic" prefix nor explicit encoding in Base64 is required when stubbing,
         # just plain username:password format. Stubby internally encodes the value in Base64
         authorization-basic: "bob:password" 
   response:
      headers:
         Content-Type: application/json
      status: 200
      body: Your request with Basic was successfully authorized!

-  request:
      url: ^/path/to/bearer$
      method: GET
      headers:
         # no "Bearer" prefix is required when stubbing, only the auth value.
         # Stubby internally does not modify (encodes) the auth value
         authorization-bearer: "YNZmIzI2Ts0Q=="
   response:
      headers:
         Content-Type: application/json
      status: 200
      body: Your request with Bearer was successfully authorized!

-  request:
      url: ^/path/to/custom$
      method: GET
      headers:
         # custom prefix name is required when stubbing, followed by space & auth value.
         # Stubby internally does not modify (encodes) the auth value
         authorization-custom: "CustomAuthorizationType YNZmIzI2Ts0Q=="
   response:
      headers:
         Content-Type: application/json
      status: 200
      body: Your request with custom authorization type was successfully authorized!
```

[Back to top](#table-of-contents)


## Response

Assuming a match between the incoming HTTP request and one of the stubbed `request` objects has been found, its stubbed `response` object properties are used to build the HTTP response back to the client.

### Summary

In YAML config, the `response` object supports the following properties:

`status`, `body`, `file`, `headers`, `latency`

Keep on reading to understand their usage, intent and behavior.

### Response object properties


<details>
 <summary><code>Click to expand</code></summary>
 <br />
  
* Response configuration can be a single `response` or a sequence of `response`s under the same `- request` definition.
* When sequenced responses is configured, on each incoming request to the same URI, a subsequent response in the list will be sent to the client. The sequenced responses play in a cycle (loop). In other words: after the response sequence plays through, the cycle restarts on the next incoming request.

```yaml
-  request:
      method: [GET,POST]
      url: /invoice/123

   response:
      status: 201
      headers:
         content-type: application/json
      body: OK


-  request:
      method: [GET]
      url: /uri/with/sequenced/responses

   response:
      -  status: 201
         headers:
            content-type: application/json
         body: OK

      -  status: 201
         headers:
            content-stype: application/json
         body: Still going strong!

      -  status: 500
         headers:
            content-type: application/json
         body: OMG!!!


-  request:
      method: [GET]
      url: /uri/with/sequenced/responses/infile

   response:
      -  status: 201
         headers:
            content-type: application/json
         file: ../json/sequenced.response.ok.json

      -  status: 201
         headers:
            content-stype: application/json
         file: ../json/sequenced.response.goingstrong.json

      -  status: 500
         headers:
            content-type: application/json
         file: ../json/sequenced.response.omfg.json


-  request:
      method: [GET]
      url: /uri/with/single/sequenced/response

   response:
      -  status: 201
         headers:
            content-stype: application/json
         body: Still going strong!
```

#### status (`required`)

* the HTTP status code of the response.
* integer or integer-like string.
* defaults to `200`.

```yaml
-  request:
      url: ^/im/a/teapot$
      method: POST
   response:
      status: 420
```

#### body (`optional`)

* contents of the response body
* defaults to an empty content body
* can be a URL (OAUTH is not supported) to `record & replay` response from a remote host (see [Record and Replay](#record-and-replay)). The HTTP response is recorded on the first call to stubbed URL, having the subsequent calls replay back the recorded HTTP response, without actually connecting to the remote host again.

```yaml
-  request:
      url: ^/give/me/a/smile$
   response:
      body: ':)'
```

```yaml
-  request:
      url: ^/give/me/a/smile$

   response:
      status: 200
      body: >
         {"status": "hello world with single quote"}
      headers:
         content-type: application/json
```

```yaml
-  request:
      method: GET
      url: /atomfeed/1

   response:
      headers:
         content-type: application/xml
      status: 200
      body: <?xml version="1.0" encoding="UTF-8"?><payment><paymentDetail><invoiceTypeLookupCode/></paymentDetail></payment>
```

```yaml
-  request:
      url: /1.1/direct_messages.json
      query:
         since_id: 240136858829479935
         count: 1
   response:
      headers:
         content-type: application/json
      body: https://api.twitter.com/1.1/direct_messages.json?since_id=240136858829479935&count=1
```

#### file (`optional`)

* similar to `request.file`, holds a path to a local file (it can be an `absolute` or `relative` path to the main YAML specified in `-d` or `--data`). This property allows you to split up stubby data across multiple files instead of making one huge bloated main config YAML. For example, let's say you want to render a large response body upon successful stub matching, so instead of dumping a lot of text under the `body` property, you could specify a local file with the response content using the `file` property (btw, the `file` can also refer to binary files):

```yaml
response:
      status: 200
      headers:
         content-type: application/json
      file: ../json/response.json
```

* please note, if both `file` & `body` properties are supplied, the `file` takes precedence & replaces `body` with the contents from the provided file
* if `--watch` command-line argument was supplied during startup, then any modifications to the supplied local file in `file` (e.g. `file: ../json/response.json`) will cause the whole configuration to be reloaded.
* if the file could not be loaded, stubby falls back to the value stubbed in `body`
* if `body` was not stubbed, an empty string is returned by default
* it can be ascii of binary file (PDF, images, etc.). Please keep in mind, that file is preloaded upon stubby4j startup and its content is kept as a byte array in memory. In other words, response files are not read from the disk on demand, but preloaded.


```yaml
-  request:
      url: /
   response:
      file: extremelyLongJsonFile.json
```

#### headers (`optional`)

* similar to `request.headers` except that these are sent back to the client.
* by default, header `x-stubby-resource-id` containing resource ID is returned with each stubbed response. The ID is useful if the returned resource needs to be updated at run time by ID via Admin portal

```yaml
-  request:
      url: ^/give/me/some/json$
   response:
      headers:
         content-type: application/json
      body: >
         [{
            "name":"John",
            "email":"john@example.com"
         },{
            "name":"Jane",
            "email":"jane@example.com"
         }]
```

#### latency (`optional`)

* time to wait, in `milliseconds`, before sending the response to the caller
* good for testing timeouts, or slow connections

```yaml
-  request:
      url: ^/hello/to/jupiter$
   response:
      latency: 800000
      body: Hello, World!
```

</details>

[Back to top](#table-of-contents)

### Dynamic token replacement in stubbed response

During HTTP request verification, you can leverage regex capturing groups ([Regex stubbing for dynamic matching](#regex-stubbing-for-dynamic-matching)) as token values for dynamic token replacement in stubbed response.

<details>
 <summary><code>Click to expand</code></summary>
 <br />

stubby supports dynamic token replacement on the following properties:
- `response` `body`
- `response` `header` name values (including `location` header value)
- `response` `file` names & payloads.

#### Example
```yaml
-  request:
      method: [GET]
      url: ^/regex-fileserver/([a-z]+).html$

   response:
      status: 200
      file: ../html/<% url.1 %>.html


-  request:
      method: [GET]
      url: ^/v\d/identity/authorize
      query:
         redirect_uri: "https://(.*)/app.*"

   response:
      headers:
         location: https://<% query.redirect_uri.1 %>/auth
      status: 302
  
            
-  request:
      method: [GET]
      url: ^/account/(\d{5})/category/([a-zA-Z]+)
      query:
         date: "([a-zA-Z]+)"
      headers:
         custom-header: "[0-9]+"

   response:
      status: 200
      body: Returned invoice number# <% url.1 %> in category '<% url.2 %>' on the date '<% query.date.1 %>', using header custom-header <% headers.custom-header.0 %>
```
#### Example explained

The `url` regex `^/account/(\d{5})/category/([a-zA-Z]+)` has two defined capturing groups: `(\d{5})` and `([a-zA-Z]+)`, `query` regex has one defined capturing group `([a-zA-Z]+)`. In other words, a manually defined capturing group has parenthesis around it.

Although, the `headers` regex does not have capturing groups defined explicitly (no regex sections within parenthesis), its matched value is still accessible in a template (keep on reading!).

#### Token structure
The tokens in `response` `body` follow the format of `<%` `PROPERTY_NAME` `.` `CAPTURING_GROUP_ID` `%>`. If it is a token that should correspond to `headers` or `query` regex match, then the token structure would be as follows: `<%` `HEADERS_OR_QUERY` `.` `KEY_NAME` `.` `CAPTURING_GROUP_ID` `%>`. Whitespace is __allowed__ between the `<%` & `%>` and what's inside.

#### Numbering the tokens based on capturing groups without sub-groups
When giving tokens their ID based on the count of manually defined capturing groups within regex, you should start from `1`, not zero (zero reserved for token that holds __full__ regex match) from left to right. So the leftmost capturing group would be `1` and the next one to the right of it would be `2`, etc.

In other words `<% url.1 %>` and `<% url.2 %>` tokens correspond to two capturing groups from the `url` regex `(\d{5})` and `([a-zA-Z]+)`, while `<% query.date.1 %>` token corresponds to one capturing group `([a-zA-Z]+)` from the `query` `date` property regex.

#### Numbering the tokens based on capturing groups with sub-groups
In regex world, capturing groups can contain capturing sub-groups, as an example consider proposed `url` regex: `^/resource/` `(` `([a-z]{3})` `-` `([0-9]{3})` `)` `$`. In the latter example, the regex has three groups - a parent group `([a-z]{3}-[0-9]{3})` and two sub-groups within: `([a-z]{3})` & `([0-9]{3})`.

When giving tokens their ID based on the count of capturing groups, you should start from `1`, not zero (zero reserved for token that holds __full__ regex match) from left to right. If a group has sub-group within, you count the sub-group(s) first (also from left to right) before counting the next one to the right of the parent group.

In other words tokens `<% url.1 %>`, `<% url.2 %>` and `<% url.3 %>` correspond to the three capturing groups from the `url` regex (starting from left to right): `([a-z]{3}-[0-9]{3})`, `([a-z]{3})` and `([0-9]{3})`.

#### Tokens with ID zero
Tokens with ID zero can obtain __full__ match value from the regex they reference. In other words, tokens with ID zero do not care whether regex has capturing groups defined or not. For example, token `<% url.0 %>` will be replaced with the `url` __full__ regex match from `^/account/(\d{5})/category/([a-zA-Z]+)`. So if you want to access the `url` __full__ regex match, respectively you would use token `<% url.0 %>` in your template.

Another example, would be the earlier case where `headers` `custom-header` property regex does not have capturing groups defined within. Which is fine, since the `<% headers.custom-header.0 %>` token corresponds to the __full__ regex match in the `header` `custom-header` property regex: `[0-9]+`.

It is also worth to mention, that the __full__ regex match value replacing token `<% query.date.0 %>`, would be equal to the regex capturing group value replacing `<% query.date.1 %>`. This is due to how the `query` `date` property regex is defined - the one and only capturing group in the `query` `date` regex, is also the __full__ regex itself.

#### Where to specify the template
You can specify template with tokens in both `body` as a string or using `file` by specifying template as external local file. When template is specified as `file`, the contents of local file from `file` will be replaced.

Alternatively, you can also template the path to the file itself:
```yaml
-  request:
      method: [GET]
      url: ^/regex-fileserver/([a-z]+).html$

   response:
      status: 200
      file: ../html/<% url.1 %>.html
```
When the request is recieved and the regex matches, the path to the file will get resolved and the file content will be served if it exists.

```yaml
-  request:
      method: POST
      url: /post-body-as-json
      headers:
         content-type: application/json
      post: >
         {"userId":"19","requestId":"(.*)","transactionDate":"(.*)","transactionTime":"(.*)"}

   response:
      headers:
         content-type: application/json
      status: 200
      body: >
         {"requestId": "<%post.1%>", "transactionDate": "<%post.2%>", "transactionTime": "<%post.3%>"}
```
Another example demonstrating the usage of tokens from the matched regex groups

#### When token interpolation happens
After successful HTTP request verification, if your `body` or contents of local file from `file` contain tokens - the tokens will be replaced just before rendering HTTP response.

#### Troubleshooting
* Make sure that the regex you used in your stubby4j configuration actually does what it suppose to do. Validate that it works before using it in stubby4j
* Make sure that the regex has capturing groups for the parts of regex you want to capture as token values. In other words, make sure that you did not forget the parenthesis within your regex if your token IDs start from `1`
* Make sure that you are using token ID zero, when wanting to use __full__ regex match as the token value
* Make sure that the token names you used in your template are correct: check that property name is correct, capturing group IDs, token ID of the __full__ match, the `<% ` and ` %>`

</details>

[Back to top](#table-of-contents)

### Stubbing HTTP 30x redirects

In order to stub a `30x` HTTP redirect, you need to stub the following:
* the `location` header in `headers` section of the `response`
* the `status` of the `response` must be `one` of the following HTTP codes: `301`, `302`, `303`, `307` or `308`.

#### Example
```yaml
- request:
    method: GET
    headers:
      content-type: application/json
    url: /item/redirect/source

  response:
    status: 301
    headers:
      location: /item/redirect/destination


- request:
    method: GET
    headers:
      content-type: application/json
    url: /item/redirect/destination

  response:
    headers:
      content-type: application/json
    status: 200
    body: >
      {"response" : "content"}
```
#### Example explained

Upon successful HTTP request verification, the `/item/redirect/destination` value of the stubbed `response` header `location` will be set as the value of the location header of the Jetty HTTP response, which will cause the redirect to another stub with `url` value `/item/redirect/destination`


[Back to top](#table-of-contents)


### Record and replay

If `body` of the stubbed `response` contains a URL starting with http(s), stubby knows that it should record an HTTP response
from the provided URL (before rendering the stubbed response) and replay the recorded HTTP response on each subsequent call.

#### Example
```yaml
-  request:
      method: [GET]
      url: /maps/api/geocode/json
      query:
         address: "1600%20Amphitheatre%20Parkway,%20Mountain%20View,%20CA"
         sensor: false

   response:
      status: 200
      headers:
         content-type: application/json
      body: http://maps.googleapis.com
```
#### Example explained

Upon successful HTTP request verification, properties of stubbed `request` (`method`, `url`, `headers`, `post` and `query`) are used to construct
an HTTP request to the destination URL specified in `body` of the stubbed `response`.

In the above example, stubby will record HTTP response received after submitting an HTTP GET request to the url below:
`http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=1600+Amphitheatre+Parkway,+Mountain+View,+CA`

#### Please note
* Recorded HTTP response is not persistable, but kept in memory only. In other words, upon stubby shutdown the recording is lost
* Make sure to specify in `response` `body` only the URL, without the path info. Path info should be specified in `request` `url`

[Back to top](#table-of-contents)

## Supplying stubbed endpoints to stubby

There are two ways available (listed in no particular order):
1. Submit `POST` requests to `localhost:8889` at runtime (check the [The admin portal](#the-admin-portal))
2. Load a YAML config data-file (using `-d` / `--data` flags) with the following structure for each stubbed endpoint:

* `description`: optional description shown in logs
* `uuid`: optional unique identifier
* `request`: describes the client's call to the server
   * `method`: `GET`/`POST`/`PUT`/`PATCH`/`DELETE`/etc.
   * `url`: the URI regex string. GET parameters should also be included inline here
   * `query`: a key/value map of query string parameters included with the request. Query param value can be regex.
   * `headers`: a key/value map of headers the server should respond to. Header value can be regex.
   * `post`: a string matching the textual body of the response. Post value can be regex.
   * `file`: if specified, returns the contents of the given file as the request post. If the file cannot be found at request time, **post** is used instead
* `response`: describes the server's response (or array of responses, refer to the earlier examples) to the client
   * `headers`: a key/value map of headers the server should use in it's response.
   * `latency`: the time in milliseconds the server should wait before responding. Useful for testing timeouts and latency
   * `file`: if specified, returns the contents of the given file as the response body. If the file cannot be found at request time, **body** is used instead
   * `body`: the textual body of the server's response to the client
   * `status`: the numerical HTTP status code (200 for OK, 404 for NOT FOUND, etc.)

[Back to top](#table-of-contents)

### Splitting main YAML config

There are situations where your main YAML config file will grow and become bloated due to large number of stubs,
e.g.: your application talks to many downstream services. 

stubby4j supports splitting the main YAML config file into multiple sub-config YAML files, which allows for more logical &
cleaner stub code organisation (kudos fly to https://github.com/harrysun2006). 

#### Example

Main `data.yaml`:

```yaml
includes:
   - service-1-stubs.yaml
   - service-2-stubs.yaml
   - ...
   - ...
   - service-N-stubs.yaml
```

#### Example explained

You define the stubbed endpoints for each service (or any other logical organisation of stubs that suits your needs) in
their own `some-name-that-suits-you-N.yaml` sub-config files. 

When stubby parses the main `data.yaml` provided using `-d` / `--data` flags, all included sub-configs will be loaded
as if all the stubs were defined in one YAML.

__Please note__

You `cannot mix` in the __same__ YAML config the `includes` with sub-configs & defining stubs using `request`/`response`, e.g.: stubby4j __will fail__ to load the following YAML:

```yaml
includes:
   - service-1-stubs.yaml
   - service-2-stubs.yaml
   - service-3-stubs.yaml
     
-  request:
      method:
         -  GET
         -  POST
         -  PUT
      url: ^/resources/asn/.*$

   response:
      status: 200
      body: >
         {"status": "ASN found!"}
      headers:
         content-type: application/json
```  

[Back to top](#table-of-contents)

## Performance optimization index

stubby4j uses a number of techniques to optimize evaluation of stubs

#### Regex pattern pre-compilation

During parsing of stubs config, the `request.url`, `request.query`, `request.headers` & `request.post` (or `request.file`)
values are checked for presence of regex. If one of the aforementioned properties is a stubbed regex, then a regex pattern
will be compiled & cached in memory. This way, the pattern(s) are compiled during config parsing, not stub evaluation.

#### Local caching of returning matched requests

On every incoming request, a local cache holding previously matched stubs is checked to see if there is a match for the
incoming request hashCode. If the incoming request hashCode found in the cache, then the cached matched stub & the
incoming request are compared to each other to determine a complete equality based on the stubbed `request` properties.

If a complete equality against the cached stub was not achieved, the incoming request is compared to all other stubs
loaded in memory. If a full match was found, then that match will be cached using the incoming request hashCode as a key.

To disable stub caching pass `--disable_stub_caching` command-line arg to stubby4j jar upon start up (refer to [Command-line switches](#command-line-switches) sectio )

[Back to top](#table-of-contents)

## The admin portal

See [docs/ADMIN_PORTAL.md](docs/ADMIN_PORTAL.md) for details


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

Pseudocode ([StubRepository#matchStub](src/main/java/io/github/azagniotov/stubby4j/stubs/StubRepository.java#L128)):

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

See [docs/CHANGELOG.md](docs/CHANGELOG.md)

## Authors

See [docs/AUTHORS.md](docs/AUTHORS.md)

## Contributors

See [docs/CONTRIBUTORS.md](docs/CONTRIBUTORS.md)

## See also
* **[stubby4net](https://github.com/mrak/stubby4net):** A .NET implementation of stubby
* **[stubby4node](https://github.com/mrak/stubby4node):** A node.js implementation of stubby


## Copyright

See [docs/COPYRIGHT.md](docs/COPYRIGHT.md) for details

## License
MIT. See [LICENSE](LICENSE) for details


[Back to top](#table-of-contents)

<!-- references -->

[circleci-badge]: https://circleci.com/gh/azagniotov/stubby4j.svg?style=shield
[circleci-link]: https://circleci.com/gh/azagniotov/stubby4j

[codecov-badge]: https://codecov.io/gh/azagniotov/stubby4j/branch/master/graph/badge.svg
[codecov-link]: https://codecov.io/gh/azagniotov/stubby4j

[maven-badge]: https://img.shields.io/maven-central/v/io.github.azagniotov/stubby4j.svg?style=flat&label=maven-central
[maven-link]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.azagniotov%22%20AND%20a%3A%22stubby4j%22

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
