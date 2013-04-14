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
* Live tweak previously loaded and parsed configuration file to auto refresh the stub data WITHOUT restarting the server
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
stubby4j is a fat JAR, which contains the following dependencies:

* commons-cli-1.2.jar
* commons-codec-1.5.jar
* commons-io-2.4.jar
* snakeyaml-1.11.jar
* javax.servlet-3.0.0.v201112011016.jar
* jetty-server-8.1.7.v20120910.jar
* jetty-continuation-8.1.7.v20120910.jar
* jetty-util-8.1.7.v20120910.jar
* jetty-io-8.1.7.v20120910.jar
* jetty-http-8.1.7.v20120910.jar

** stubby4j is also compatible with Jetty 7.x.x and servlet API v2.5 **


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

## Building with Gradle
stubby4j is a multi-module Gradle project. IntelliJ IDEA users should run ```gradle cleanIdea idea``` in order to generate IntelliJ IDEA project files. Eclipse users should run ```cleanEclipse eclipse``` in order to generate Eclipse project files. Task ```testAll``` runs unit, integration and functional tests.

## Documentation
See `docs` directory for:

* [Build Instructions](https://github.com/azagniotov/stubby4j/blob/master/docs/BUILDING.md)
* [Commandline Usage](https://github.com/azagniotov/stubby4j/blob/master/docs/COMMAND-LINE-USAGE.md)
* [Code Examples](https://github.com/azagniotov/stubby4j/blob/master/docs/CODE-EXAMPLES.md)
* [YAML Sample](https://github.com/azagniotov/stubby4j/blob/master/docs/YAML.md)

## Change Log
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
See AUTHORS.md for details.


## Kudos
See THANKS.md for details.


## See Also
**[stubby4node](https://github.com/mrak/stubby4node):** A NodeJS implementation of stubby

## Copyright
See COPYRIGHT for details.
