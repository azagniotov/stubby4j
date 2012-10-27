[![Build Status](https://secure.travis-ci.org/azagniotov/stubby4j.png?branch=master)](http://travis-ci.org/azagniotov/stubby4j)

# stubby4j

A stub HTTP server written in Java with embedded Jetty server	
##### Why the word "stubby"?
It is a stub HTTP server after all, hence the "stubby". Also, in Australian slang "stubby" means _beer bottle_

## Why would a developer use stubby4j?
####You want to:
* Simulate responses from real server and don't care (or cannot) to go over the network
* Verify that your code makes HTTP requests with all the required parameters and/or headers
* Verify that your code correctly handles HTTP error codes
* You want to trigger response from the server based on the request parameters over HTTP or HTTPS
* Support for any of the available HTTP methods
* Simulate support for Basic Authorization
* Support for HTTP 30x redirects
* Trigger multiple responses based on multiple requests on the same URI
* Configure stub data using configuration file
* Configure stub data at runtime, without restarting the server by making a POST to an exposed endpoint
* Provide canned answers in your contract/integration tests
* Enable delayed responses for performance and stability testing
* Avoid to spend time coding for the above requirements
* Concentrate on the task at hand



## Why would a QA use stubby4j?
* Specifiable mock responses to simulate page conditions without real data.
* Easily swappable data config files to run different data sets and responses.
* All-in-one stub server to handle mock data with less need to upkeep code for test generation

###### All this goodness in just under 1.5MB

## Dependencies
stubby4j is a fat JAR (for the skinny JAR, please refer to `artifacts`), and contains the following dependencies.

* jetty-server-8.1.1.v20120215.jar
* javax.servlet-3.0.0.v201112011016.jar
* jetty-continuation-8.1.1.v20120215.jar
* jetty-http-8.1.1.v20120215.jar
* jetty-io-8.1.1.v20120215.jar
* jetty-util-8.1.1.v20120215.jar
* commons-cli-1.2.jar
* commons-codec-1.5.jar
* snakeyaml-1.11.jar

**stubby4j is also compatible with Jetty 7.x.x and servlet API v2.5**


## Documentation
See `docs` directory for:

* [Commandline Usage](https://github.com/azagniotov/stubby4j/blob/master/docs/COMMAND-LINE-USAGE.md)
* [Code Examples](https://github.com/azagniotov/stubby4j/blob/master/docs/CODE-EXAMPLES.md)
* [YAML Sample](https://github.com/azagniotov/stubby4j/blob/master/docs/YAML.md)


## Artifacts
See `artifacts` directory for fat and skinny stubby4j JARs:


## Authors
See AUTHORS.md for details.


## Kudos
See THANKS.md for details.


## See Also
**[stubby4node](https://github.com/Afmrak/stubby4node):** A NodeJS implementation of stubby4j


## Copyright
See COPYRIGHT for details.