[![Build Status](https://secure.travis-ci.org/azagniotov/stubby4j.png?branch=master)](http://travis-ci.org/azagniotov/stubby4j)

# stubby4j

A Java-based stub HTTP server with embedded Jetty.

### Why would a developer use stubby4j?

* You want to simulate responses from real server and don't care (or cannot) to go over the network
* You want to verify that your code makes HTTP requests with all the required parameters and/or headers
* You want to verify that your code correctly handles HTTP error codes
* You want to trigger response from the server based on the request parameters
* You want support for GET/POST/PUT/DELETE HTTP methods
* You want to trigger multiple responses based on multiple requests on the same URI
* You want to easily configure stub data using configuration file
* You want to easily configure stub data at runtime, without restarting the server by making a POST to an exposed endpoint
* You want to easily provide canned answers in your contract/integration tests
* You don't want to spend time coding for the above requirements and just want to concentrate on the task at hand

### Why would a QA use stubby4j?

* Specifiable mock responses to simulate page conditions without real data.
* Easily swappable data config files to run different data sets and responses.
* All-in-one stub server to handle mock data with less need to upkeep code for test generation

##### All this goodness in just under 1.1MB
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
   response:
      status: 200
      body: This is a response for 123

httplifecycle:
   request:
      method: GET
      url: /invoice/567
   response:
      body: This is a response for 567
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
      status: 200
      body: This is a response for 123
```
The parent node called `httplifecycle`. You can have as many httplifecycles as you want in one configuration.
Under each `httplifecycle` you should have one `request` and one `response` nodes. Each of the latter has its
respective children nodes as per above example. Indentation of `httplifecycle` is _not_ required. In other words,
the following format is also valid:

```
httplifecycle:
request:
method: GET
url: /invoice/123
response:
status: 200
body: This is a response for 123

httplifecycle:
request:
method: POST
url: /invoice/123
postBody: post body
response:
status: 200
body: This is a response for 123
```

Please keep in mind, you MUST ensure that the provided `response` body is on one line. In other words, no line
breaks.
________________________________________________

Commandline Usage
=================

```
java -jar stubby4j-x.x.x.jar [-a <arg>] [-c <arg>] [-f <arg>] [-h] [-m <arg>]

 -a,--address <arg>      Host address that stubby4j should run on. Default is localhost
 -c,--clientport <arg>   Port for incoming client requests
 -f,--config <arg>       YAML file with request/response configuration
 -h,--help               This help message
 -m,--adminport <arg>    Port for admin status check requests
```

By default client (the request consumer) is running on port `8882`, while admin (system status) is running on port `8889`.

For system status (ATM it is just a database dump), navigate to `http://<host>:<admin_port>/ping`

________________________________________________

Starting stubby4j programmatically
==================================
```
private static Stubby4J stubby4J;

@BeforeClass
public static void beforeClass() throws Exception {
   final URL url = UtilsTest.class.getResource("/config.yaml");
   stubby4J = new Stubby4J(url.getFile());
   stubby4J.start();
}
```

OR

```
@BeforeClass
public static void beforeClass() throws Exception {
   int clientPort = 8888;
   int adminPort = 9999;
   final URL url = Stubby4JTest.class.getResource("/config.yaml");
   stubby4J = new Stubby4J(url.getFile());
   stubby4J.start(clientPort, adminPort);
}
```

```
@Test
public void shouldDoGetOnURI() throws Exception {
   final Map<String, String> result = stubby4J.doGetOnURI("/item/1");
   Assert.assertEquals("200", result.get(Stubby4J.KEY_STATUS));
   Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", result.get(Stubby4J.KEY_RESPONSE));
}

@Test
public void shouldDoPostOnURI() throws Exception {
   final Map<String, String> result = stubby4J.doPostOnURI("/item/1", "post body");
   Assert.assertEquals("200", result.get(Stubby4J.KEY_STATUS));
   Assert.assertEquals("Got post response", result.get(Stubby4J.KEY_RESPONSE));
}
.
.
.
@AfterClass
public static void afterClass() throws Exception {
   stubby4J.stop();
}
```
________________________________________________

Configuring HTTP request and response stubs at runtime without restarting the server
====================================================================================

In order to configure HTTP request and response stubs at runtime, you need to make a POST
to the following end point: `http://<host>:<admin_port>/endpoint/new`

##### The data that should be POSTed is as follows:
1. `method` => Any of the HTTP methods (GET/POST/PUT/DELETE) (Required)
2. `url` => An endpoint URI to be consumed (Required)
2. `postBody` => HTTP request post body (Optional)
3. `body` => Expected response body (a JSON string etc.) (Required)
4. `status` => HTTP status (Required)
5. `responseHeaders` => HTTP response headers as key/value pairs, separated by comma: `content-type=application/json,charset=UTF-8` (Optional)

##### Please note:
1. POSTed duplicate or incomplete data will result in an error from the server.
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
