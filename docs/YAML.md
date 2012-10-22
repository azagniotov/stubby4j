## YAML Configuration Sample

When creating request/response data for the stub server, the config data should be specified in valid YAML 1.1 syntax.
Submit `POST` requests to `http://<host>:<admin_port>/stubdata/new` or load a data file (`-d` or `--data`) with the following structure for each endpoint:

* `request`: describes the client's call to the server
   * `method`: GET/POST/PUT/DELETE/etc.
   * `headers`: a key/value map of headers the server should read from the request
   * `url`: the URI string. GET parameters should also be included inline here
   * `headers`: a key/value map of headers the server should respond to
   * `postBody`: a string matching the textual body of the response.
* `response`: describes the server's response to the client
   * `headers`: a key/value map of headers the server should use in it's response
   * `latency`: the time in milliseconds the server should wait before responding. Useful for testing timeouts and latency
   * `body`: the textual body of the server's response to the client
   * `status`: the numerical HTTP status code (200 for OK, 404 for NOT FOUND, etc.)

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
      url: /some/uri
      method: POST
      headers:
         authorization: bob:secret
      postBody: this is some post data in textual format
   
   response:
      headers:
         content-type: application/json
      latency: 1000
      status: 200
      body: You're request was successfully processed!


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
