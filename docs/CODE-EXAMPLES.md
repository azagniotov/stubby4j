## How to Start stubby4j Programmatically

```java
private static StubbyClient stubbyClient;

@BeforeClass
public static void beforeClass() throws Exception {
   final URL url = StubbyClientStubsIntegrationTest.class.getResource("/atom-feed.yaml");

   ANSITerminal.mute = true;
   stubbyClient = new StubbyClient(url.getFile());
   stubbyClient.startJetty();
}
.
.
.
@AfterClass
public static void afterClass() throws Exception {
   stubbyClient.stop();
}
```

OR

```java
@BeforeClass
public static void beforeClass() throws Exception {
   int clientPort = 8888;
   int adminPort = 9999;
   final URL url = SomeClass.class.getResource("/config.yaml");
   stubbyClient = new StubbyClient(url.getFile());
   stubbyClient.start(clientPort, adminPort);
}
.
.
.
@AfterClass
public static void afterClass() throws Exception {
   stubbyClient.stop();
}
```

## How to Make HTTP Request to stubby4j at Runtime Using Client

```java
 @Test
   public void doGet_ShouldMakeSuccessfulGet() throws Exception {

      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGet(host, uri, port);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", clientHttpResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGet() throws Exception {

      final String uri = "/item/1";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGetUsingDefaults(uri);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", clientHttpResponse.getContent());
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGet(host, uri, port, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", clientHttpResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));
      final String uri = "/item/auth";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGetUsingDefaults(uri, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", clientHttpResponse.getContent());
   }

   @Test
   public void doGetOverSsl_ShouldMakeSuccessfulGet() throws Exception {

      final String host = "localhost";
      final String uri = "/item/1";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGetOverSsl(host, uri);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", clientHttpResponse.getContent());
   }

   @Test
   public void doGetOverSsl_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/auth";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGetOverSsl(host, uri, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", clientHttpResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, "post body");

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("Got post response", clientHttpResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/submit";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;
      final String post = "{\"action\" : \"submit\"}";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, encodedCredentials, post);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String uri = "/item/submit";
      final String post = "{\"action\" : \"submit\"}";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPostUsingDefaults(uri, post, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost() throws Exception {
      final String uri = "/item/1";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPostUsingDefaults(uri, "post body");

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("Got post response", clientHttpResponse.getContent());
   }


   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost_WhenEmptyPostGiven() throws Exception {
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPostUsingDefaults(uri, "");

      Assert.assertEquals(HttpStatus.CREATED_201, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }
```

## How to Make HTTP Request with Basic Authorization to stubby4j at Runtime Using Client


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

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, encodedCredentials, post);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String uri = "/item/submit";
      final String post = "{\"action\" : \"submit\"}";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPostUsingDefaults(uri, post, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }
```

## How to Configure HTTP Stub Data at Runtime

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

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, content);

      Assert.assertEquals(HttpStatus.CREATED_201, clientHttpResponse.getResponseCode());
      Assert.assertEquals("Configuration created successfully", clientHttpResponse.getContent());
   }
```


##### Please note:
1. New POSTed data will purge the previous stub data from stubby4j memory.
2. POSTed stub data will be lost on server restart. If you want to use the same stub data all over again, load it from configuration file