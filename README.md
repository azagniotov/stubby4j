[![Build Status](https://secure.travis-ci.org/azagniotov/stubby4j.png?branch=master)](http://travis-ci.org/azagniotov/stubby4j)

# stubby4j

A Java-based stub and mock HTTP server with embedded Jetty.

### Why would a developer use stubby4j?

* You want to simulate responses from real server and don't care (or cannot) to go over the network
* You want to verify that your code makes HTTP requests with all the required parameters and/or headers
* You want to verify that your code correctly handles HTTP error codes
* You want to trigger response from the server based on the request parameters over HTTP or HTTPS
* You want support for any of the available HTTP methods
* You want support for Basic Authorization
* You want to trigger multiple responses based on multiple requests on the same URI
* You want to easily configure stub data using configuration file
* You want to easily configure stub data at runtime, without restarting the server by making a POST to an exposed endpoint
* You want to easily provide canned answers in your contract/integration tests
* You want to enable delayed responses for performance and stability testing
* You don't want to spend time coding for the above requirements and just want to concentrate on the task at hand

### Why would a QA use stubby4j?

* Specifiable mock responses to simulate page conditions without real data.
* Easily swappable data config files to run different data sets and responses.
* All-in-one stub server to handle mock data with less need to upkeep code for test generation

##### All this goodness in just under 1.2MB
_______________________________________________

Why "stubby"?
=============

It is a stub HTTP server after all, hence "stubby". Also, in Australian slang "stubby" means beer bottle
________________________________________________

YAML Configuration
==================

When creating request/response data for the stub server, the config data should be specified in YAML-like syntax:

```
httplifecycle:
   request:
      method: GET
      url: /invoice/123
      headers:
         authorization: bob:secret
   response:
      status: 200
      body: This is a response for 123

httplifecycle:
   request:
      method: GET
      url: /invoice/567
   response:
      headers:
         content-type: application/json
         access-control-allow-origin: *
      body: {"name" : "stubby4j"}
      latency: 5000
      status: 503

httplifecycle:
   request:
      method: GET
      headers:
         content-type: application/json
      url: /invoice/123
      postBody: null

   response:
      headers:
         content-type: application/text
         access-control-allow-origin: *
      status: 200
      latency: 1000
      body: This is a response for 123
```
The parent node called `httplifecycle`. You can have as many httplifecycles as you want in one configuration.
Under each `httplifecycle` you should have one `request` and one `response` nodes. Each of the latter has its
respective children nodes as per above example. Response `latency` is in milliseconds.
Indentation of `httplifecycle` is _not_ required. In other words, the following format is also valid:

```
httplifecycle:
request:
headers:
authorization: bob:secret
method: GET
url: /invoice/123
response:
headers:
content-type: application/json
access-control-allow-origin: *
status: 200
body: {"message" : "This is a response for 123"}

httplifecycle:
request:
method: POST
url: /invoice/123
postBody: post body
response:
status: 200
body: This is a response for 123
```

Please keep in mind:
1. Ensure that the provided `response` body is on one line. In other words, no line breaks.
2. Because stubby4j listens on its own port, due to Ajax same-origin policy
the `access-control-allow-origin: *` header has to be sent with HTTP response. In other words, if your web app running
on `localhost:8080`, submitting Ajax request to `localhost:8882` (stubby4j) is not allowed unless correct headers are set
________________________________________________

Commandline Usage
=================

```
java -jar stubby4j-x.x.x.jar [-a <arg>] [-c <arg>] [-f <arg>] [-h] [-k <arg>] [-m <arg>] [-p <arg>]

 -a,--address <arg>       Host address that stubby4j should run on
 -c,--clientport <arg>    Port for incoming client requests
 -f,--config <arg>        YAML file with request/response configuration
 -h,--help                This help message
 -k,--keystore <arg>      Path to a local keystore file for enabling SSL
 -m,--adminport <arg>     Port for admin status check requests
 -p,--keypassword <arg>   Password for the provided keystore file
```

1. By default client (the request consumer) is running on port `8882`, while admin (system status) is running on port `8889`.
2. For system status (ATM it is just a dump of in-memory stub configuration), navigate to `http://<host>:<admin_port>/ping`
3. It is possible to enable stubby4j to accept HTTP requests over SSL. Use command line options as per above example to provide
SSL certificate and password. The default SSL port is `7443`

________________________________________________

Starting stubby4j programmatically
==================================
```
private static Stubby4JClient stubby4JClient;

@BeforeClass
public static void beforeClass() throws Exception {
   final URL url = UtilsTest.class.getResource("/config.yaml");
   stubby4JClient = Stubby4JClientFactory.getInstance(url.getFile());
   stubby4JClient.start();
}
.
.
.
@AfterClass
public static void afterClass() throws Exception {
   stubby4JClient.stop();
}
```

OR

```
@BeforeClass
public static void beforeClass() throws Exception {
   stubby4JClient = Stubby4JClientFactory.getInstance();
   stubby4JClient.start();
}
.
.
.
@AfterClass
public static void afterClass() throws Exception {
   stubby4JClient.stop();
}
```

OR

```
@BeforeClass
public static void beforeClass() throws Exception {
   int clientPort = 8888;
   int adminPort = 9999;
   final URL url = Stubby4JTest.class.getResource("/config.yaml");
   stubby4JClient = Stubby4JClientFactory.getInstance(url.getFile());
   stubby4JClient.start(clientPort, adminPort);
}
.
.
.
@AfterClass
public static void afterClass() throws Exception {
   stubby4JClient.stop();
}
```

________________________________________________

Making HTTP requests to stubby4j at runtime using stubby4j client
=================================================================

```
@Test
public void shouldDoGetOnURI() throws Exception {
   final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/1", "localhost", 8882);
   final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

   Assert.assertEquals(200, stubby4JResponse.getResponseCode());
   Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", stubby4JResponse.getContent());
}

@Test
public void shouldDoPostOnURI() throws Exception {
   final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.POST, "/item/1", "localhost", 8882, "post body");
   final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

   Assert.assertEquals(200, stubby4JResponse.getResponseCode());
   Assert.assertEquals("Got post response", stubby4JResponse.getContent());
}
```

________________________________________________

Making HTTP requests to stubby4j at runtime using stubby4j client with Basic Authorization
==========================================================================================

In order to configure Basic Authorization, you need to specify username followed by `:`, followed by password
as `authorization` header value in the stub `request` configuration:

```
httplifecycle:
   request:
      method: GET
      url: /invoice/123
      headers:
         authorization: bob:secret
   response:
      status: 200
      body: This is a response for 123
```

Upon parsing of the stub config data, base64 encoding scheme will be applied to the provided `username:password` value, which
will be prepended with the word "Basic". The final result will conform to HTTP header `Authorization` format, eg.: `Basic Ym9iOnNlY3JldA==`


```
@Test
public void shouldDoGetOnURIWithAuthorization() throws Exception {
   final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(Charset.forName("UTF-8"))));
   final String postBody = null;
   final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/auth", "localhost", 8882, postBody, encodedCredentials);
   final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

   Assert.assertEquals(200, stubby4JResponse.getResponseCode());
   Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", stubby4JResponse.getContent());
}

```
________________________________________________

Configuring HTTP request and response stubs at runtime without restarting the server
====================================================================================

In order to configure HTTP request and response stubs at runtime, you need to POST
stub config data to the following end point: `http://<host>:<admin_port>/stubdata/new`


###### The POSTed stub data should have the same structure as the config data from YAML configuration, eg.:

```
httplifecycle:
request:
headers:
authorization: bob:secret
method: GET
url: /invoice/123
response:
headers:
content-type: application/json
access-control-allow-origin: *
status: 200
body: {"message" : "This is a response for 123"}

httplifecycle:
request:
method: POST
url: /invoice/123
postBody: post body
response:
status: 200
body: This is a response for 123
```

```
@Test
public void shouldCreateStubbedData() throws Exception {
   final URL url = Stubby4JAdminClientIntegrationTest.class.getResource("/config.yaml");
   final String content = HandlerUtils.inputStreamToString(url.openStream());
   final ClientRequestInfo adminRequest = new ClientRequestInfo(HttpMethods.POST, AdminHandler.RESOURCE_STUBDATA_NEW, "localhost", 8889, content);
   final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(adminRequest);

   Assert.assertEquals(201, stubby4JResponse.getResponseCode());
   Assert.assertEquals("Configuration created successfully", stubby4JResponse.getContent());
}
```


##### Please note:
1. New POSTed data will purge the previous stub data from stubby4j memory.
2. POSTed stub data will be lost on server restart. If you want to use the same stub data all over again, load it from configuration file
________________________________________________

DEPENDENCIES
=============

The following dependencies embedded within stubby4j:

1. jetty-server-8.1.1.v20120215.jar 
2. javax.servlet-3.0.0.v201112011016.jar 
3. jetty-continuation-8.1.1.v20120215.jar 
4. jetty-http-8.1.1.v20120215.jar 
5. jetty-io-8.1.1.v20120215.jar 
6. jetty-util-8.1.1.v20120215.jar
7. commons-cli-1.2.jar
8. commons-codec-1.5.jar

Kudos
=====
Special thanks fly out to the following Ninjas for their help, support and feedback:

1. Isa Goksu
2. Eric Mrak
3. Sankalp Saxena
4. Simon Brunning
5. Ed Hewell
6. Kenny Lin
