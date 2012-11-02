## YAML Configuration Sample

When creating request/response data for the stub server, the config data should be specified in valid YAML 1.1 syntax.
Submit `POST` requests to `http://<host>:<admin_port>/stubdata/new` or load a data file (`-d` or `--data`) with the following structure for each endpoint:

* `request`: (REQUIRED) describes the client's call to the server
   * `method`: (REQUIRED) GET/POST/PUT/DELETE/etc.
   * `headers`: (OPTIONAL) a key/value map of HTTP headers the server should read from the request
   * `query`: (OPTIONAL) a key/value map of query string params the server should read from the URI
   * `url`: (REQUIRED) the URI string. Can include query string
   * `file`: (OPTIONAL) if specified (an absolute path or path relative to the stubby4j JAR),
         returns the contents of the given file as the request POST content. If the file cannot be found at YAML data parse time,
         value from `post` is used instead. If `post` was not provided, it is assumed that POST body was not provided
   * `post`: (OPTIONAL) a string matching the textual body of the POST request.
* `response`: (REQUIRED) describes the server's response to the client
   * `headers`: (OPTIONAL) a key/value map of headers the server should respond with
   * `latency`: (OPTIONAL) delay in milliseconds the server should wait before responding
   * `file`: (OPTIONAL) if specified (an absolute path or path relative to the stubby4j JAR),
      returns the contents of the given file as the response body. If the file cannot be found at YAML data parse time,
      value from `body` is used instead. If `body` was not provided, an empty string is returned by default
   * `body`: (OPTIONAL) the textual body of the server's response to the client
   * `status`: (REQUIRED) the numerical HTTP status code (200 for OK, 404 for NOT FOUND, etc.)

```yaml
-  request:
      method: GET
      url: /some/uri?param=true&anotherParam=false
      headers:
         authorization: bob:secret
         
   response:
      status: 200
      body: This is a single line text response


-  request:
      method: POST
      headers:
         content-type: application/json
      file: ../data/post-body-as-file.json

   response:
      headers:
         content-type: application/json
      status: 200
      body: OK


-  request:
      method: POST
      url: /some/uri
      headers:
         content-type: application/json
      post: >
         {
            "name": "value",
            "param": "description"
         }

   response:
      headers:
         content-type: application/json
      status: 200
      body: >
         {"status" : "OK"}


-  request:
      method: GET
      url: /some/uri?param=true&anotherParam=false
      headers:
         authorization: bob:secret

   response:
      status: 200
      file: /home/development/application/testing/data/create-account-soap-response.xml


-  request:
      url: /some/uri
      query:
         paramTwo: 12345
         paramOne: valueOne
      method: POST
      headers:
         authorization: bob:secret
      post: this is some post data in textual format
   
   response:
      headers:
         content-type: application/json
      latency: 1000
      status: 200
      body: You're request was successfully processed!


-  request:
      method: GET
      url: /some/uri
      query:
         paramTwo: 12345
         paramOne: valueOne

   response:
      status: 200
      file: ../data/create-service-soap-response.xml
      latency: 1000


-  request:
      url: /some/uri?firstParam=1&secondParam=2
      method: POST
      headers:
         authorization: bob:secret

   response:
      headers:
         content-type: text/plain
      status: 200
      body: Success!


-  request:
      method: GET
      url: /some/uri
      
   response:
      headers:
         content-type: application/json
         access-control-allow-origin: "*"
      body: >
         {"status" : "success"}
      latency: 5000
      status: 201


-  request:
      method: GET
      headers:
         content-type: application/json
      url: /some/uri

   response:
      headers:
         content-type: application/text
         access-control-allow-origin: "*"
      latency: 1000
      body: >
         This is a text response, that can span across 
         multiple lines as long as appropriate indentation is in place.
      status: 200


-  request:
      method: GET
      headers:
         content-type: application/json
      url: /some/uri

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
      
      
-  request:
      method: GET
      url: /some/redirecting/uri

   response:
      latency: 1000
      status: 301
      headers:
         location: /some/other/uri
      body:
```
