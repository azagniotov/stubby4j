[![CircleCI build master branch][circleci-badge]][circleci-link]
[![codecov][codecov-badge]][codecov-link]
[![Maven Central][maven-badge]][maven-link]
[![Stackoverflow stubby4j][stackoverflow-badge]][stackoverflow-link]
[![Chat on gitter about stubby4j][chat-badge]][chat-link]
[![License][license-badge]][license-link]


[![stubb4j][logo-badge]][logo-link]


A highly flexible and configurable tool for testing interactions of SOA applications with web services (REST, SOAP, WSDL etc.) over HTTP(S) protocol. It is an actual HTTP server (stubby4j uses embedded Jetty) that allows stubbing of external systems with ease for integration, contract & behavior testing. Please refer to [Key features](#key-features) for more information

#### Why the word "stubby"?
It is a stub HTTP server after all, hence the "stubby". Also, in Australian slang "stubby" means _beer bottle_

## User manual for stubby4j v7.0.0
### Table of contents

* [Quick start example](#quick-start-example)
* [Key features](#key-features)
* [Why would a developer use stubby4j](#why-would-a-developer-use-stubby4j)
* [Why would a QA use stubby4j](#why-would-a-qa-use-stubby4j)
* [Building](#building)
* [Third-party dependencies](#third-party-dependencies)
* [Adding stubby4j to your project](#adding-stubby4j-to-your-project)
   * [Installing stubby4j to local .m2 repository](#installing-stubby4j-to-local-m2-repository)
* [Command-line switches](#command-line-switches)
* [Endpoint configuration HOWTO](#endpoint-configuration-howto)
   * [Stub/Feature](#stubfeature)
   * [Request](#request)
      * [Regex stubbing for dynamic matching](#regex-stubbing-for-dynamic-matching)
      * [Authorization Header](#authorization-header)
   * [Response](#response)
      * [Dynamic token replacement in stubbed response](#dynamic-token-replacement-in-stubbed-response)
      * [Record and Play](#record-and-play)
* [Performance optimization index](#performance-optimization-index)
   * [Regex pattern precompilation](#regex-pattern-pre-compilation)
   * [Local caching of returning matched requests](#local-caching-of-returning-matched-requests)
* [The admin portal](#the-admin-portal)
   * [Supplying endpoints to stubby](#supplying-endpoints-to-stubby)
   * [YAML (file only or POST/PUT)](#yaml-file-only-or-postput)
   * [JSON support](#json-support)
   * [Getting the current list of stubbed endpoints](#getting-the-current-list-of-stubbed-endpoints)
   * [The status page](#the-status-page)
   * [Refreshing stubbed data via an endpoint](#refreshing-stubbed-data-via-an-endpoint)
   * [Updating existing endpoints](#updating-existing-endpoints)
   * [Deleting endpoints](#deleting-endpoints)
   * [Deleting ALL endpoints at once](#deleting-all-endpoints-at-once)
* [The stubs portal](#the-stubs-portal)
* [Programmatic API](#programmatic-api)
* [Change log](#change-log)
* [Roadmap](#roadmap)
* [Authors](#authors)
* [Kudos](#kudos)
* [See also](#see-also)

## Quick start example

This section explains how to get stubby4j up and running using a very simple example "Hello, World", without building stubby4j from source locally using Gradle. 

#### Minimum system requirements to run stubby4j archives hosted on [Maven Central][maven-link]

* version >= 4.0.0:  Oracle JRE v1.8 or OpenJDK 1.8
* version >= 3.0.0:  Oracle JRE v1.7.0_76
* version = 2.0.22: Oracle JRE v1.7.0_04
* version < 2.0.22: Oracle JRE 1.6.0_65-b14-462

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
* Execute the downloaded stubby JAR using command `java -jar stubby4j-x.x.xx.jar -d <PATH_TO_YOUR_CREATED_LOCAL_YAML_FILE>`
* Navigate to `http://localhost:8882/hello-world` to get the stubbed response "Hello World!"
* Navigate to stubby4j admin portal at `http://localhost:8889/status` to see what has been stubbed & other useful data

That's it!

For more information and more complex examples, please dive into the rest of documentation, especially [Endpoint configuration HOWTO](#endpoint-configuration-howto)

## Key features
* Emulate external webservice in a SANDBOX for your application to consume over HTTP(S)
* HTTP request verification and HTTP response stubbing
* Regex support for dynamic matching on URI, query params, headers, POST payload (ie:. `mod_rewrite` in Apache)
* Dynamic token replacement in stubbed response, by leveraging regex capturing groups as token values during HTTP request verification
* Record & Replay. The HTTP response is recorded on the first call, having the subsequent calls play back the recorded HTTP response, without actually connecting to the external server
* Dynamic flows. Multiple stubbed responses on the same stubbed URI to test multiple application flows
* Fault injection, where after X good responses on the same URI you get a bad one
* Serve binary files as stubbed response content (images, PDFs. etc.)
* Embed stubby4j to create a web service SANDBOX for your integration test suite

## Why would a developer use stubby4j?
#### You want to:
* Simulate responses from real server and don't care (or cannot) to go over the network
* Third party web service your application suppose to contract with is not ready yet
* Verify that your code makes HTTP requests with all the required parameters and/or headers
* Verify that your code correctly handles HTTP error codes
* You want to trigger response from the server based on the request parameters over HTTP or HTTPS
* Support for any of the available HTTP methods
* Simulate support for different types of HTTP Authorizations: Basic, Bearer Token & others
* Support for HTTP 30x redirects
* Provide canned answers in your contract/integration tests
* Enable delayed responses for performance and stability testing
* Avoid to spend time coding for the above requirements
* Concentrate on the task at hand


## Why would a QA use stubby4j?
* Specifiable mock responses to simulate page conditions without real data.
* Ability to test polling mechanisms by stubbing a sequence of responses for the same URI
* Easily swappable data config files to run different data sets and responses.
* All-in-one stub server to handle mock data with less need to upkeep code for test generation


## Building
stubby4j is a multi-module Gradle v6.2.2 project

Run `./gradlew` command to:
* Clean
* Run unit, integration and functional tests without Cobertura
* Build (the generated JAR artifacts will be located under `<PROJECT_ROOT>/build/libs/`)

Run `./gradlew cobertura` command to:
* Clean
* Generate Cobertura report under the `<PROJECT_ROOT>/main/build/reports/cobertura/`


## Third-party dependencies

* See the [conf/gradle/dependency.gradle](conf/gradle/dependency.gradle)


## Adding stubby4j to your project
The following are the stubby4j artifacts that are hosted on [Maven Central][maven-link]:

* `stubby4j-x.x.x.jar` - an `uber` JAR containing all the 3rd-party deps
* `stubby4j-x.x.x-no-dependencies.jar` - a `skinny` JAR containing no 3rd-party dependencies at all
* `stubby4j-x.x.x-no-jetty.jar` - an `uber-ish` JAR containing all the 3rd-party deps __except__ Jetty binaries
* `stubby4j-x.x.x-sources.jar`
* `stubby4j-x.x.x-javadoc.jar`

#### Gradle
```xml
compile("io.github.azagniotov:stubby4j:7.0.0")
```
or by adding a `classifier` to the JAR name like `no-dependencies` or `no-jetty`, i.e.:

```xml
compile("io.github.azagniotov:stubby4j:7.0.0:no-jetty")
```

#### Maven
```xml
<dependency>
    <groupId>io.github.azagniotov</groupId>
    <artifactId>stubby4j</artifactId>
    <version>7.0.0</version>
</dependency>
```
or by adding a `classifier` to the JAR name like `no-dependencies` or `no-jetty`, i.e.:

```xml
<dependency>
    <groupId>io.github.azagniotov</groupId>
    <artifactId>stubby4j</artifactId>
    <version>7.0.0</version>
    <classifier>no-dependencies</classifier>
</dependency>
```

### Installing stubby4j to local .m2 repository

Run `./gradlew installLocally` command to:

* Install `stubby4j-7.0.1-SNAPSHOT*.jar` to local `~/.m2/repository`
* All the artifacts will be installed under `~/.m2/repository/{groupId}/{artifactId}/{version}/`, e.g.: `~/.m2/repository/io/github/azagniotov/stubby4j/7.0.1-SNAPSHOT/`

Now you can include locally installed stubby4j `SNAPSHOT` artifacts in your project:
```xml
compile("io.github.azagniotov:stubby4j:7.0.1-SNAPSHOT")
```
or by adding a `classifier` to the JAR name like `no-dependencie`s or `no-jetty`, i.e.:

```xml
compile("io.github.azagniotov:stubby4j:7.0.1-SNAPSHOT:no-jetty")
```


## Command-line switches
```
usage:
       java -jar stubby4j-x.x.xx.jar [-a <arg>] [-d <arg>] [-da] [-ds]
       [-h] [-k <arg>] [-l <arg>] [-m] [-o] [-p <arg>] [-s <arg>] [-t
       <arg>] [-v] [-w]
 -a,--admin <arg>             Port for admin portal. Defaults to 8889.
 -d,--data <arg>              Data file to pre-load endpoints. Optional
                              valid YAML 1.1 is expected. If YAML is not
                              provided, you will be expected to configure
                              stubs via the stubby4j HTTP POST API.
 -da,--disable_admin_portal   Does not start Admin portal
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
 -w,--watch                   Periodically scans for changes in last
                              modification date of the main YAML and
                              referenced external files (if any). The flag
                              can accept an optional arg value which is
                              the watch scan time in milliseconds. If
                              milliseconds is not provided, the watch
                              scans every 100ms. If last modification date
                              changed since the last scan period, the stub
                              configuration is reloaded
```

## Endpoint configuration HOWTO

This section explains the usage, intent and behavior of each property on the `request` and `response` objects.

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

## Stub/Feature

#### description (optional)

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

#### uuid (optional)

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


## Request

This object is used to match an incoming request to stubby against the available endpoints that have been configured.

#### url (required)

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

#### method

* defaults to `GET`.
* case-insensitive.
* can be any of the following:
    * HEAD
    * GET
    * POST
    * PUT
    * POST
    * DELETE
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

#### query

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

#### post

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

#### file

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

#### headers

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


## Response

Assuming a match has been made against the given `request` object, data from `response` is used to build the stubbed response back to the client.

* Can be a single response or a sequence of responses.
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

#### status

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

#### body

* contents of the response body
* defaults to an empty content body
* can be a URL (OAUTH is not supported) to record & replay. The HTTP response is recorded on the first call to stubbed `url`, having the subsequent calls play back the recorded HTTP response, without actually connecting to the external server

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

#### file

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

#### headers

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

#### latency

* time to wait, in milliseconds, before sending back the response
* good for testing timeouts, or slow connections

```yaml
-  request:
      url: ^/hello/to/jupiter$
   response:
      latency: 800000
      body: Hello, World!
```

### Dynamic token replacement in stubbed response

During HTTP request verification, you can leverage regex capturing groups ([Regex stubbing for dynamic matching](#regex-stubbing-for-dynamic-matching)) as token values for dynamic token replacement in stubbed response.

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


## Record and play

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


## Performance optimization index

stubby4j uses a number of techniques to optimize evaluation of stubs

#### Regex pattern pre-compilation

During parsing of stubs config, the `request.url`, `request.query`, `request.headers` & `request.post` (or `request.file`)
values are checked for presence of regex. If one of the aforementioned properties is a stubbed regex, then a regex pattern
will be compiled & cached in memory. This way, the pattern(s) are compiled during config parsing, not stub evaluation.

#### Local caching of returning matched requests

On every incoming request, a local cache holding previously matched stubs is checked to see if there is a match for the
incoming request URI. If the incoming URI found in the cache, then the cached matched stub & the incoming request are
compared to each other to determine a complete equality based on the stubbed `request` properties.

If a complete equality against the cached stub was not achieved, the incoming request is compared to all other stubs
loaded in memory. If a full match was found, then that match will be cached using the incoming request URI as a key.
                  

## The admin portal

The admin portal is a RESTful(ish) endpoint running on `localhost:8889`. Or wherever you described through stubby's command line args.


#### The status page

You can view the currently configured endpoints by going to `localhost:8889/status`


#### Supplying endpoints to stubby

Submit `POST` requests to `localhost:8889` at runtime __OR__ load a data-file (using non-optional `-d` / `--data` flags) with the following structure for each endpoint:

* `description`: optional description shown in logs
* `uuid`: optional unique identifier
* `request`: describes the client's call to the server
   * `method`: GET/POST/PUT/DELETE/etc.
   * `url`: the URI regex string. GET parameters should also be included inline here
   * `query`: a key/value map of query string parameters included with the request. Query param value can be regex.
   * `headers`: a key/value map of headers the server should respond to. Header value can be regex.
   * `post`: a string matching the textual body of the response. Post value can be regex.
   * `file`: if specified, returns the contents of the given file as the request post. If the file cannot be found at request time, **post** is used instead
* `response`: describes the server's response (or array of responses, refer to the examples) to the client
   * `headers`: a key/value map of headers the server should use in it's response.
   * `latency`: the time in milliseconds the server should wait before responding. Useful for testing timeouts and latency
   * `file`: if specified, returns the contents of the given file as the response body. If the file cannot be found at request time, **body** is used instead
   * `body`: the textual body of the server's response to the client
   * `status`: the numerical HTTP status code (200 for OK, 404 for NOT FOUND, etc.)

#### Getting the current list of stubbed endpoints

Performing a `GET` request on `localhost:8889` will return a YAML list of all currently saved responses. It will reply with `204 : No Content` if there are none saved.
Performing a `GET` request on `localhost:8889/<id>` will return the YAML object representing the response with the supplied id.

#### Refreshing stubbed data via an endpoint

If for some reason you do not want/cannot/not able to use `--watch` flag when starting stubby4j (or cannot restart stubby),
you can submit `GET` request to `localhost:8889/refresh` (or load it in a browser) in order to refresh the stubbed data.

#### Updating existing endpoints

Stubs can be updated by either `(a)` stub ID or `(b)` unique identifier (See [Stub/Feature UUID](#uuid-optional)).

The specific stub ID (`resource-id-<id>`) can be found when viewing stubs at `localhost:8889/status`. 

Updating stubs by stub ID can get rather brittle when dealing with big YAML configs or working with shared stubs. Therefore it is better to configure `uuid` property 
per stub in order to make stub management easier & isolated. 

* Send a `PUT` request with a stub payload to `localhost:8889/<id>`. It will reply with `400 Bad Request` if id does not exist. Success `201 Created`
* Send a `PUT` request with a stub payload to `localhost:8889/configured-uuid`. It will reply with `400 Bad Request` if uuid does not exist. Success `201 Created` 

#### Deleting endpoints

Stubs can be deleted by either `(a)` stub ID or `(b)` unique identifier (See [Stub/Feature UUID](#uuid-optional)).

The specific stub ID (`resource-id-<id>`) can be found when viewing stubs at `localhost:8889/status`. 

Deleting stubs by stub ID can get rather brittle when dealing with big YAML configs or working with shared stubs. Therefore it is better to configure `uuid` property 
per stub in order to make stub management easier & isolated. 

* Send a `DELETE` request to `localhost:8889/<id>`. It will reply with `400 Bad Request` if id does not exist. Success `200 OK`
* Send a `DELETE` request to `localhost:8889/configured-uuid`. It will reply with `400 Bad Request` if uuid does not exist. Success `200 OK`

#### Deleting ALL endpoints at once

Send a `DELETE` request to `localhost:8889`


#### YAML (file only or POST/PUT)
```yaml
-  description: "this is a feature describing something"
   request:
      url: ^/path/to/something$
      method: POST
      headers:
         authorization-basic: "bob:password" 
         x-custom-header: "^this/is/\d/test"
      post: this is some post data in textual format
   response:
      headers:
         Content-Type: application/json
      latency: 1000
      status: 200
      body: Your request was successfully processed!

-  request:
      url: ^/path/to/bearer$
      method: POST
      headers:
         authorization-bearer: "YNZmIzI2Ts0Q=="
      post: this is some post data in textual format
   response:
      headers:
         Content-Type: application/json
      status: 200
      body: Your request with Bearer was successfully authorized!

-  request:
      url: ^/path/to/anotherThing
      query:
         a: anything
         b: more
         custom: "^this/is/\d/test"
      method: GET
      headers:
         Content-Type: application/json
      post:
   response:
      headers:
         Content-Type: application/json
         Access-Control-Allow-Origin: "*"
      status: 204
      file: path/to/page.html

-  request:
      url: ^/path/to/thing$
      method: POST
      headers:
         Content-Type: application/json
      post: this is some post data in textual format
   response:
      headers:
         Content-Type: application/json
      status: 304
```

#### JSON support

JSON is a subset of YAML 1.2, SnakeYAML (Third-party library used by stubby4j for YAML & JSON parsing) implements YAML 1.1 at the moment. It means that not all the JSON documents can be parsed. Just give it a go.

#### JSON (file or POST/PUT)

```json
[
  { 
    "description": "this is a feature describing something",
    "request": {
      "url": "^/path/to/something$",
      "post": "this is some post data in textual format",
      "headers": {
         "authorization-basic": "bob:password"  // for basic authorization DO NOT base64 encode when stubbing
      },
      "method": "POST"
    },
    "response": {
      "status": 200,
      "headers": {
        "Content-Type": "application/json"
      },
      "latency": 1000,
      "body": "Your request was successfully processed!"
    }
  },
  {
    "request": {
      "url": "^/path/to/anotherThing",
      "query": {
         "a": "anything",
         "b": "more"
      },
      "headers": {
        "Content-Type": "application/json"
      },
      "method": "GET"
    },
    "response": {
      "status": 204,
      "headers": {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
      },
      "file": "path/to/page.html"
    }
  },
  {
    "request": {
      "url": "^/path/to/thing$",
      "headers": {
        "Content-Type": "application/json"
      },
      "post": "this is some post data in textual format",
      "method": "POST"
    },
    "response": {
      "status": 304,
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }
]
```

If you want to load more than one endpoint via file, use either a JSON array or YAML list (-) syntax. When creating or updating one stubbed request, the response will contain `Location` in the header with the newly created resources' location


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

Pseudocode ([StubRepository#matchStub](main/java/io/github/azagniotov/stubby4j/stubs/StubRepository.java#L142)):

```
    if (<incoming request>.url found in <previous matched cache>) {
        get <cached stubbed endpoint> from <previous matched cache> by <incoming request>.url
        if (<cached stubbed endpoint> == <incoming request>) {
            return <cached stubbed endpoint>
        }
    }
    for each <stubbed endpoint> of stored endpoints {
        for each <property> of <stubbed endpoint> {
            if (<stubbed endpoint>.<property> != <incoming request>.<property>) {
                next stubbed endpoint
            }
        }
        store in <previous matched cache> the found <stubbed endpoint> by url

        return <stubbed endpoint>
    }
```

## Programmatic API

You can start-up and manage stubby4j with the help of [StubbyClient](main/java/io/github/azagniotov/stubby4j/client/StubbyClient.java)

## Change log

See [CHANGELOG.md](CHANGELOG.md) for details

## Roadmap
* Add support for OAuth in Record & Replay feature
* Scenarios where multiple endpoints correlate with each other based on the scenario. Useful in e2e testing where system brought to a certain state (maybe?)

## Authors
A number of people have contributed directly to stubby4j by writing
documentation or developing software.

1. Alexander Zagniotov <azagniotov@gmail.com>
2. Eric Mrak <enmrak@gmail.com>


## Kudos
A number of people have contributed to stubby4j by reporting problems, suggesting improvements or submitting changes. Special thanks fly out to the following **Ninjas** for their help, support and feedback

* Isa Goksu
* Eric Mrak
* Oleksandr Berezianskyi
* Sankalp Saxena
* Simon Brunning
* Ed Hewell
* Kenny Lin
* Logan McGrath


## See also
* **[stubby4net](https://github.com/mrak/stubby4net):** A .NET implementation of stubby
* **[stubby4node](https://github.com/mrak/stubby4node):** A node.js implementation of stubby


## Copyright
Yes. See COPYRIGHT for details

## License
MIT. See LICENSE for details


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

[logo-badge]: https://cdn.rawgit.com/azagniotov/stubby4j/master/assets/stubby-logo-duke-hiding.svg
[logo-link]: https://github.com/azagniotov/stubby4j
