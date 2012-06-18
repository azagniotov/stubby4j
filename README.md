stubby4j
========
A Java-based stub HTTP server.

Why use stubby4j?
=================
1. You want to simulate responses from real server and don't care (or cannot) to go over the network
2. You want to verify that your code makes HTTP requests with all the required parameters and/or headers
3. You want to verify that your code correctly handles HTTP error codes
4. You want to trigger response from the server based on the request parameters
5. You want support for GET/POST/PUT/DELETE HTTP methods
6. You want to trigger multiple responses based on multiple requests on the same URI
7. You want to easily configure HTTP request and response stubs
8. You don't want to spend time coding for the above requirements and just want to concentrate on the task at hand

All this goodness in just under 2.5MB

Why "stubby"?
=============
It is a stub HTTP server after all, hence "stubby". Also, in Australian slang "stubby" means beer bottle

YAML Configuration
==================
When creating request/response data for the stub server, it must be in the following format:

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
respective children nodes as per above example. 

Please keep in mind, you MUST ensure that the provided `response` body is on one line. In other words, no line
breaks.

Usage
=====
```
java -jar stubby4j-x.x.x-SNAPSHOT.jar [-a <arg>] [-c <arg>] [-f <arg>] [-h] [-m <arg>]

 -a,--address <arg>      Host address that stubby4j should run on. Default is localhost
 -c,--clientport <arg>   Port for incoming client requests
 -f,--config <arg>       YAML file with request/response configuration
 -h,--help               This help message
 -m,--adminport <arg>    Port for admin status check requests
```

By default client (the request consumer) is running on port 8882, while admin (system status) is running on port 8889
For system status (ATM it is just a database dump), navigate to `http://<host>:<admin_port>/ping`
