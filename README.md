Fork of [stubby4j](https://github.com/azagniotov/stubby4j) Heroku-deployable out-of-the-box
=================================================
1. Fork this repo.
2. Edit `stubby4j/main/resources/yaml/stub.yaml` to configure your endpoints
3. Make sure you have the [Heroku Toolbelt](https://toolbelt.heroku.com/) installed.
4. Follow __Deploying locally__ or __Deploying to Heroku__ steps

## Deploying locally
```sh
git clone git@github.com:YOUR_GITHUB_USER/stubby4j.git # clone your fork
cd stubby4j
./gradlew stage && heroku local web
```
Your fork should now be running on [localhost:5000](http://localhost:5000/).

## Deploying to Heroku
```
git clone git@github.com:YOUR_GITHUB_USER/stubby4j.git # clone your fork
cd stubby4j
heroku create
git push heroku master
heroku open
```
Alternatively, you can deploy your own copy of the app using this button:

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

---

[![Build Status](https://secure.travis-ci.org/azagniotov/stubby4j.png?branch=master)](http://travis-ci.org/azagniotov/stubby4j)
[![Dependency Status](https://www.versioneye.com/user/projects/54fd17e14f31081ed1000017/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54fd17e14f31081ed1000017)
[![Coverage Status](https://coveralls.io/repos/azagniotov/stubby4j/badge.svg?style=flat)](https://coveralls.io/r/azagniotov/stubby4j)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/by.stub/stubby4j/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/by.stub/stubby4j)
[![Pending Pull-Requests](http://githubbadges.herokuapp.com/azagniotov/stubby4j/pulls.svg?style=flat)](https://github.com/azagniotov/stubby4j/pulls)
[![Github Issues](http://githubbadges.herokuapp.com/azagniotov/stubby4j/issues.svg?style=flat)](https://github.com/azagniotov/stubby4j/issues)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat)](http://badges.mit-license.org)


# stubby4j
A highly flexible and configurable tool for testing interactions of SOA applications with web services (REST, SOAP, WSDL etc.) over HTTP(S) protocol.
It is an actual HTTP server (stubby4j uses embedded Jetty) that acts like a real web service, ready for consumption by your code. Allows stubbing of external systems with ease for integration testing

##### Why the word "stubby"?
It is a stub HTTP server after all, hence the "stubby". Also, in Australian slang "stubby" means _beer bottle_

## User manual for stubby4j v3.3.0
### Table of contents

* [Quick start example](#quick-start-example)
* [Key features](#key-features)
* [Why would a developer use stubby4j](#why-would-a-developer-use-stubby4j)
* [Why would a QA use stubby4j](#why-would-a-qa-use-stubby4j)
* [Building](#building)
* [Third-party dependencies](#third-party-dependencies)
* [Adding stubby4j to your project](#adding-stubby4j-to-your-project)
* [Command-line switches](#command-line-switches)
* [Endpoint configuration HOWTO](#endpoint-configuration-howto)
   * [Request](#request)
   * [Response](#response)
   * [Record and Play](#record-and-play)
   * [Dynamic token replacement in stubbed response](#dynamic-token-replacement-in-stubbed-response)
   * [Authorization Header](#authorization-header)
* [The admin portal](#the-admin-portal)
* [The stubs portal](#the-stubs-portal)
* [Programmatic API](#programmatic-api)
* [Change log](#change-log)
* [Roadmap](#roadmap)
* [Authors](#authors)
* [Kudos](#kudos)
* [See also](#see-also)

### Quick start example

This section explains how to get stubby4j up and running using a very simple example "Hello, World", without building stubby4j from source locally using Gradle. 

##### Minimum system requirements to run stubby4j archives hosted on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cstubby4j)

* version >= 3.0.0:  Oracle JRE v1.7.0_76
* version = 2.0.22: Oracle JRE v1.7.0_04
* version < 2.0.22: Oracle JRE 1.6.0_65-b14-462

##### Setup

* Download the [latest stubby4j version](http://search.maven.org/#search%7Cga%7C1%7Cstubby4j) (the JAR archive).
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

### Key features
* Emulate external webservice in a SANDBOX for your application to consume over HTTP(S)
* HTTP request verification and HTTP response stubbing
* Regex support for dynamic matching on URI, query params, headers, POST body (ie:. `mod_rewrite` in Apache)
* Dynamic token replacement in stubbed response, by leveraging regex capturing groups as token values during HTTP request verification
* Record & Replay. The HTTP response is recorded on the first call, having the subsequent calls play back the recorded HTTP response, without actually connecting to the external server
* Dynamic flows. Multiple stubbed responses on the same stubbed URI to test multiple application flows
* Fault injection, where after X good responses on the same URI you get a bad one
* Serve binary files as stubbed response content (images, PDFs. etc.)
* Embed stubby4j to create a web service SANDBOX for your integration test suite
* Over 98% test coverage (the percentile alone should not be taken as an indicator of test quality, but nevertheless - the library is thoroughly tested)

### Why would a developer use stubby4j?
####You want to:
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


### Why would a QA use stubby4j?
* Specifiable mock responses to simulate page conditions without real data.
* Ability to test polling mechanisms by stubbing a sequence of responses for the same URI
* Easily swappable data config files to run different data sets and responses.
* All-in-one stub server to handle mock data with less need to upkeep code for test generation


### Building
stubby4j is a multi-module Gradle project

* IntelliJ IDEA 13 users should run ```gradle cleanIdea idea``` in order to generate IntelliJ IDEA 13 project files
* Eclipse users should run ```gradle cleanEclipse eclipse``` in order to generate Eclipse project files

Run `gradle` command to:
* Clean
* Run unit, integration and functional tests without Cobertura
* Build (the generated `stubby4j-x.x.x-SNAPSHOT.jar` will be located under `stubby4j/target/libs/`)

Run `gradle build -x test` command to:
* Clean
* Build (the generated `stubby4j-x.x.x-SNAPSHOT.jar` will be located under `stubby4j/target/libs/`)

Run `gradle cobertura` command to:
* Clean
* Generate Cobertura report under the `stubby4j/main/target/reports/cobertura/`


### Third-party dependencies
stubby4j is a fat JAR, which contains the following dependencies:

* __javax.servlet-api-3.1.0.jar__
* jetty-server-9.2.10.v20150310.jar
* jetty-servlets-9.2.10.v20150310.jar
* jetty-http-9.2.10.v20150310.jar
* jetty-io-9.2.10.v20150310.jar
* jetty-continuation-9.2.10.v20150310.jar
* jetty-util-9.2.10.v20150310.jar
* commons-cli-1.2.jar
* snakeyaml-1.15.jar
* jsonassert-1.2.3.jar
* xmlunit-1.6.jar
* json-20090211.jar


### Adding stubby4j to your project
stubby4j is hosted on [Maven Central](http://search.maven.org) and can be added as a dependency in your project's build script.
Keep in mind that __it takes 5-8 hours for a new release to appear on live Maven Central repo__. In other words, if you cannot fetch `v.3.3.0` as a dependency yet, it means [Maven Central](http://search.maven.org) has not been synced yet ;)

##### Apache Maven
```xml
<dependency>
    <groupId>by.stub</groupId>
    <artifactId>stubby4j</artifactId>
    <version>3.3.0</version>
</dependency>
```

##### Apache Ivy
```xml
<dependency org="by.stub" name="stubby4j" rev="3.3.0" />
```

##### Apache Buildr
```xml
'by.stub:stubby4j:jar:3.3.0'
```

##### Gradle
```xml
compile 'by.stub:stubby4j:3.3.0'
```

##### Scala SBT
```xml
libraryDependencies += "by.stub" % "stubby4j" % "3.3.0"
```

### Command-line switches
```
usage:
       java -jar stubby4j-x.x.xx.jar [-a <arg>] [-d <arg>] [-da] [-ds]
       [-h] [-k <arg>] [-l <arg>] [-m] [-o] [-p <arg>] [-s <arg>] [-t
       <arg>] [-v] [-w]
 -a,--admin <arg>             Port for admin portal. Defaults to 8889.
 -d,--data <arg>              Data file to pre-load endpoints. Valid YAML
                              1.1 expected.
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

### Endpoint configuration HOWTO

This section explains the usage, intent and behavior of each property on the `request` and `response` objects.

Here is a fully-populated, unrealistic endpoint:
```yaml
-  request:
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

### Request

This object is used to match an incoming request to stubby against the available endpoints that have been configured.

##### url (required)

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

##### method

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

##### query

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

##### post

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

##### file

* holds a path to a local file (absolute or relative to the YAML specified in `-d` or `--data`)
* if supplied, replaces `post` with the contents from the provided file
* if the local file could not be loaded for whatever reason (ie.: not found), stubby falls back to `post` for matching.
* allows you to split up stubby data across multiple files instead of making one huge bloated main YAML
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

##### headers

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

### Response

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

##### status

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

##### body

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

##### file

* similar to `request.file`, but the contents of the file are used as the response `body`
* if the file could not be loaded, stubby falls back to the value stubbed in `body`
* if `body` was not stubbed, an empty string is returned by default
* it can be ascii of binary file (PDF, images, etc.). Please keep in mind, that file is preloaded upon stubby4j startup and its content is kept as a byte array in memory. In other words, response files are not read from the disk on demand, but preloaded.


```yaml
-  request:
      url: /
   response:
      file: extremelyLongJsonFile.json
```

##### headers

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

##### latency

* time to wait, in milliseconds, before sending back the response
* good for testing timeouts, or slow connections

```yaml
-  request:
      url: ^/hello/to/jupiter$
   response:
      latency: 800000
      body: Hello, World!
```

### Record and play

If `body` of the stubbed `response` contains a URL starting with http(s), stubby knows that it should record an HTTP response
from the provided URL (before rendering the stubbed response) and replay the recorded HTTP response on each subsequent call.

##### Example
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
##### Example explained

Upon successful HTTP request verification, properties of stubbed `request` (`method`, `url`, `headers`, `post` and `query`) are used to construct
an HTTP request to the destination URL specified in `body` of the stubbed `response`.

In the above example, stubby will record HTTP response received after submitting an HTTP GET request to the url below:
`http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=1600+Amphitheatre+Parkway,+Mountain+View,+CA`

##### Please note
* Recorded HTTP response is not persistable, but kept in memory only. In other words, upon stubby shutdown the recording is lost
* Make sure to specify in `response` `body` only the URL, without the path info. Path info should be specified in `request` `url`

### Dynamic token replacement in stubbed response

During HTTP request verification, you can leverage regex capturing groups as token values for dynamic token replacement in stubbed response.

##### Example
```yaml
-  request:
      method: [GET]
      url: ^/regex-fileserver/([a-z]+).html$

   response:
      status: 200
      file: ../html/<% url.1 %>.html
      
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
##### Example explained

The `url` regex `^/account/(\d{5})/category/([a-zA-Z]+)` has two defined capturing groups: `(\d{5})` and `([a-zA-Z]+)`, `query` regex has one defined capturing group `([a-zA-Z]+)`. In other words, a manually defined capturing group has parenthesis around it.

Although, the `headers` regex does not have capturing groups defined explicitly (no regex sections within parenthesis), its matched value is still accessible in a template (keep on reading!).

##### Token structure
The tokens in `response` `body` follow the format of `<%``PROPERTY_NAME``.``CAPTURING_GROUP_ID``%>`. If it is a token that should correspond to `headers` or `query` regex match, then the token structure would be as follows: `<%``HEADERS_OR_QUERY``.``KEY_NAME``.``CAPTURING_GROUP_ID``%>`. Whitespace is __allowed__ between the `<% ` & ` %>` and what's inside.

##### Numbering the tokens based on capturing groups without sub-groups
When giving tokens their ID based on the count of manually defined capturing groups within regex, you should start from `1`, not zero (zero reserved for token that holds __full__ regex match) from left to right. So the leftmost capturing group would be `1` and the next one to the right of it would be `2`, etc.

In other words `<% url.1 %>` and `<% url.2 %>` tokens correspond to two capturing groups from the `url` regex `(\d{5})` and `([a-zA-Z]+)`, while `<% query.date.1 %>` token corresponds to one capturing group `([a-zA-Z]+)` from the `query` `date` property regex.

##### Numbering the tokens based on capturing groups with sub-groups
In regex world, capturing groups can contain capturing sub-groups, as an example consider proposed `url` regex: `^/resource/``(``([a-z]{3})``-``([0-9]{3})``)``$`. In the latter example, the regex has three groups - a parent group `([a-z]{3}-[0-9]{3})` and two sub-groups within: `([a-z]{3})` & `([0-9]{3})`.

When giving tokens their ID based on the count of capturing groups, you should start from `1`, not zero (zero reserved for token that holds __full__ regex match) from left to right. If a group has sub-group within, you count the sub-group(s) first (also from left to right) before counting the next one to the right of the parent group.

In other words tokens `<% url.1 %>`, `<% url.2 %>` and `<% url.3 %>` correspond to the three capturing groups from the `url` regex (starting from left to right): `([a-z]{3}-[0-9]{3})`, `([a-z]{3})` and `([0-9]{3})`.

##### Tokens with ID zero
Tokens with ID zero can obtain __full__ match value from the regex they reference. In other words, tokens with ID zero do not care whether regex has capturing groups defined or not. For example, token `<% url.0 %>` will be replaced with the `url` __full__ regex match from `^/account/(\d{5})/category/([a-zA-Z]+)`. So if you want to access the `url` __full__ regex match, respectively you would use token `<% url.0 %>` in your template.

Another example, would be the earlier case where `headers` `custom-header` property regex does not have capturing groups defined within. Which is fine, since the `<% headers.custom-header.0 %>` token corresponds to the __full__ regex match in the `header` `custom-header` property regex: `[0-9]+`.

It is also worth to mention, that the __full__ regex match value replacing token `<% query.date.0 %>`, would be equal to the regex capturing group value replacing `<% query.date.1 %>`. This is due to how the `query` `date` property regex is defined - the one and only capturing group in the `query` `date` regex, is also the __full__ regex itself.

##### Where to specify the template
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

##### When token interpolation happens
After successful HTTP request verification, if your `body` or contents of local file from `file` contain tokens - the tokens will be replaced just before rendering HTTP response.

##### Troubleshooting
* Make sure that the regex you used in your stubby4j configuration actually does what it suppose to do. Validate that it works before using it in stubby4j
* Make sure that the regex has capturing groups for the parts of regex you want to capture as token values. In other words, make sure that you did not forget the parenthesis within your regex if your token IDs start from `1`
* Make sure that you are using token ID zero, when wanting to use __full__ regex match as the token value
* Make sure that the token names you used in your template are correct: check that property name is correct, capturing group IDs, token ID of the __full__ match, the `<% ` and ` %>`

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

### The admin portal

The admin portal is a RESTful(ish) endpoint running on `localhost:8889`. Or wherever you described through stubby's command line args.

##### Supplying endpoints to stubby

Submit `POST` requests to `localhost:8889` or load a data-file (using -d / --data flags) with the following structure for each endpoint:

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


##### YAML (file only or POST/PUT)
```yaml
-  request:
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

##### JSON (file or POST/PUT)

```json
[
  {
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

##### Getting the current list of stubbed endpoints

Performing a `GET` request on `localhost:8889` will return a YAML list of all currently saved responses. It will reply with `204 : No Content` if there are none saved.

Performing a `GET` request on `localhost:8889/<id>` will return the YAML object representing the response with the supplied id.

##### The status page

You can also view the currently configured endpoints by going to `localhost:8889/status`

##### Refreshing stubbed data via an endpoint

If for some reason you do not want/cannot/not able to use `--watch` flag when starting stubby4j (or cannot restart stubby),
you can submit `GET` request to `localhost:8889/refresh` (or load it in a browser) in order to refresh the stubbed data.

##### Changing existing endpoints

Perform `PUT` requests in the same format as using `POST`, only this time supply the id in the path. For instance, to update the response with id 4 you would `PUT` to `localhost:8889/4`.

##### Deleting endpoints

Send a `DELETE` request to `localhost:8889/<id>`


### The stubs portal

Requests sent to any url at `localhost:8882` (or wherever you told stubby to run) will search through the available endpoints and, if a match is found, respond with that endpoint's `response` data

##### How endpoints are matched

For a given endpoint, stubby only cares about matching the properties of the request that have been defined in the YAML. The exception to this rule is `method`; if it is omitted it is defaulted to `GET`.

For instance, the following will match any `POST` request to the root url:

```yaml
-  request:
      url: /
      method: POST
   response: {}
```

The request could have any headers and any post body it wants. It will match the above.

Pseudocode:

```
for each <endpoint> of stored endpoints {

   for each <property> of <endpoint> {
      if <endpoint>.<property> != <incoming request>.<property>
         next endpoint
   }

   return <endpoint>
}
```


### Programmatic API

##### Starting and using stubby4j with the help of StubbyClient

```java
   /**
    * Starts stubby using default ports of Stubs (8882), Admin (8889) and SslStubs portals (7443) on localhost.
    *
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
    * @throws Exception
    */
   public void startJetty(final String yamlConfigurationFilename) throws Exception

   /**
    * Starts stubby using default ports of Admin (8889) and SslStubs portals (7443), and given Stubs portal port  on localhost.
    *
    * @param stubsPort                 Stubs portal port
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final String yamlConfigurationFilename) throws Exception

   /**
    * Starts stubby using default port of SslStubs (7443), and given Stubs and Admin portals ports  on localhost.
    *
    * @param stubsPort                 Stubs portal port
    * @param adminPort                 Admin portal port
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final int adminPort, final String yamlConfigurationFilename) throws Exception

   /**
    * Starts stubby using given Stubs, SslStubs and Admin portals ports on localhost.
    *
    * @param stubsPort                 Stubs portal port
    * @param tlsPort                   TLS Stubs portal port
    * @param adminPort                 Admin portal port
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final int tlsPort, final int adminPort, final String yamlConfigurationFilename) throws Exception

   /**
    * Starts stubby using default port of SslStubs (7443), and given Stubs and Admin portals ports on a given host address.
    *
    * @param stubsPort                 Stubs portal port
    * @param adminPort                 Admin portal port
    * @param addressToBind             Address to bind Jetty
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final int adminPort, final String addressToBind, final String yamlConfigurationFilename) throws Exception

   /**
    * Starts stubby using given Stubs, SslStubs, Admin portals ports and host address.
    *
    * @param stubsPort                 Stubs portal port
    * @param tlsPort                   TLS Stubs portal port
    * @param adminPort                 Admin portal port
    * @param addressToBind             Address to bind Jetty
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file.
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final int tlsPort, final int adminPort, final String addressToBind, final String yamlConfigurationFilename) throws Exception

   /**
    * Starts stubby using given Stubs, TlsStubs, Admin portals ports and host address without YAML configuration file.
    *
    * @param stubsPort                 Stubs portal port
    * @param tlsPort                   TLS Stubs portal port
    * @param adminPort                 Admin portal port
    * @param addressToBind             Address to bind Jetty
    * @throws Exception
    */
   public void startJettyYamless(final int stubsPort, final int tlsPort, final int adminPort, final String addressToBind) throws Exception

   /**
    * Stops Jetty if it is up
    *
    * @throws Exception
    */
   public void stopJetty() throws Exception

   /**
    * Makes GET HTTP request to stubby
    *
    * @param host      host that stubby4j is running on
    * @param uri       URI for the HTTP request
    * @param stubsPort port that stubby4j Stubs is running on
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGet(final String host, final String uri, final int stubsPort) throws Exception

   /**
    * Makes GET HTTP request to stubby over TLS on stubby4j default TLS port: 7443
    *
    * @param host host that stubby4j is running on
    * @param uri  URI for the HTTP request
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetOverSsl(final String host, final String uri) throws Exception

   /**
    * Makes GET HTTP request to stubby over TLS on stubby4j
    *
    * @param host host that stubby4j is running on
    * @param uri  URI for the HTTP request
    * @param port TLS port
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetOverSsl(final String host, final String uri, final int port) throws Exception

   /**
    * Makes GET HTTP request to stubby over TLS on stubby4j default TLS port: 7443
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host          host that stubby4j is running on
    * @param uri           URI for the HTTP request
    * @param port          TLS port
    * @param authorization {@link Authorization} object holding the HTTP header authorization type & value
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetOverSsl(final String host, final String uri, final int port, final Authorization authorization) throws Exception

   /**
    * Makes GET HTTP request to stubby
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host          host that stubby4j is running on
    * @param uri           URI for the HTTP request
    * @param stubsPort     port that stubby4j Stubs is running on
    * @param authorization {@link Authorization} object holding the HTTP header authorization type & value
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGet(final String host, final String uri, final int stubsPort, final Authorization authorization) throws Exception

   /**
    * Makes GET HTTP request to stubby running on default host and port - localhost:8882
    *
    * @param uri URI for the HTTP request
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetUsingDefaults(final String uri) throws Exception

   /**
    * Makes GET HTTP request to stubby running on default host and port - localhost:8882.
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param uri           URI for the HTTP request
    * @param authorization {@link Authorization} object holding the HTTP header authorization type & value
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetUsingDefaults(final String uri, final Authorization authorization) throws Exception

   /**
    * Makes POST HTTP request to stubby
    *
    * @param host      host that stubby4j is running on
    * @param uri       URI for the HTTP request
    * @param stubsPort port that stubby4j Stubs is running on
    * @param payload   data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPost(final String host, final String uri, final int stubsPort, final String payload) throws Exception

   /**
    * Makes POST HTTP request to stubby
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host          host that stubby4j is running on
    * @param uri           URI for the HTTP request
    * @param stubsPort     port that stubby4j Stubs is running on
    * @param authorization {@link Authorization} object holding the HTTP header authorization type & value
    * @param payload       data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPost(final String host, final String uri, final int stubsPort, final Authorization authorization, final String payload) throws Exception

   /**
    * Makes POST HTTP request to stubby running on default host and port - localhost:8882
    *
    * @param uri  URI for the HTTP request
    * @param payload data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPostUsingDefaults(final String uri, final String payload) throws Exception

   /**
    * Makes POST HTTP request to stubby running on default host and port - localhost:8882.
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param uri           URI for the HTTP request
    * @param payload       data to POST to the server
    * @param authorization {@link Authorization} object holding the HTTP header authorization type & value
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPostUsingDefaults(final String uri, final String payload, final Authorization authorization) throws Exception

   /**
    * Makes PUT HTTP request to stubby
    * Also can set basic authorisation HTTP header using encoded credentials (if provided).
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host          host that stubby4j is running on
    * @param uri           URI for the HTTP request
    * @param stubsPort     port that stubby4j Stubs is running on
    * @param authorization {@link Authorization} object holding the HTTP header authorization type and value (can be null)
    * @param payload       data to PUT to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPut(final String host, final String uri, final int stubsPort, final Authorization authorization, final String payload) throws Exception

   /**
    * Makes PUT HTTP request to stubby over TLS on stubby4j default TLS port: 7443
    *
    * Also can set basic authorisation HTTP header using encoded credentials (if provided).
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host          host that stubby4j is running on
    * @param uri           URI for the HTTP request
    * @param stubsPort     port that stubby4j Stubs is running on
    * @param authorization {@link Authorization} object holding the HTTP header authorization type and value (can be null)
    * @param payload       data to PUT to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPutOverSsl(final String host, final String uri, final int stubsPort, final Authorization authorization, final String payload) throws Exception

   /**
    * Makes DELETE HTTP request to stubby
    * Also can set basic authorisation HTTP header using encoded credentials (if provided).
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host          host that stubby4j is running on
    * @param uri           URI for the HTTP request
    * @param stubsPort     port that stubby4j Stubs is running on
    * @param authorization {@link Authorization} object holding the HTTP header authorization type and value (can be null)
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doDelete(final String host, final String uri, final int stubsPort, final Authorization authorization) throws Exception

   /**
    * Makes DELETE HTTP request to stubby over TLS on stubby4j default TLS port: 7443
    *
    * Also can set basic authorisation HTTP header using encoded credentials (if provided).
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host          host that stubby4j is running on
    * @param uri           URI for the HTTP request
    * @param port          TLS port
    * @param authorization {@link Authorization} object holding the HTTP header authorization type and value
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doDeleteOverSsl(final String host, final String uri, final int port, final Authorization authorization) throws Exception

   /**
    * Updated stubbed data with new data. This method creates a POST request to Admin portal
    *
    * @param url       fully constructed URL which included HTTP scheme, host and port
    * @param stubsData data to post
    */
   public StubbyResponse updateStubbedData(final String url, final String stubsData) throws Exception

   /**
    * Makes HTTP request to stubby.
    *
    * @param scheme HTTP protocol scheme, HTTP or HTTPS
    * @param method HTTP method, currently supported: GET, HEAD, PUT, POST
    * @param host   host that stubby4j is running on
    * @param uri    URI for the HTTP request
    * @param port   port that stubby4j Stubs is running on
    * @param post   data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse makeRequest(final String scheme,
                                     final String method,
                                     final String host,
                                     final String uri,
                                     final int port,
                                     final String post) throws Exception

   /**
    * Makes HTTP request to stubby.
    *
    * @param scheme HTTP protocol scheme, HTTP or HTTPS
    * @param method HTTP method, currently supported: GET, HEAD, PUT, POST
    * @param host   host that stubby4j is running on
    * @param uri    URI for the HTTP request
    * @param port   port that stubby4j Stubs is running on
    * @param post   data to POST to the server
    * @param authorization {@link Authorization} object holding the HTTP header authorization type & value
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse makeRequest(final String scheme,
                                     final String method,
                                     final String host,
                                     final String uri,
                                     final int port,
                                     final String post,
                                     final Authorization authorization) throws Exception
```


### Change log

##### 3.3.1-SNAPSHOT

##### 3.3.0
* Allow callers to wait for the StubbyClient to finish (Oliver Weiler, https://github.com/helpermethod)
* Serving response files from a local path constructed with regex tokens from the stubbed request (Radek Ostrowski, https://github.com/radek1st)

##### 3.2.3
* Dumping more debug information to the console if `--debug` option is on, also for successfully matched requests
* Added support for `PUT` and `DELETE` methods in `StubbyClient` class

##### 3.1.3
* If POST'ed data type is `application/json`, the comparison of stubbed to posted data will be done using JSON entities with non-strict checking (content ordering wont matter, as long as it is the same)
* If POST'ed data type is `application/xml`, the comparison of stubbed to posted data will be done using XML entities with non-strict checking (element & attribute ordering wont matter, as long as content is the same)

##### 3.0.3
* Added support for custom authorization type header with the help of the new `header` property `authorization-custom`
* Fixed issue #43 (Live refresh in response sequence only for first response)

##### 3.0.2
* Added support for Bearer Token authorization with the help of the new `header` property `authorization-bearer`
* Renamed existing `header` property `authorization` to `authorization-basic`
* Some changes around the programmatic APIs in `StubbyClient` class due to the above changes
* Respective changes in the current README due to the above changes

##### 3.0.1
* Upgraded Jetty to v9.2.10.v20150310
* Added `--debug` option that dumps incoming raw HTTP request to the console
* Added `--disable_admin_portal` option that does not configure Admin portal for stubby
* Added `--disable_ssl` option that does not enable SSL for stubby
* Added a new API to start stubby programmatically without specifying a YAML file `StubbyClient.startJettyYamless(...)`
* Added a new `FaviconHandler` to handle requests for `favicon.ico` under the context root

##### 3.0.0
* Built using Java v1.7.0_76
* Updated Jetty to v9.2.9.v20150224 (requires at least JRE v1.7.0_76: [Issue with Java v1.7.0_04](http://dev.eclipse.org/mhonarc/lists/jetty-users/msg05635.html))

##### 2.0.22
* Built using Java v1.7.0_04
* Cleaned up project Gradle configuration 
* Updated Gradle configuration to be compatible with Gradle v2.2.1 & Gradle Nexus plugin v2.3
* Updated all (except Jetty) dependencies to their latest versions (as of May 10th, 2015)

##### 2.0.21
* Added console outputs for record & play functionality

##### 2.0.20
* Replacing all hardcoded `\n` with dynamically generated system line break characters

##### 2.0.19
* Record&Play is now more intelligent: when stubbed `request` is matched, its stubbed properties (`method`, `url`, `headers`, `post` and `query`) are used to construct HTTP request to the recordable destination URL provided in stubbed `response` body [ENHANCEMENT]
* Added a workaround a limitation in SnakeYAML v1.13 used by stubby (it has limited JSON support, not all the JSON documents can be parsed) where it cannot parse escaped forward slashes in JSON [BUG]
* Refreshing Admin status page was changing sequenced response counter ID [BUG]
* Replaced hardcoded Unix new line character '\n' in YamlBuilderTest that caused the tests to fail on Windows [BUG]
* Admin status page now shows what is the next sequenced response in the sequence queue [ENHANCEMENT]
* Supporting HTTP requests with empty query params: `/uri?some_param=` or `/uri?some_param` [ENHANCEMENT]

##### 2.0.18
* When `--data` file was just a relative filename without parent directory, NPE was thrown when Admin portal status page was loaded [BUG]
* Configured jetty GZIP handler for static resources and Stubs & Admin portals [ENHANCEMENT]
* Added resource hit stats section to Admin portal status page [ENHANCEMENT]

##### 2.0.17
* Added a verification check in StubbyManager to make sure that Jetty has been started or shut down [ENHANCEMENT]
* Upgraded SnakeYAML dependency library to v1.13 [ENHANCEMENT]
* Admin status page: displaying YAML snippet for a given `request` or `response` separately instead of pairing them up [ENHANCEMENT]
* Admin status page: displaying metadata of loaded external files (if any) [ENHANCEMENT]
* Admin status page: displaying Ajax response in a JS popup, instead of injecting the Ajax response into HTML table [ENHANCEMENT]
* A bunch of cosmetic changes on Admin status page [COSMETICS]

##### 2.0.16
* Displaying stubby JAR: version, its classpath location, built date, up-time, its input args and Java input args on status page [ENHANCEMENT]
* Displaying stubby heap/non-heap memory usage on status page [ENHANCEMENT]
* Added an option on admin status page to display YAML snippet for a given request & response pair [ENHANCEMENT]
* Making sure that 'x-stubby-resource-id' header is always the first item in the stubbed headers on status page [ENHANCEMENT]
* Changed colors of status page [COSMETICS]

##### 2.0.15
* When creating template token names for `query` or `headers` regex matches, the name format to be followed should be `headers.key_name.ID` or `query.key_name.ID` [ENHANCEMENT]

##### 2.0.14
* Whitespace was not allowed between the `<% ` & ` %>` and what's inside when specifying template tokens for dynamic token replacement in stubbed response [BUG]
* Regex matches were stored against incorrect token names for `query` and `headers` regexes [BUG]
* Renamed command line arg `--ssl` to `--tls` to reduce the confusion when having another command line arg that starts with letter `s`, like `--stubs` [ENHANCEMENT]
* Added command line arg `--version` that prints current stubby4j version to the console [ENHANCEMENT]

##### 2.0.13
* Dynamic token replacement in stubbed response, by leveraging regex capturing groups as token values during HTTP request verification [FEATURE]

##### 2.0.12
* Removed flag `--watch_sleep_time`. The `--watch` flag can now accept an optional arg value which is the watch scan time in milliseconds. If milliseconds is not provided, the watch scans every 100ms [ENHANCEMENT]
* Added additional API to start Jetty via StubbyClient by specifying an address to bind [ENHANCEMENT]

##### 2.0.11
* `--watch` flag sleep time is now configurable via `--watch_sleep_time` and defaults to `100ms` if `--watch_sleep_time` is not provided [ENHANCEMENT]
* Added a `GET` endpoint on Admin portal `localhost:8889/refresh` for refreshing stubbed data [ENHANCEMENT]

##### 2.0.10
* Record & Replay. The HTTP traffic is recorded on the first call to stubbed `uri` and subsequent calls will play back the recorded HTTP response, without actually connecting to the external server [FEATURE]

##### 2.0.9
* Ensuring that Admin portal status page loads fast by not rendering stubbed response content which slows down page load. User can invoke Ajax request to fetch the desired response content as needed [ENHANCEMENT]
* Pre-setting header `x-stubby-resource-id` during YAML parse time, instead of on demand. This way resource IDs are viewable on Admin status page [ENHANCEMENT]
* Making sure that header `x-stubby-resource-id` is recalculated accordingly after stubbed data in memory has changed (due to reset, or deletion etc.) [BUG]
* Added date stamp to live reload success message [COSMETICS]

##### 2.0.8
* Making sure that every stubbed response returned to the client contains its resource ID in the header `x-stubby-resource-id`. The latter is useful if the returned resource needs to be updated at run time by ID via Admin portal [FEATURE]

##### 2.0.7
* Force regex matching only everywhere (url, query, post, headers, etc.) to avoid confusion and unexpected behaviour, with default fallback to simple full-string match (Michael England) [ENHANCEMENT]

##### 2.0.6
* Live YAML scan now also check for modifications to external files referenced from main YAML [ENHANCEMENT]
* YAML parsing logic revisited [COSMETICS]
* Code cleanup [COSMETICS]

##### 2.0.5
* Added ability to specify sequence of responses on the same URI using `file` (Prakash Kandavel) [ENHANCEMENT]
* Minor code clean up [COSMETICS]
* Documentation update [COSMETICS]

##### 2.0.4
* Making sure that operations starting up stubby and managing stubbed data are atomic  [ENHANCEMENT]

##### 2.0.3
* Typo in test was giving wrong indication that when `file` not set, stubbed response fallsback to `body` [BUG]
* Eliminated implicit test to test dependencies in AdminPortalTest that was causing issues when running the tests under JDK 1.7 [BUG]
* Added convenience method in StubbyClient `updateStubbedData` [ENHANCEMENT]

##### 2.0.2
* Stubbed request HTTP header names were not lower-cased at the time of match [BUG]
* Doing GET on Admin portal `/` will display all loaded stub data in a YAML format in the browser [FEATURE]
* Doing GET on Admin portal `/<id>` will display loaded stub data matched by provided index in a YAML format in the browser [FEATURE]
* Doing DELETE on Admin portal `/<id>` will delete stubbed request from the list of loaded stub requests using the index provided [FEATURE]
* Doing PUT on Admin portal `/<id>` will update stubbed request in the list of loaded stub requests using the index provided [FEATURE]
* When YAML is parsed, if `file` could not be loaded, the IOException is not thrown anymore. Instead a warning recorded in the terminal [ENHANCEMENT]
* When could not match submitted HTTP request to stubbed requests, the not found message is much more descriptive [ENHANCEMENT]
* URI for registering new stub data programmatically via POST on Admin portal was changed from `/stubdata/new` to `/` [COSMETICS]
* URI for getting loaded stub data status was changed from `/ping` to `/status` on Admin portal [COSMETICS]
* Updated to SnakeYAML v1.12 [COSMETICS]
* Updated default response message when response content could not be loaded from `file` [ENHANCEMENT]
* Documentation refinement [COSMETICS]

##### 2.0.1
* Every ```url``` is treated as a regular expression now [ENHANCEMENT]
* ANSI logging in the terminal was working only for HTTP requests with status 200 [BUG]
* Documentation refinement [COSMETICS]

##### 2.0.0
* Mainly backend code improvements: A lot of refactoring for better code readability, expanding test coverage [COSMETICS]

##### 1.0.63
* Added ability to specify sequence of stub responses for the same URI, that are sent to the client in the loop [FEATURE]
* Configuration scan was not enabled, even if the ```--watch``` command line argument was passed [BUG]

##### 1.0.62
* Added ability to specify regex in stabbed URL for dynamic matching [FEATURE]
* A lot of minor fixes, refactorings and code cleaned up [COSMETICS]
* Documentation revisited and rewritten into a much clearer format [ENHANCEMENT]

##### 1.0.61
* Just some changes around unit, integration and functional tests. Code cleanup [COSMETICS]

##### 1.0.60
* stubby's admin page was generating broken hyper links if URL had single quotes [BUG]
* stubby is able to match URL when query string param was an array with elements within single quotes, ie: ```attributes=['id','uuid']``` [ENHANCEMENT]

##### 1.0.59
* stubby's admin page was not able to display the contents of stubbed response/request ```body```, ```post``` or ```file``` [BUG]
* stubby was not able to match URL when query string param was an array with quoted elements, ie: ```attributes=["id","uuid","created","lastUpdated","displayName","email"]``` [BUG]

##### 1.0.58
* Making sure that stubby can serve binary files as well as ascii files, when response is loaded using the ```file``` property [ENHANCEMENT]

##### 1.0.57
* Migrated the project from Maven to Gradle (thanks to [Logan McGrath](https://github.com/lmcgrath) for his feedback and assistance). The project has now a multi-module setup [ENHANCEMENT]

##### 1.0.56
* If `request.post` was left out of the configuration, stubby would ONLY match requests without a post body to it [BUG]
* Fixing `See Also` section of readme [COSMETICS]

##### 1.0.55
* Updated YAML example documentation [COSMETICS]
* Bug fix where command line options `mute`, `debug` and `watch` were overlooked [BUG]

##### 1.0.54
* Previous commit (`v1.0.53`) unintentionally broke use of embedded stubby [BUG]

### Roadmap
* Add support for OAuth in Record & Replay feature
* Scenarios where multiple endpoints correlate with each other based on the scenario. Useful in e2e testing where system brought to a certain state (maybe?)

### Authors
A number of people have contributed directly to stubby4j by writing
documentation or developing software.

1. Alexander Zagniotov <azagniotov@gmail.com>
2. Eric Mrak <enmrak@gmail.com>


### Kudos
A number of people have contributed to stubby4j by reporting problems, suggesting improvements or submitting changes. Special thanks fly out to the following **Ninjas** for their help, support and feedback

* Isa Goksu
* Eric Mrak
* Oleksandr Berezianskyi
* Sankalp Saxena
* Simon Brunning
* Ed Hewell
* Kenny Lin
* Logan McGrath


### See also
* **[stubby4net](https://github.com/mrak/stubby4net):** A .NET implementation of stubby
* **[stubby4node](https://github.com/mrak/stubby4node):** A node.js implementation of stubby


### Copyright
Yes. See COPYRIGHT for details

### License
MIT. See LICENSE for details
