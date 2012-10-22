## Starting stubby4j programmatically

```java
private static Stubby4JClient stubby4JClient;

@BeforeClass
public static void beforeClass() throws Exception {
   final URL url = SomeClass.class.getResource("/config.yaml");
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

```java
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

```java
@BeforeClass
public static void beforeClass() throws Exception {
   int clientPort = 8888;
   int adminPort = 9999;
   final URL url = SomeClass.class.getResource("/config.yaml");
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

## Making HTTP Requests to stubby4j at Runtime Using stubby4j Client

```java
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

## Making HTTP requests to stubby4j at runtime using stubby4j client with Basic Authorization


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
public void shouldDoGetOnURIWithAuthorization() throws Exception {
   final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(Charset.forName("UTF-8"))));
   final String postBody = null;
   final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/auth", "localhost", 8882, postBody, encodedCredentials);
   final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

   Assert.assertEquals(200, stubby4JResponse.getResponseCode());
   Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", stubby4JResponse.getContent());
}

```

## Configuring HTTP Request and Response Stubs at Runtime Without Restarting stubby4j

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
public void shouldCreateStubbedData() throws Exception {
   final URL url = SomeClass.class.getResource("/config.yaml");
   final String content = SomeUtils.inputStreamToString(url.openStream());
   final ClientRequestInfo adminRequest = new ClientRequestInfo(HttpMethods.POST, AdminHandler.RESOURCE_STUBDATA_NEW, "localhost", 8889, content);
   final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(adminRequest);

   Assert.assertEquals(201, stubby4JResponse.getResponseCode());
   Assert.assertEquals("Configuration created successfully", stubby4JResponse.getContent());
}
```


##### Please note:
1. New POSTed data will purge the previous stub data from stubby4j memory.
2. POSTed stub data will be lost on server restart. If you want to use the same stub data all over again, load it from configuration file