[![Build Status](https://secure.travis-ci.org/azagniotov/stubby4j.png?branch=master)](http://travis-ci.org/azagniotov/stubby4j)

# stubby4j
A highly flexible & configurable tool for testing interactions of SOA applications with web services (REST, SOAP, WSDL etc.) over HTTP(S) protocol.
It is an actual HTTP server (stubby4j uses embedded Jetty) that acts like a real web service, ready for consumption by your code.

##### Why the word "stubby"?
It is a stub HTTP server after all, hence the "stubby". Also, in Australian slang "stubby" means _beer bottle_

## Table of Contents

* [Key Features](#key-features)
* [Why would a developer use stubby4j](#why-would-a-developer-use-stubby4j)
* [Why would a QA use stubby4j](#why-would-a-qa-use-stubby4j)
* [Building](#building)
* [stubby4j dependencies](#stubby4j-dependencies)
* [Adding stubby4j to your project](#adding-stubby4j-to-your-project)
* [Command-line Switches](#command-line-switches)
* [Endpoint Configuration HOWTO](#endpoint-configuration)
   * [Request](#request)
   * [Response](#response)
   * [Dynamic token replacement in stubbed response](#dynamic-token-replacement-in-stubbed-response)
* [The Admin Portal](#the-admin-portal)
* [The Stubs Portal](#the-stubs-portal)
* [Programmatic API](#programmatic-api)
* [Change Log](#change-log)
* [Roadmap](#roadmap)
* [Authors](#authors)
* [Kudos](#kudos)
* [See Also](#see-also)

## Key Features
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

## Why would a developer use stubby4j?
####You want to:
* Simulate responses from real server and don't care (or cannot) to go over the network
* Third party web service your application suppose to contract with is not ready yet
* Verify that your code makes HTTP requests with all the required parameters and/or headers
* Verify that your code correctly handles HTTP error codes
* You want to trigger response from the server based on the request parameters over HTTP or HTTPS
* Support for any of the available HTTP methods
* Simulate support for Basic Authorization
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

###### All this goodness in just under 1.5MB


## Building
stubby4j is a multi-module Gradle project

* IntelliJ IDEA users should run ```gradle cleanIdea idea``` in order to generate IntelliJ IDEA project files
* Eclipse users should run ```gradle cleanEclipse eclipse``` in order to generate Eclipse project files

Run `gradle` command to:
* Clean
* Run unit, integration and functional tests without Cobertura
* Build (the generated `stubby4j-x.x.x-SNAPSHOT.jar` will be located under `main/target/libs/`)

Run `gradle cobertura` command to:
* Clean
* Generate Cobertura report under the ```main``` module

Run `gradle clean check` command to:
* Clean
* Generate PMD report under the ```main``` module
* Generate JDepend report under the ```main``` module
* Generate Cobertura report under the ```main``` module


## Dependencies
stubby4j is a fat JAR, which contains the following dependencies:

* commons-cli-1.2.jar
* snakeyaml-1.12.jar
* javax.servlet-3.0.0.v201112011016.jar
* jetty-server-8.1.7.v20120910.jar
* jetty-continuation-8.1.7.v20120910.jar
* jetty-util-8.1.7.v20120910.jar
* jetty-io-8.1.7.v20120910.jar
* jetty-http-8.1.7.v20120910.jar

**stubby4j is also compatible with Jetty 7.x.x and servlet API v2.5**


## Adding stubby4j to your project
stubby4j is hosted on [Maven Central](http://search.maven.org) and can be added as a dependency in your POM.
Check Maven Central for the [latest version](http://search.maven.org/#search|ga|1|stubby4j) of stubby4j.
Keep in mind that __it takes 6-9 hours for the new release to appear on live Maven Central repo__. In other words, if you don't see the v2.0.13 there yet, its probably on its way ;)

#### Apache Maven
```xml
<dependency>
    <groupId>by.stub</groupId>
    <artifactId>stubby4j</artifactId>
    <version>2.0.13</version>
</dependency>
```

#### Apache Ivy
```xml
<dependency org="by.stub" name="stubby4j" rev="2.0.13" />
```

#### Apache Buildr
```xml
'by.stub:stubby4j:jar:2.0.13'
```

#### Gradle
```xml
compile 'by.stub:stubby4j:2.0.13'
```

## Command-line Switches
```
usage:
       java -jar stubby4j-2.0.13.jar [-a <arg>] [-d <arg>] [-h] [-k <arg>]
       [-l <arg>] [-m] [-p <arg>] [-s <arg>] [-t <arg>] [-w]
 -a,--admin <arg>      Port for admin portal. Defaults to 8889.
 -d,--data <arg>       Data file to pre-load endpoints. Valid YAML 1.1
                       expected.
 -h,--help             This help text.
 -k,--keystore <arg>   Keystore file for custom SSL. By default SSL is
                       enabled using internal keystore.
 -l,--location <arg>   Hostname at which to bind stubby.
 -m,--mute             Prevent stubby from printing to the console.
 -p,--password <arg>   Password for the provided keystore file.
 -s,--stubs <arg>      Port for stub portal. Defaults to 8882.
 -t,--ssl <arg>        Port for SSL connection. Defaults to 7443.
 -w,--watch            Periodically scans for changes in last modification
                       date of the main YAML and referenced external files
                       (if any). The flag can accept an optional arg value
                       which is the watch scan time in milliseconds. If
                       milliseconds is not provided, the watch scans every
                       100ms. If last modification date changed since the
                       last scan period, the stub configuration is
                       reloaded
```

## Endpoint Configuration

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

### request

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

#### file

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

### response

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

## Dynamic token replacement in stubbed response

During HTTP request verification, you can leverage regex capturing groups as token values for dynamic token replacement in stubbed response. Consider the following example:

```yaml
-  request:
      method: [GET]
      url: ^/account/(\d{5})/category/([a-zA-Z]+)
      query:
         date: "([a-zA-Z]+)"

   response:
      status: 200
      body: Returned invoice number# <%url.1%> in category '<%url.2%>' on the date '<%query.1%>'
```
The `url` regex `^/account/(\d{5})/category/([a-zA-Z]+)` has two capturing groups: `(\d{5})` and `([a-zA-Z]+)`, while `query` regex has one capturing group `([a-zA-Z]+)`. Please note that a capturing group has parenthesis around it.

The tokens in `response` `body` follow the format of `<%``PROPERTY_NAME``.``CAPTURING_GROUP_ID``%>`. In other words `<%url.1%>` and `<%url.2%>` tokens correspond to two capturing groups from `url` regex `(\d{5})` and `([a-zA-Z]+)`, while `<%query.1%>` token corresponds to one capturing group `([a-zA-Z]+)`.

__Keep in mind__, when counting capturing groups, you should start from `1`, not zero. Capturing group ID zero which is the entire regex match (ie. token `<%url.0%>`) is __not__ used at the moment.

You can specify template with tokens in both `body` as a string or using `file` by specifying template as external local file. When template is specified as `file`, the contents of the template from `file` will be replaced, __not__ the `file` path. After successful HTTP request verification, if your `body` or contents of local file from `file` contain tokens and your regex has capturing groups - the tokens will be replaced before rendering HTTP response content.

#### Troubleshooting
* Make sure that the regex you used in your stubby4j configuration actually does what it suppose to do. Validate that it works before using it in stubby4j
* Make sure that the regex has capturing groups for the parts of regex you want to capture as token values. In other words, make sure that you did not forget the parenthesis within your regex
* Make sure that the token names you used in your template, correspond to regex capturing groups (check property name, capturing group IDs, the `<%` and `%>`)


## The Admin Portal

The admin portal is a RESTful(ish) endpoint running on `localhost:8889`. Or wherever you described through stubby's options.

### Supplying Endpoints to Stubby

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


#### YAML (file only or POST/PUT)
```yaml
-  request:
      url: ^/path/to/something$
      method: POST
      headers:
         authorization: "bob:password"
         x-custom-header: "^this/is/\d/test"
      post: this is some post data in textual format
   response:
      headers:
         Content-Type: application/json
      latency: 1000
      status: 200
      body: You're request was successfully processed!

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


#### JSON (file or POST/PUT)
```json
[
  {
    "request": {
      "url": "^/path/to/something$",
      "post": "this is some post data in textual format",
      "headers": {
         "authorization": "bob:password"
      },
      "method": "POST"
    },
    "response": {
      "status": 200,
      "headers": {
        "Content-Type": "application/json"
      },
      "latency": 1000,
      "body": "You're request was successfully processed!"
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

### Getting the Current List of Stubbed Endpoints

Performing a `GET` request on `localhost:8889` will return a YAML list of all currently saved responses. It will reply with `204 : No Content` if there are none saved.

Performing a `GET` request on `localhost:8889/<id>` will return the YAML object representing the response with the supplied id.

#### The Status Page

You can also view the currently configured endpoints by going to `localhost:8889/status`

#### The Refresh Stubbed Data Endpoint

If for some reason you do not want/cannot/not able to use `--watch` flag when starting stubby4j (or cannot restart),
you can submit `GET` request to `localhost:8889/refresh` (or load it in a browser) in order to refresh the stubbed data.

### Changing Existing Endpoints

Perform `PUT` requests in the same format as using `POST`, only this time supply the id in the path. For instance, to update the response with id 4 you would `PUT` to `localhost:8889/4`.

### Deleting Endpoints

Send a `DELETE` request to `localhost:8889/<id>`


## The Stubs Portal

Requests sent to any url at `localhost:8882` (or wherever you told stubby to run) will search through the available endpoints and, if a match is found, respond with that endpoint's `response` data

### How Endpoints Are Matched

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


## Programmatic API

### Starting and using stubby4j with the help of StubbyClient

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
    * @param sslPort                   SSL Stubs portal port
    * @param adminPort                 Admin portal port
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final int sslPort, final int adminPort, final String yamlConfigurationFilename) throws Exception

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
    * @param sslPort                   SSL Stubs portal port
    * @param adminPort                 Admin portal port
    * @param addressToBind             Address to bind Jetty
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file.
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final int sslPort, final int adminPort, final String addressToBind, final String yamlConfigurationFilename) throws Exception

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
    * Makes GET HTTP request to stubby over SSL on stubby4j default SSL port: 7443
    *
    * @param host host that stubby4j is running on
    * @param uri  URI for the HTTP request
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetOverSsl(final String host, final String uri) throws Exception

   /**
    * Makes GET HTTP request to stubby over SSL on stubby4j
    *
    * @param host host that stubby4j is running on
    * @param uri  URI for the HTTP request
    * @param port SSL port
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetOverSsl(final String host, final String uri, final int port) throws Exception

   /**
    * Makes GET HTTP request to stubby over SSL on stubby4j default SSL port: 7443
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host               host that stubby4j is running on
    * @param uri                URI for the HTTP request
    * @param port               SSL port
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetOverSsl(final String host, final String uri, final int port, final String encodedCredentials) throws Exception

   /**
    * Makes GET HTTP request to stubby
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host               host that stubby4j is running on
    * @param uri                URI for the HTTP request
    * @param stubsPort          port that stubby4j Stubs is running on
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGet(final String host, final String uri, final int stubsPort, final String encodedCredentials) throws Exception

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
    * @param uri                URI for the HTTP request
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetUsingDefaults(final String uri, final String encodedCredentials) throws Exception

   /**
    * Makes POST HTTP request to stubby
    *
    * @param host      host that stubby4j is running on
    * @param uri       URI for the HTTP request
    * @param stubsPort port that stubby4j Stubs is running on
    * @param post      data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPost(final String host, final String uri, final int stubsPort, final String post) throws Exception

   /**
    * Makes POST HTTP request to stubby
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host               host that stubby4j is running on
    * @param uri                URI for the HTTP request
    * @param stubsPort          port that stubby4j Stubs is running on
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @param post               data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPost(final String host, final String uri, final int stubsPort, final String encodedCredentials, final String post) throws Exception

   /**
    * Makes POST HTTP request to stubby running on default host and port - localhost:8882
    *
    * @param uri  URI for the HTTP request
    * @param post data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPostUsingDefaults(final String uri, final String post) throws Exception

   /**
    * Makes POST HTTP request to stubby running on default host and port - localhost:8882.
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param uri                URI for the HTTP request
    * @param post               data to POST to the server
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPostUsingDefaults(final String uri, final String post, final String encodedCredentials) throws Exception

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
```


## Change Log
### 2.0.13
* Dynamic token replacement in stubbed response, by leveraging regex capturing groups as token values during HTTP request verification [FEATURE]

### 2.0.12
* Removed flag `--watch_sleep_time`. The `--watch` flag can now accept an optional arg value which is the watch scan time in milliseconds. If milliseconds is not provided, the watch scans every 100ms [ENHANCEMENT]
* Added additional API to start Jetty via StubbyClient by specifying an address to bind [ENHANCEMENT]

### 2.0.11
* `--watch` flag sleep time is now configurable via `--watch_sleep_time` and defaults to `100ms` if `--watch_sleep_time` is not provided [ENHANCEMENT]
* Added a `GET` endpoint on Admin portal `localhost:8889/refresh` for refreshing stubbed data [ENHANCEMENT]

### 2.0.10
* Record & Replay. The HTTP traffic is recorded on the first call to stubbed `uri` and subsequent calls will play back the recorded HTTP response, without actually connecting to the external server [FEATURE]

### 2.0.9
* Ensuring that Admin portal status page loads fast by not rendering stubbed response content which slows down page load. User can invoke Ajax request to fetch the desired response content as needed [ENHANCEMENT]
* Pre-setting header `x-stubby-resource-id` during YAML parse time, instead of on demand. This way resource IDs are viewable on Admin status page [ENHANCEMENT]
* Making sure that header `x-stubby-resource-id` is recalculated accordingly after stubbed data in memory has changed (due to reset, or deletion etc.) [BUG]
* Added date stamp to live reload success message [COSMETICS]

### 2.0.8
* Making sure that every stubbed response returned to the client contains its resource ID in the header `x-stubby-resource-id`. The latter is useful if the returned resource needs to be updated at run time by ID via Admin portal [FEATURE]

### 2.0.7
* Force regex matching only everywhere (url, query, post, headers, etc.) to avoid confusion and unexpected behaviour, with default fallback to simple full-string match (Michael England) [ENHANCEMENT]

### 2.0.6
* Live YAML scan now also check for modifications to external files referenced from main YAML [ENHANCEMENT]
* YAML parsing logic revisited [COSMETICS]
* Code cleanup [COSMETICS]

### 2.0.5
* Added ability to specify sequence of responses on the same URI using `file` (Prakash Kandavel) [ENHANCEMENT]
* Minor code clean up [COSMETICS]
* Documentation update [COSMETICS]

### 2.0.4
* Making sure that operations starting up stubby and managing stubbed data are atomic  [ENHANCEMENT]

### 2.0.3
* Typo in test was giving wrong indication that when `file` not set, stubbed response fallsback to `body` [BUG]
* Eliminated implicit test to test dependencies in AdminPortalTest that was causing issues when running the tests under JDK 1.7 [BUG]
* Added convenience method in StubbyClient `updateStubbedData` [ENHANCEMENT]

### 2.0.2
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

### 2.0.1
* Every ```url``` is treated as a regular expression now [ENHANCEMENT]
* ANSI logging in the terminal was working only for HTTP requests with status 200 [BUG]
* Documentation refinement [COSMETICS]

### 2.0.0
* Mainly backend code improvements: A lot of refactoring for better code readability, expanding test coverage [COSMETICS]

### 1.0.63
* Added ability to specify sequence of stub responses for the same URI, that are sent to the client in the loop [FEATURE]
* Configuration scan was not enabled, even if the ```--watch``` command line argument was passed [BUG]

### 1.0.62
* Added ability to specify regex in stabbed URL for dynamic matching [FEATURE]
* A lot of minor fixes, refactorings and code cleaned up [COSMETICS]
* Documentation revisited and rewritten into a much clearer format [ENHANCEMENT]

### 1.0.61
* Just some changes around unit, integration and functional tests. Code cleanup [COSMETICS]

### 1.0.60
* stubby's admin page was generating broken hyper links if URL had single quotes [BUG]
* stubby is able to match URL when query string param was an array with elements within single quotes, ie: ```attributes=['id','uuid']``` [ENHANCEMENT]

### 1.0.59
* stubby's admin page was not able to display the contents of stubbed response/request ```body```, ```post``` or ```file``` [BUG]
* stubby was not able to match URL when query string param was an array with quoted elements, ie: ```attributes=["id","uuid","created","lastUpdated","displayName","email"]``` [BUG]

### 1.0.58
* Making sure that stubby can serve binary files as well as ascii files, when response is loaded using the ```file``` property [ENHANCEMENT]

### 1.0.57
* Migrated the project from Maven to Gradle (thanks to [Logan McGrath](https://github.com/lmcgrath) for his feedback and assistance). The project has now a multi-module setup [ENHANCEMENT]

### 1.0.56
* If `request.post` was left out of the configuration, stubby would ONLY match requests without a post body to it [BUG]
* Fixing `See Also` section of readme [COSMETICS]

### 1.0.55
* Updated YAML example documentation [COSMETICS]
* Bug fix where command line options `mute`, `debug` and `watch` were overlooked [BUG]

### 1.0.54
* Previous commit (`v1.0.53`) unintentionally broke use of embedded stubby [BUG]

## Roadmap
* Add support for OAuth in Record & Replay feature
* Scenarios where multiple endpoints correlate with each other based on the scenario. Useful in e2e testing where system brought to a certain state
* Use dynamically generated identifier in the response

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


## See Also
* **[stubby4net](https://github.com/mrak/stubby4net):** A .NET implementation of stubby
* **[stubby4node](https://github.com/mrak/stubby4node):** A node.js implementation of stubby


## Copyright
See COPYRIGHT for details.
