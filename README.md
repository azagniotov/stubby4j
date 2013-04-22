# stubby4j
A stub HTTP server written in Java with embedded Jetty server

##### Why the word "stubby"?
It is a stub HTTP server after all, hence the "stubby". Also, in Australian slang "stubby" means _beer bottle_

## Table of Contents

* [Why would a developer use stubby4j](#why-would-a-developer-use-stubby4j)
* [Why would a QA use stubby4j](#why-would-a-qa-use-stubby4j)
* [Building](#building)
* [Dependencies](#dependencies)
* [Maven Central](#maven-central)
* [Command-line Switches](#command-line-switches)
* [YAML Configuration Explained](#yaml-configuration-explained)
* [How to Start stubby4j Programmatically](#how-to-start-stubby4j-programmatically)
* [Change Log](#change-log)
* [Authors](#authors)
* [Kudos](#kudos)
* [See Also](#see-also)


## Why would a developer use stubby4j?
####You want to:
* Simulate responses from real server and don't care (or cannot) to go over the network
* Verify that your code makes HTTP requests with all the required parameters and/or headers
* Verify that your code correctly handles HTTP error codes
* You want to trigger response from the server based on the request parameters over HTTP or HTTPS
* Support for any of the available HTTP methods
* Simulate support for Basic Authorization
* Support for HTTP 30x redirects
* Support for regular expressions (like mod_rewrite in Apache) in stubbed URIs for dynamic matching
* Trigger multiple responses based on multiple requests on the same URI
* Configure stub data using configuration file
* Configure stub data at runtime, without restarting the server by making a POST to an exposed endpoint
* Live tweak previously loaded and parsed configuration file to auto refresh the stub data WITHOUT restarting the server
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
stubby4j is a multi-module Gradle project. IntelliJ IDEA users should run ```gradle cleanIdea idea``` in order to generate IntelliJ IDEA project files. Eclipse users should run ```cleanEclipse eclipse``` in order to generate Eclipse project files.

Run `gradle` command to:
* Clean
* Build

Run `gradle testAll` command to:
* Clean
* Run unit, integration and functional tests in the specified order without Cobertura

Run `gradle clean cobertura` command to:
* Generate Cobertura report under the ```main``` module


## Dependencies
stubby4j is a fat JAR, which contains the following dependencies:

* commons-cli-1.2.jar
* snakeyaml-1.11.jar
* javax.servlet-3.0.0.v201112011016.jar
* jetty-server-8.1.7.v20120910.jar
* jetty-continuation-8.1.7.v20120910.jar
* jetty-util-8.1.7.v20120910.jar
* jetty-io-8.1.7.v20120910.jar
* jetty-http-8.1.7.v20120910.jar

**stubby4j is also compatible with Jetty 7.x.x and servlet API v3.0**


## Maven Central
stubby4j is hosted on [Maven Central](http://search.maven.org) and can be added as a dependency in your POM.
Check Maven Central for the [latest version](http://search.maven.org/#search|ga|1|stubby4j) of stubby4j

```xml
<dependency>
    <groupId>by.stub</groupId>
    <artifactId>stubby4j</artifactId>
    <version>x.x.xx</version>
</dependency>
```

## Command-line Switches
```
java -jar stubby4j-x.x.xx.jar [-a <arg>] [-d <arg>] [-h]
       [-k <arg>] [-l <arg>] [-m] [-p <arg>] [-s <arg>] [-t <arg>] [-w]
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
 -w,--watch            Reload datafile when changes are made.
```

## YAML Configuration Explained
<br />
When creating stubbed request/response data for stubby4j, the config data should be specified in valid YAML 1.1 syntax. Submit POST requests to ```http://<host>:<admin_port>/stubdata/new``` or load a data file (```-d``` or ```--data```) with the following structure for each endpoint:
<br />

#### Stub request and its properties

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>request</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.request</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>Describes the client's call to the server </td>
</tr>
</table>

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>method</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.request.method[*]</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Holds HTTP method verbs</li>
<li>If multiple verbs are defined, YAML array should be used
</li>
</ul></td>
</tr>
</table>
```
-  request:
      method: GET

   request:
      method: [GET, HEAD]

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>url</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.request.url</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>URI string</li>
<li>If you include query string in stubbed URI, the HTTP request WILL NOT match since stubbed URI compared only to URI from HTTP request. If you want to make string query params match, include them in the <i><b>query</b></i> key</li>
<li>Supports regular expressions (similiar to <i>mod_rewrite</i> in Apache) for dynamic matching. The regular expression must be a valid Java regex and able to be compiled by <i>java.util.regex.Pattern</i></li>
<li>When stubbing regular expression in <i><b>url</b></i>, string query params (if any) must be attached to the <i><b>url</b></i> unlike said in the first bullet point</li>
<li>The regular expression <b>must start with ^</b>, otherwise a full match will be performed using equals() without compiling <i>java.util.regex.Pattern</i></li>
</ul>
</td>
</tr>
</table>
```
-  request:
      url: /some/uri


-  request:
      url: /some/uri
      query:
         param: true
         anotherParam: false


-  request:
      method: GET
      url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\?paramOne=[a-zA-Z]{3,8}&paramTwo=[a-zA-Z]{3,8}


-  request:
      method: GET
      url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\?view=full&status=active


-  request:
      method: GET
      url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$


-  request:
      method: GET
      url: ^/(account|profile)/user/session/[a-zA-Z0-9]{32}/?
```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>headers</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.request.headers[*]</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Key/value map of HTTP headers the server should read from the request</li>
<li>If stubbed headers are a subset of headers in HTTP request, then the match is successful (<i>left outer join</i> concept)</li>
</ul>
</td>
</tr>
</table>
```
-  request:
      method: POST
      headers:
         content-type: application/json
         content-length: 80

   response:
      headers:
         content-type: application/json

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>query</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.request.query[*]</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Key/value map of query string params the server should read from the URI </li>
<li>The stubbed key (param name) must have the letter case as the query string param name, ie: HTTP request query string <i><b>paRamNaME=12 => paRamNaME: 12</b></i></li>
<li>The order of query string params does not matter. In other words the
<i><b>server.com?something=1&else=2</b></i> is the same as <i><b>server.com?else=2&something=1</b></i></li>
<li>If stubbed query params are a subset of query params in HTTP request, then the match is successful (<i>left outer join</i> concept)</li>
<li>query param can also be an array with double/single quoted/un-quoted elements: <i><b>attributes=["id","uuid"]</b></i> or <i><b>attributes=[id,uuid]</b></i>. Please note no spaces between the CSV</li>
</ul>
</td>
</tr>
</table>
```
-  request:
      url: /some/uri
      query:
         paramTwo: 12345
         paramOne: valueOne
      method: POST


-  request:
      method: GET
      url: /entity.find
      query:
         type_name: user
         client_id: id
         client_secret: secret
         attributes: '["id","uuid","created","lastUpdated","displayName","email","givenName","familyName"]'

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>post</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.request.post</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>String matching the textual body of the POST request.</li>
</ul>
</td>
</tr>
</table>
```
-  request:
      method: POST
      post: >
         {
            "name": "value",
            "param": "description"
         }


-  request:
      url: /some/uri
      post: this is some post data in textual format
```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>file</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.request.file</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>If specified (an absolute path or path relative to the YAML in <i><b>-d</b></i> or <i><b>--data</b></i>), returns the contents of the given file as the stubbed <i><b>request</b></i> POST content</li>
<li>If the file was not provided, stubby fallsback to value from <i><b>post</b></i> property</li>
<li>If <i><b>post</b></i> key was not stubbed, it is assumed that stubbed POST body was not provided at all</li>
<li>Use file for large POST content that otherwise inconvenient to configure as a one-liner or you do not want to pollute YAML config</li>
<li>Please keep in mind: <i><b>SnakeYAML</b></i> library (used by stubby4j) parser ruins multi-line strings by not preserving system line breaks. If <i><b>file</b></i> property is stubbed, the file content is loaded as-is, in other words - it does not go through SnakeYAML parser. Therefore its better to load big POST content for <i><b>request</b></i> using <b><i>file</i></b> attribute. Keep in mind, stubby4j stub server is dumb and does not use smart matching mechanism (ie:. don't match line separators or don't match any white space characters) - whatever you stubbed, must be POSTed exactly for successful match</li>
</ul>
</td>
</tr>
</table>
```
-  request:
      method: POST
      headers:
         content-type: application/json
      file: ../data/post-body-as-file.json
```

#### Stub response and its properties
<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>response</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.response[*]</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Describes stubby4j's response to the client</li>
<li>Can be a single response or a sequence of responses.</li>
<li>When sequenced responses configured, on each request to a URI, a subsequent response in the list will be sent to the client. The sequenced responses play in a cycle (loop). In other words: after the response sequence plays through, the cycle restarts on the next request.</li>
</ul>
</td>
</tr>
</table>
```
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
      url: /uri/with/single/sequenced/response

   response:
      -  status: 201
         headers:
            content-stype: application/json
         body: Still going strong!

```


<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>status</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.response[*].status</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Numerical HTTP status code (200 for OK, 404 for NOT FOUND, etc.)</li></ul></td>
</tr>
</table>
```
  response:
      status: 200

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>headers</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.response[*].headers[*]</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Key/value map of HTTP headers the stubby4j should send with the response</li>
</ul>
</td>
</tr>
</table>
```
  response:
      headers:
         content-type: application/json


   response:
      headers:
         content-type: application/pdf
         content-disposition: "attachment; filename=release-notes.pdf"
         pragma: no-cache

   response:
      status: 301
      headers:
         location: /some/other/uri
```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>latency</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.response[*].latency</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Delay in milliseconds stubby4j should wait before responding to the client</li>
</ul>
</td>
</tr>
</table>
```
  response:
      latency: 1000

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>body</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.response[*].body</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>String matching the textual body of the response body</li>
</ul>
</td>
</tr>
</table>
```
   response:
      body: OK
      status: 200


   response:
      latency: 1000
      body: >
         This is a text response, that can span across
         multiple lines as long as appropriate indentation is in place.
      status: 200


   response:
      status: 200
      body: >
         {"status": "hello world"}
      headers:
         content-type: application/json


   response:
      headers:
         content-type: application/xml
         access-control-allow-origin: "*"
      latency: 1000
      body: >
         <?xml version="1.0" encoding="UTF-8"?>
            <Response>
            <Play loop="10">https://api.twilio.com/cowbell.mp3</Play>
         </Response>
      status: 200
```
<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>file</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.response[*].file</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>If specified (an absolute path or path relative to the YAML in <i><b>-d</b></i> or <i><b>--data</b></i>), returns the contents of the given file as the HTTP response body</li>
<li>If the <i><b>file</b></i> was not provided, stubby fallsback to value from <i><b>body</b></i> property </li>
<li>If <i><b>body</b></i> was not provided, an empty string is returned by default</li>
<li>Can be ascii of binary file (PDF, images, etc.). Please keep in mind, that file is preloaded upon stubby4j startup and its content is kept in byte array in memory. In other words, response files are not read from the disk on demand, but preloaded.</li>
</ul>
</td>
</tr>
</table>
```
-  response:
      status: 201
      headers:
         content-type: application/json
      file: ../data/response-body-as-file.json
```


## How to Start stubby4j Programmatically

```java
private static StubbyClient stubbyClient;

@BeforeClass
public static void beforeClass() throws Exception {
   final URL url = StubbyClientIntegrationTest.class.getResource("/atom-feed.yaml");

   ANSITerminal.mute = true;
   stubbyClient = new StubbyClient();
   stubbyClient.startJetty(url.getFile());
}
.
.
.
@AfterClass
public static void afterClass() throws Exception {
   stubbyClient.stopJetty();
}
```

OR

```java
@BeforeClass
public static void beforeClass() throws Exception {
   int clientPort = 8882;
   int adminPort = 8889;
   final URL url = SomeClass.class.getResource("/config.yaml");
   stubbyClient = new StubbyClient();
   stubbyClient.startJetty(clientPort, adminPort, url.getFile());
}

OR

@BeforeClass
public static void beforeClass() throws Exception {
   int clientPort = 8888;
   int sslPort = 4993;
   int adminPort = 9999;
   final URL url = SomeClass.class.getResource("/config.yaml");
   stubbyClient = new StubbyClient();
   stubbyClient.startJetty(clientPort, sslPort, adminPort, url.getFile());
}
.
.
.
@AfterClass
public static void afterClass() throws Exception {
   stubbyClient.stopJetty();
}
```

#### How to Make HTTP Request to stubby4j at Runtime Using Client

```java
 @Test
   public void doGet_ShouldMakeSuccessfulGet() throws Exception {

      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doGet(host, uri, port);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", stubbyResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGet() throws Exception {

      final String uri = "/item/1";

      final StubbyResponse stubbyResponse = stubbyClient.doGetUsingDefaults(uri);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", stubbyResponse.getContent());
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doGet(host, uri, port, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", stubbyResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));
      final String uri = "/item/auth";

      final StubbyResponse stubbyResponse = stubbyClient.doGetUsingDefaults(uri, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", stubbyResponse.getContent());
   }

   @Test
   public void doGetOverSsl_ShouldMakeSuccessfulGet() throws Exception {

      final String host = "localhost";
      final String uri = "/item/1";
      final int sslPort = 4993;

      final StubbyResponse stubbyResponse = stubbyClient.doGetOverSsl(host, uri, sslPort);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", stubbyResponse.getContent());
   }

   @Test
   public void doGetOverSsl_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/auth";
      final int sslPort = 4993;

      final StubbyResponse stubbyResponse = stubbyClient.doGetOverSsl(host, uri, sslPort, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, "post body");

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("Got post response", stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/submit";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;
      final String post = "{\"action\" : \"submit\"}";

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, encodedCredentials, post);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("OK", stubbyResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String uri = "/item/submit";
      final String post = "{\"action\" : \"submit\"}";

      final StubbyResponse stubbyResponse = stubbyClient.doPostUsingDefaults(uri, post, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("OK", stubbyResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost() throws Exception {
      final String uri = "/item/1";

      final StubbyResponse stubbyResponse = stubbyClient.doPostUsingDefaults(uri, "post body");

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("Got post response", stubbyResponse.getContent());
   }


   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost_WhenEmptyPostGiven() throws Exception {
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";

      final StubbyResponse stubbyResponse = stubbyClient.doPostUsingDefaults(uri, "");

      Assert.assertEquals(HttpStatus.CREATED_201, stubbyResponse.getResponseCode());
      Assert.assertEquals("OK", stubbyResponse.getContent());
   }
```

#### How to Make HTTP Request with Basic Authorization to stubby4j at Runtime Using Client


In order to configure Basic Authorization, you need to specify username followed by `:`, followed by password
as `authorization` header value in the stub `request` configuration:

```yaml
-  request:
      method: GET
      url: /invoice/123
      headers:
         authorization: bob:secret
   response:
      status: 200
      body: This is a response for 123,
```

Upon parsing of the stub config data, base64 encoding scheme will be applied to the provided `username:password` value, which
will be prepended with the word "Basic". The final result will conform to HTTP header `Authorization` format, eg.: `Basic Ym9iOnNlY3JldA==`


```java
   @Test
   public void doPost_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/submit";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;
      final String post = "{\"action\" : \"submit\"}";

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, encodedCredentials, post);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("OK", stubbyResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String uri = "/item/submit";
      final String post = "{\"action\" : \"submit\"}";

      final StubbyResponse stubbyResponse = stubbyClient.doPostUsingDefaults(uri, post, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, stubbyResponse.getResponseCode());
      Assert.assertEquals("OK", stubbyResponse.getContent());
   }
```

#### How to Configure HTTP Stub Data at Runtime

In order to configure HTTP request and response stubs at runtime, you need to POST
stub config data to the following end point: `http://<host>:<admin_port>/stubdata/new`


###### The POSTed stub data should have the same structure as the config data from YAML configuration, eg.:

```yaml
-  request:
      headers:
         authorization: bob:secret
      method: GET
      url: /some/uri
   response:
      headers:
         content-type: application/json
         access-control-allow-origin: "*"
      status: 200
      body: >
         {"message" : "This is a response for 123"}

-  request:
      method: POST
      url: /some/uri
      post: some post body context as a plain text
   response:
      status: 200
      body: This is a response for 123
```

```java
   @Test
   public void doPost_ShouldMakeSuccessfulPostToCreateStubData() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.RESOURCE_STUBDATA_NEW;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, content);

      Assert.assertEquals(HttpStatus.CREATED_201, stubbyResponse.getResponseCode());
      Assert.assertEquals("Configuration created successfully", stubbyResponse.getContent());
   }
```


##### Please note:
1. New POSTed data will purge the previous stub data from stubby4j memory.
2. POSTed stub data will be lost on server restart. If you want to use the same stub data all over again, load it from configuration file


#### How to Live Tweak Stub Data at Runtime
It is possible to make updates to already loaded and parsed YAML configuration file.
Just tweak the file and stubbed data will be refreshed within 3 seconds (assuming you did not introduce YAML parse errors)


## Change Log
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
