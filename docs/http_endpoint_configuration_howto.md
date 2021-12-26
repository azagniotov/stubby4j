---
layout: default
title: "HTTP endpoint configuration HOWTO"
description: "This page explains the usage, intent and behavior of each property on the request and response, regex stubbing for dynamic matching, stubbing HTTP 30x redirects, record-and-replay and more"
keywords: request,response,url,post,file,headers,json,regex,http,stub,stubby4j,http stub configuration,stub endpoint,stub TLS request,stub request,stub response,stub http API
---

[Back to Home](../README.md#http-endpoint-configuration-howto)

## HTTP endpoint configuration HOWTO

### Table of contents

* [Summary](#summary)
  * [Request](#request)
    * [Request object properties](#request-object-properties)
    * [Request proxying](#request-proxying)
    * [Regex stubbing for dynamic matching](#regex-stubbing-for-dynamic-matching)
    * [Regex stubbing for XML content](#regex-stubbing-for-xml-content)
    * [Authorization Header](#authorization-header)
  * [Response](#response)
    * [Response object properties](#response-object-properties)
    * [Dynamic token replacement in stubbed response](#dynamic-token-replacement-in-stubbed-response)
    * [Stubbing HTTP 30x redirects](#stubbing-http-30x-redirects)
    * [Record and Replay](#record-and-replay)
  * [Supplying stubbed endpoints to stubby](#supplying-stubbed-endpoints-to-stubby)
    * [Splitting main YAML config](#splitting-main-yaml-config)

### Summary

This page explains the usage, intent and behavior of each property on the `request` and `response` objects.

Also, you will learn about request proxying to other hosts, regex stubbing for dynamic matching, stubbing HTTP 30x redirects, record-and-replay and more.

<details markdown=block>
  <summary markdown=span>

   <code>
     Click to expand
   </code>

  </summary>

Here is a fully-populated, unrealistic endpoint:

```yaml
-  description: Optional description shown in logs
   uuid: fdkfsd8f8ds7f
   request:
      url: ^/your/awesome/endpoint$
      method: POST
      query:
         exclamation: post requests can have query strings!
      headers:
         content-type: application/xml
      post: >
         <!xml blah="blah blah blah">
         <envelope>
            <unaryTag/>
         </envelope>
      file: tryMyFirst.xml

   response:
      status: 200
      latency: 5000
      headers:
         content-type: application/xml
         server: stubbedServer/4.2
      body: >
         <!xml blah="blah blah blah">
         <responseXML>
            <content></content>
         </responseXML>
      file: responseData.xml
```

Keep on reading to understand how to add http endpoint configurations to your `stubby4j` YAML config.

[Back to top](#table-of-contents)

## Stub/Feature

#### description (`optional`)

* Description field which can be used to show optional descriptions in the logs
* Useful when you have a number of stubs loaded for the same endpoint, and it starts to get confusing as to which is being matched

```yaml
-  description: Stub one
   request:
      url: ^/one$
      method: GET

   response:
      status: 200
      latency: 100
      body: 'One!'

-  description: Stub two
   request:
      url: ^/two$
      method: GET

   response:
      status: 200
      latency: 100
      body: 'Two!'

-  request:
      url: ^/three$
      method: GET

   response:
      status: 200
      latency: 100
      body: 'Three!'
```

#### uuid (`optional`)

* Useful when you want to specify unique identifier, so it would be easier to update/delete it at runtime

```yaml
-  uuid: 9136d8b7-f7a7-478d-97a5-53292484aaf6
   request:
      method: GET
      url: /with/configured/uuid/property

   response:
      headers:
         content-type: application/json
      status: 200
      body: >
         {"status" : "OK"}
```

</details>

[Back to top](#table-of-contents)


## Request

This object used to match an incoming request to stubby against the available endpoints that have been configured.

In YAML config, the `request` object supports the following properties:

`url`, `method`, `query`, `headers`, `post`, `file`

Keep on reading to understand their usage, intent and behavior.

### Request object properties

<details markdown=block>
  <summary markdown=span>

   <code>
     Click to expand
   </code>

  </summary>

#### url (`required`)

* is a full-fledged __regular expression__
* This is the only required property of an endpoint.
* signify the url after the base host and port (i.e. after `localhost:8882`).
* must begin with ` / `.
* any query paramters are stripped (so don't include them, that's what `query` is for).
  * `/url?some=value&another=value` becomes `/url`
* no checking is done for URI-encoding compliance.
  * If it's invalid, it won't ever trigger a match.

This is the simplest you can get:
```yaml
-  request:
      url: /
```

A demonstration when not using regular expressions:
```yaml
-  request:
      url: /some/resource/that/will/be/fully/matched
```

A demonstration using regular expressions:
```yaml
-  request:
      url: ^/has/to/begin/with/this/

-  request:
      url: /has/to/end/with/this/$

-  request:
      url: ^/must/be/this/exactly/with/optional/trailing/slash/?$

-  request:
      url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$
```

#### method (`required`)

* defaults to `GET`.
* case-insensitive.
* can be any of the following:
  * HEAD
  * GET
  * POST
  * PUT
  * POST
  * DELETE
  * PATCH
  * etc.

```yaml
-  request:
      url: /anything
      method: GET
```

* it can also be an array of values.

```yaml
-  request:
      url: /anything
      method: [GET, HEAD]

-  request:
      url: /anything
      method:
         -  GET
         -  HEAD
```

#### query (`optional`)

* can be a full-fledged __regular expression__
* if not stubbed, stubby ignores query parameters on incoming request and will match only request URL
* stubby accommodates for HTTP requests that contain query string params with no values
* query params can be specified regardless of their order in incoming request. In other words - order agnostic
* query params can also be an array with double/single quoted/un-quoted elements: ```attributes=["id","uuid"]``` or ```attributes=[id,uuid]```. Please note no spaces between the CSV

```yaml
-  request:
      method: GET
      url: ^/with/parameters$
      query:
         type_name: user
         client_id: id
         client_secret: secret
         random_id: "^sequence/-/\\d/"
         session_id: "^user_\\d{32}_local"
         attributes: '["id","uuid","created","lastUpdated","displayName","email","givenName","familyName"]'

```

* The following will match either of these:
  * `/with/parameters?search=search+terms&filter=month`
  * `/with/parameters?filter=month&search=search+terms`

```yaml
-  request:
      url: ^/with/parameters$
      query:
         search: search terms
         filter: month
```

* The following will match either of these:
  * `/with/parameters?search&filter=month`
  * `/with/parameters?search=&filter=month`

```yaml
-  request:
      url: ^/with/parameters$
      query:
         search:
         filter: month
```

* The following will match:
  * From the browser: `http://localhost:8882/with/parameters?term=boo+and+foo`
  * From the browser: `http://localhost:8882/with/parameters?term=boo%2Band%2Bfoo`
  * From the browser: `http://localhost:8882/with/parameters?term=boo  and   foo`
  * From the browser: `http://localhost:8882/with/parameters?term=boo%20and%20foo`
  * From the code: `String request = "http://localhost:8882/with/parameters?term=boo+and+foo"`
  * From the code: `String request = "http://localhost:8882/with/parameters?term=boo%2Band%2Bfoo"`
  * From the code: `String request = "http://localhost:8882/with/parameters?term=boo and foo"`
  * From the code: `String request = "http://localhost:8882/with/parameters?term=boo%20and%20foo"`

```yaml
-  request:
      url: ^/with/parameters$
      query:
         term: "boo and foo"
```

* The following will match:
  * From the browser: `http://localhost:8882/with/parameters?term=['stalin+and+truman']`
  * From the browser: `http://localhost:8882/with/parameters?term=['stalin+and+++++++++++truman']`
  * From the browser: `http://localhost:8882/with/parameters?term=['stalin%2Band%2Btruman']`
  * From the browser: `http://localhost:8882/with/parameters?term=['stalin and    truman']`
  * From the browser: `http://localhost:8882/with/parameters?term=['stalin%20and%20truman']`
  * From the code: `String request = "http://localhost:8882/with/parameters?term=%5B%27stalin%2Band%2Btruman%27%5D"`
  * From the code: `String request = "http://localhost:8882/with/parameters?term=%5B%27stalin+++++and+truman%27%5D"`
  * From the code: `String request = "http://localhost:8882/with/parameters?term=%5B%27stalin and   truman%27%5D"`
  * From the code: `String request = "http://localhost:8882/with/parameters?term=%5B%27stalin%20and%20truman%27%5D"`

```yaml
-  request:
      url: ^/with/parameters$
      query:
         term: "['stalin and truman']"
```

#### post (`optional`)

* Represents the body POST of incoming request, ie.: form data
* can be a full-fledged __regular expression__
* if not stubbed, any POSTed data on incoming request is ignored

```yaml
-  request:
      url: ^/post/form/data$
      post: name=John&email=john@example.com
```

```yaml
-  request:
      method: [POST]
      url: /uri/with/post/regex
      post: "^[\\.,'a-zA-Z\\s+]*$"
```

```yaml
-  request:
      url: ^/post/form/data$
      post: "^this/is/\\d/post/body"
```

```yaml
-  request:
      method: POST
      url: /post-body-as-json
      headers:
         content-type: application/json
      post: >
         {"userId":"19","requestId":"(.*)","transactionDate":"(.*)","transactionTime":"(.*)"}

   response:
      headers:
         content-type: application/json
      status: 200
      body: >
         {"requestId": "<%post.1%>", "transactionDate": "<%post.2%>", "transactionTime": "<%post.3%>"}
```

```yaml
-  request:
      method: POST
      url: /post-body-as-json-2
      headers:
         content-type: application/json
      post: >
         {"objects": [{"key": "value"}, {"key": "value"}, {"key": {"key": "(.*)"}}]}

   response:
      headers:
         content-type: application/json
      status: 200
      body: >
         {"internalKey": "<%post.1%>"}
```

#### file (`optional`)

* holds a path to a local file (it can be an `absolute` or `relative` path to the main YAML specified in `-d` or `--data`). This property allows you to split up stubby data across multiple files instead of making one huge bloated main config YAML. For example, let's say you want to stub a big POST payload, so instead of dumping a lot of text under the `post` property, you could specify a local file with the payload using the `file` property:

```yaml
-  request:
      method: POST
      headers:
         content-type: application/json
      file: ../json/post.payload.json
```

* please note, if both `file` & `post` properties are supplied, the `file` takes precedence & replaces `post` with the contents from the provided file.
* if `--watch` command-line argument was supplied during startup, then any modifications to the supplied local file in `file` (e.g. `file: ../json/post.payload.json`) will cause the whole configuration to be reloaded.
* if the local file could not be loaded for whatever reason (ie.: not found), stubby falls back to `post` for matching.
* please keep in mind: ```SnakeYAML``` library (used by stubby4j) parser ruins multi-line strings by not preserving system line breaks. If `file` property is stubbed, the file content is loaded as-is, in other words - it does not go through SnakeYAML parser. Therefore it's better to load big POST content for request using `file` property. Keep in mind, stubby4j stub server is dumb and does not use smart matching mechanism (i.e.: don't match line separators or don't match any white space characters) - whatever you stubbed, must be POSTed exactly for successful match. Alternatively you can consider using regular expression in `post`

```yaml
-  request:
      url: ^/match/against/file$
      file: postedData.json
      post: '{"fallback":"data"}'
```

postedData.json
```json
{"fileContents":"match against this if the file is here"}
```

* if `postedData.json` doesn't exist on the filesystem when `/match/against/file` is matched in incoming request, stubby will match post contents against `{"fallback":"data"}` (from `post`) instead.

#### headers (`optional`)

* can be a full-fledged __regular expression__
* if not stubbed, stubby ignores headers on incoming request and will match only request URL
* if stubbed, stubby will try to match __only__ the supplied headers and will ignore other headers of incoming request. In other words, the incoming request __must__ contain stubbed header values
* headers are case-insensitive during matching
* a hashmap of header/value pairs similar to `query`.

The following endpoint only accepts requests with `application/json` post values:

```yaml
-  request:
      url: /post/json
      method: post
      headers:
         content-type: application/json
         x-custom-header: "^this/is/\d/test"
         x-custom-header-2: "^[a-z]{4}_\\d{32}_(local|remote)"
```


</details>

[Back to top](#table-of-contents)

### Request proxying

See [request_proxying.html](request_proxying.md) for details

### Regex stubbing for dynamic matching

stubby supports regex stubbing for dynamic matching on the following properties:
- `request` `url`
- `request` `query` param values
- `request` `header` name values
- `request` `post` payloads
- `request` `file` names & payloads.

Under the hood, stubby first attempts to compile the stubbed pattern into an instance of `java.util.regex.Pattern` class using the `Pattern.MULTILINE` flag. If the pattern compilation fails and `PatternSyntaxException` exception is thrown, stubby compiles the stubbed pattern into an instance of `java.util.regex.Pattern` class using the `Pattern.LITERAL | Pattern.MULTILINE` flags.

__Please note__, before using regex patterns in stubs, first it is best to ensure that the desired regex pattern "works" outside of stubby. One of the safest (and easiest) ways to test the desired pattern would be to check if the following condition is met: `Pattern.compile("YOUR_PATTERN").matcher("YOUR_TEST_STRING").matches() == true`.

The latter would ensure that the stubbed regex pattern actually works, also it is easier to debug a simple unit test case instead of trying to figure out why stub matching failed

[Back to top](#table-of-contents)

### Regex stubbing for XML content

XML is not a regular language, it can be tricky to parse it using a regular expression (well, sometimes it is not as tricky when XML regex snippet is simple. But, most of the times this will cause you tears), especially when dealing with large XML `POST` payloads. XML is very complex: nested tags, XML comments, CDATA sections, preprocessor directives, namespaces, etc. make it very difficult to create a parse-able & working regular expression.

<details markdown=block>
  <summary markdown=span>

   <code>
     Click to expand
   </code>

  </summary>

Therefore, `stubby4j` uses under the hood a full-fledged 3rd party XML parser - [XMLUnit](https://github.com/xmlunit/xmlunit).

XMLUnit enables stubbing of XML content with regular expressions by leveraging XMLUnit-specific Regex match placeholders. Placeholders are used to specify exceptional requirements in the control XML document for use during equality comparison (i.e.: regex matching).

#### How to stub XML containing regular expressions?

1. Using [XMLUnit](https://github.com/xmlunit/xmlunit) regular expression matcher placeholders [__recommended__]
2. Using vanilla regular expressions

##### Using XMLUnit regular expression matcher placeholders [__recommended__]

Using built-in XMLUnit regular expression matchers is a much more elegant, cleaner and less painful way to do XML-based regular expression matching. Also, you can still leverage the [dynamic token replacement in stubbed response](#dynamic-token-replacement-in-stubbed-response) while using XMLUnit matchers!

Now, XMLUnit placeholder `${xmlunit.matchesRegex( ... )}` to the rescue! Let's understand how to use it, consider the following example of stubbed `request` and `response` objects:

```yaml
- description: rule_1
  request:
    url: /some/resource/uri
    method: POST
    headers:
      content-type: application/xml
    post: >
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <idex:type xmlns:idex="http://idex.bbc.co.uk/v1">
          <idex:authority>${xmlunit.matchesRegex(.*)}</idex:authority>
          <idex:name>${xmlunit.matchesRegex(.*)}</idex:name>
          <idex:startsWith>${xmlunit.matchesRegex(.*)}</idex:startsWith>
      </idex:type>
  
  response:
      status: 200
      body: Captured values are, authority: <% post.1 %> with name <% post.2 %> that starts with <% post.3 %>
```
In the above example, the regular expressions defined in `post` XML will match any values inside `idex:authority`, `idex:name` and `idex:startsWith` elements, which then will be used to interpolate the template tokens in the stubbed `response`.

Please refer to the following XMLUnit [Placeholders](https://github.com/xmlunit/user-guide/wiki/Placeholders) guide or/and [their unit tests](https://github.com/xmlunit/xmlunit/blob/1c25e0171123b1a1fc543c87c5a9039d850d9b73/xmlunit-placeholders/src/test/java/org/xmlunit/placeholder/PlaceholderDifferenceEvaluatorTest.java) for more information.




##### Using vanilla regular expressions

Consider the following examples of stubbed `request` that have XML regex snippets under `post`:

```yaml
- description: rule_1
  request:
    url: /some/resource/uri
    method: POST
    headers:
      content-type: application/xml
    post: >
      <\?xml version="1.0" encoding="UTF-8" standalone="yes"\?><idex:type xmlns:idex="http://idex.bbc.co.uk/v1"><idex:authority>(.*)</idex:authority><idex:name>(.*)</idex:name><idex:startsWith>(.*)</idex:startsWith></idex:type>
```

In the above example, do note that the `?` in `<?xml .. ?>` are escaped (i.e.: `<\?xml .. \?>`) as these are regex specific characters.

```yaml
- description: rule_1
  request:
    url: /some/resource/uri
    method: POST
    headers:
      content-type: application/xml
    post: >
      <\?xml version="1.0" encoding="UTF-8"\?>
      <person xmlns="http://www.your.example.com/xml/person">
          <VocabularyElement id="urn:epc:idpat:sgtin:(.*)">
              <attribute id="urn:epcglobal:product:drugName">(.*)</attribute>
              <attribute id="urn:epcglobal:product:manufacturer">(.*)</attribute>
              <attribute id="urn:epcglobal:product:dosageForm">(.*)</attribute>
              <attribute id="urn:epcglobal:product:strength">(.*)</attribute>
              <attribute id="urn:epcglobal:product:containerSize">(.*)</attribute>
          </VocabularyElement>
          <name>(.*)</name>
          <age>(.*)</age>
          <!--
            Hello,
               I am a multi-line XML comment
               <staticText>
                  <reportElement x="180" y="0" width="200" height="20"/>
                  <text><!\[CDATA\[(.*)\]\]></text>
                </staticText>
            -->
          <homecity xmlns="(.*)cities">
              <long>(.*)</long>
              <lat>(.*)</lat>
              <name>(.*)</name>
          </homecity>
          <one name="(.*)" id="urn:company:namespace:type:id:one">(.*)</one>
          <two id="urn:company:namespace:type:id:two" name="(.*)">(.*)</two>
          <three name="(.*)" id="urn:company:namespace:type:id:(.*)">(.*)</three>
      </person>
```

In the above example, do note that the:
1. `?` in `<?xml .. ?>` are escaped (i.e.: `<\?xml .. \?>`) as these are regex specific characters, and
2. `[` and `]` in `<![CDATA[ .. ]]>` are escaped (i.e.: `<!\[CDATA\[(.*)\]\]>`) as these are regex specific characters

As you can see, using vanilla regular expressions in complex XML content is a much more brittle approach.

</details>

[Back to top](#table-of-contents)

### Authorization Header

```yaml
-  request:
      url: ^/path/to/basic$
      method: GET
      headers:
         # no "Basic" prefix nor explicit encoding in Base64 is required when stubbing,
         # just plain username:password format. Stubby internally encodes the value in Base64
         authorization-basic: "bob:password" 
   response:
      headers:
         Content-Type: application/json
      status: 200
      body: Your request with Basic was successfully authorized!

-  request:
      url: ^/path/to/bearer$
      method: GET
      headers:
         # no "Bearer" prefix is required when stubbing, only the auth value.
         # Stubby internally does not modify (encodes) the auth value
         authorization-bearer: "YNZmIzI2Ts0Q=="
   response:
      headers:
         Content-Type: application/json
      status: 200
      body: Your request with Bearer was successfully authorized!

-  request:
      url: ^/path/to/custom$
      method: GET
      headers:
         # custom prefix name is required when stubbing, followed by space & auth value.
         # Stubby internally does not modify (encodes) the auth value
         authorization-custom: "CustomAuthorizationType YNZmIzI2Ts0Q=="
   response:
      headers:
         Content-Type: application/json
      status: 200
      body: Your request with custom authorization type was successfully authorized!
```

[Back to top](#table-of-contents)


## Response

Assuming a match between the incoming HTTP request and one of the stubbed `request` objects has been found, its stubbed `response` object properties used to build the HTTP response back to the client.

In YAML config, the `response` object supports the following properties:

`status`, `body`, `file`, `headers`, `latency`

Keep on reading to understand their usage, intent and behavior.

### Response object properties


<details markdown=block>
  <summary markdown=span>

   <code>
     Click to expand
   </code>

  </summary>

* Response configuration can be a single `response` or a sequence of `response`s under the same `- request` definition.
* When sequenced responses is configured, on each incoming request to the same URI, a subsequent response in the list will be sent to the client. The sequenced responses play in a cycle (loop). In other words: after the response sequence plays through, the cycle restarts on the next incoming request.

```yaml
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
      url: /uri/with/sequenced/responses/infile

   response:
      -  status: 201
         headers:
            content-type: application/json
         file: ../json/sequenced.response.ok.json

      -  status: 201
         headers:
            content-stype: application/json
         file: ../json/sequenced.response.goingstrong.json

      -  status: 500
         headers:
            content-type: application/json
         file: ../json/sequenced.response.omfg.json


-  request:
      method: [GET]
      url: /uri/with/single/sequenced/response

   response:
      -  status: 201
         headers:
            content-stype: application/json
         body: Still going strong!
```

#### status (`required`)

* the HTTP status code of the response.
* integer or integer-like string.
* defaults to `200`.

```yaml
-  request:
      url: ^/im/a/teapot$
      method: POST
   response:
      status: 420
```

#### body (`optional`)

* contents of the response body
* defaults to an empty content body
* can be a URL (OAUTH is not supported) to `record & replay` response from a remote host (see [Record and Replay](#record-and-replay)). The HTTP response is recorded on the first call to stubbed URL, having the subsequent calls replay back the recorded HTTP response, without actually connecting to the remote host again.

```yaml
-  request:
      url: ^/give/me/a/smile$
   response:
      body: ':)'
```

```yaml
-  request:
      url: ^/give/me/a/smile$

   response:
      status: 200
      body: >
         {"status": "hello world with single quote"}
      headers:
         content-type: application/json
```

```yaml
-  request:
      method: GET
      url: /atomfeed/1

   response:
      headers:
         content-type: application/xml
      status: 200
      body: <?xml version="1.0" encoding="UTF-8"?><payment><paymentDetail><invoiceTypeLookupCode/></paymentDetail></payment>
```

```yaml
-  request:
      url: /1.1/direct_messages.json
      query:
         since_id: 240136858829479935
         count: 1
   response:
      headers:
         content-type: application/json
      body: https://api.twitter.com/1.1/direct_messages.json?since_id=240136858829479935&count=1
```

#### file (`optional`)

* similar to `request.file`, holds a path to a local file (it can be an `absolute` or `relative` path to the main YAML specified in `-d` or `--data`). This property allows you to split up stubby data across multiple files instead of making one huge bloated main config YAML. For example, let's say you want to render a large response body upon successful stub matching, so instead of dumping a lot of text under the `body` property, you could specify a local file with the response content using the `file` property (btw, the `file` can also refer to binary files):

```yaml
response:
      status: 200
      headers:
         content-type: application/json
      file: ../json/response.json
```

* please note, if both `file` & `body` properties are supplied, the `file` takes precedence & replaces `body` with the contents from the provided file
* if `--watch` command-line argument was supplied during startup, then any modifications to the supplied local file in `file` (e.g. `file: ../json/response.json`) will cause the whole configuration to be reloaded.
* if the file could not be loaded, stubby falls back to the value stubbed in `body`
* if `body` was not stubbed, an empty string is returned by default
* it can be ascii of binary file (PDF, images, etc.). Please keep in mind, that file is preloaded upon stubby4j startup and its content is kept as a byte array in memory. In other words, response files are not read from the disk on demand, but preloaded.


```yaml
-  request:
      url: /
   response:
      file: extremelyLongJsonFile.json
```

#### headers (`optional`)

* similar to `request.headers` except that these are sent back to the client.
* by default, header `x-stubby-resource-id` containing resource ID is returned with each stubbed response. The ID is useful if the returned resource needs to be updated at run time by ID via Admin portal

```yaml
-  request:
      url: ^/give/me/some/json$
   response:
      headers:
         content-type: application/json
      body: >
         [{
            "name":"John",
            "email":"john@example.com"
         },{
            "name":"Jane",
            "email":"jane@example.com"
         }]
```

#### latency (`optional`)

* time to wait, in `milliseconds`, before sending the response to the caller
* good for testing timeouts, or slow connections

```yaml
-  request:
      url: ^/hello/to/jupiter$
   response:
      latency: 800000
      body: Hello, World!
```

</details>

[Back to top](#table-of-contents)

### Dynamic token replacement in stubbed response

During HTTP request verification, you can leverage regex capturing groups ([Regex stubbing for dynamic matching](#regex-stubbing-for-dynamic-matching)) as token values for dynamic token replacement in stubbed response.

<details markdown=block>
  <summary markdown=span>

   <code>
     Click to expand
   </code>

  </summary>

stubby supports dynamic token replacement on the following properties:
- `response` `body`
- `response` `header` name values (including `location` header value)
- `response` `file` names & payloads.

#### Example
```yaml
-  request:
      method: [GET]
      url: ^/regex-fileserver/([a-z]+).html$

   response:
      status: 200
      file: ../html/<% url.1 %>.html


-  request:
      method: [GET]
      url: ^/v\d/identity/authorize
      query:
         redirect_uri: "https://(.*)/app.*"

   response:
      headers:
         location: https://<% query.redirect_uri.1 %>/auth
      status: 302
  
            
-  request:
      method: [GET]
      url: ^/account/(\d{5})/category/([a-zA-Z]+)
      query:
         date: "([a-zA-Z]+)"
      headers:
         custom-header: "[0-9]+"

   response:
      status: 200
      body: Returned invoice number# <% url.1 %> in category '<% url.2 %>' on the date '<% query.date.1 %>', using header custom-header <% headers.custom-header.0 %>
```
#### Example explained

The `url` regex `^/account/(\d{5})/category/([a-zA-Z]+)` has two defined capturing groups: `(\d{5})` and `([a-zA-Z]+)`, `query` regex has one defined capturing group `([a-zA-Z]+)`. In other words, a manually defined capturing group has parenthesis around it.

Although, the `headers` regex does not have capturing groups defined explicitly (no regex sections within parenthesis), its matched value is still accessible in a template (keep on reading!).

#### Token structure
The tokens in `response` `body` follow the format of `<%` `PROPERTY_NAME` `.` `CAPTURING_GROUP_ID` `%>`. If it is a token that should correspond to `headers` or `query` regex match, then the token structure would be as follows: `<%` `HEADERS_OR_QUERY` `.` `KEY_NAME` `.` `CAPTURING_GROUP_ID` `%>`. Whitespace is __allowed__ between the `<%` & `%>` and what's inside.

#### Numbering the tokens based on capturing groups without sub-groups
When giving tokens their ID based on the count of manually defined capturing groups within regex, you should start from `1`, not zero (zero reserved for token that holds __full__ regex match) from left to right. So the leftmost capturing group would be `1` and the next one to the right of it would be `2`, etc.

In other words `<% url.1 %>` and `<% url.2 %>` tokens correspond to two capturing groups from the `url` regex `(\d{5})` and `([a-zA-Z]+)`, while `<% query.date.1 %>` token corresponds to one capturing group `([a-zA-Z]+)` from the `query` `date` property regex.

#### Numbering the tokens based on capturing groups with sub-groups
In regex world, capturing groups can contain capturing sub-groups, as an example consider proposed `url` regex: `^/resource/` `(` `([a-z]{3})` `-` `([0-9]{3})` `)` `$`. In the latter example, the regex has three groups - a parent group `([a-z]{3}-[0-9]{3})` and two sub-groups within: `([a-z]{3})` & `([0-9]{3})`.

When giving tokens their ID based on the count of capturing groups, you should start from `1`, not zero (zero reserved for token that holds __full__ regex match) from left to right. If a group has sub-group within, you count the sub-group(s) first (also from left to right) before counting the next one to the right of the parent group.

In other words tokens `<% url.1 %>`, `<% url.2 %>` and `<% url.3 %>` correspond to the three capturing groups from the `url` regex (starting from left to right): `([a-z]{3}-[0-9]{3})`, `([a-z]{3})` and `([0-9]{3})`.

#### Tokens with ID zero
Tokens with ID zero can obtain __full__ match value from the regex they reference. In other words, tokens with ID zero do not care whether regex has capturing groups defined or not. For example, token `<% url.0 %>` will be replaced with the `url` __full__ regex match from `^/account/(\d{5})/category/([a-zA-Z]+)`. So if you want to access the `url` __full__ regex match, respectively you would use token `<% url.0 %>` in your template.

Another example, would be the earlier case where `headers` `custom-header` property regex does not have capturing groups defined within. Which is fine, since the `<% headers.custom-header.0 %>` token corresponds to the __full__ regex match in the `header` `custom-header` property regex: `[0-9]+`.

It is also worth to mention, that the __full__ regex match value replacing token `<% query.date.0 %>`, would be equal to the regex capturing group value replacing `<% query.date.1 %>`. This is due to how the `query` `date` property regex is defined - the one and only capturing group in the `query` `date` regex, is also the __full__ regex itself.

#### Where to specify the template
You can specify template with tokens in both `body` as a string or using `file` by specifying template as external local file. When template is specified as `file`, the contents of local file from `file` will be replaced.

Alternatively, you can also template the path to the file itself:
```yaml
-  request:
      method: [GET]
      url: ^/regex-fileserver/([a-z]+).html$

   response:
      status: 200
      file: ../html/<% url.1 %>.html
```
When the request is recieved and the regex matches, the path to the file will get resolved and the file content will be served if it exists.

```yaml
-  request:
      method: POST
      url: /post-body-as-json
      headers:
         content-type: application/json
      post: >
         {"userId":"19","requestId":"(.*)","transactionDate":"(.*)","transactionTime":"(.*)"}

   response:
      headers:
         content-type: application/json
      status: 200
      body: >
         {"requestId": "<%post.1%>", "transactionDate": "<%post.2%>", "transactionTime": "<%post.3%>"}
```
Another example demonstrating the usage of tokens from the matched regex groups

#### When token interpolation happens
After successful HTTP request verification, if your `body` or contents of local file from `file` contain tokens - the tokens will be replaced just before rendering HTTP response.

#### Troubleshooting
* Make sure that the regex you used in your stubby4j configuration actually does what it suppose to do. Validate that it works before using it in stubby4j
* Make sure that the regex has capturing groups for the parts of regex you want to capture as token values. In other words, make sure that you did not forget the parenthesis within your regex if your token IDs start from `1`
* Make sure that you are using token ID zero, when wanting to use __full__ regex match as the token value
* Make sure that the token names you used in your template are correct: check that property name is correct, capturing group IDs, token ID of the __full__ match, the `<% ` and ` %>`

</details>

[Back to top](#table-of-contents)

### Stubbing HTTP 30x redirects

In order to stub a `30x` HTTP redirect, you need to stub the following:
* the `location` header in `headers` section of the `response`
* the `status` of the `response` must be `one` of the following HTTP codes: `301`, `302`, `303`, `307` or `308`.

#### Example
```yaml
- request:
    method: GET
    headers:
      content-type: application/json
    url: /item/redirect/source

  response:
    status: 301
    headers:
      location: /item/redirect/destination


- request:
    method: GET
    headers:
      content-type: application/json
    url: /item/redirect/destination

  response:
    headers:
      content-type: application/json
    status: 200
    body: >
      {"response" : "content"}
```
#### Example explained

Upon successful HTTP request verification, the `/item/redirect/destination` value of the stubbed `response` header `location` will be set as the value of the location header of the Jetty HTTP response, which will cause the redirect to another stub with `url` value `/item/redirect/destination`


[Back to top](#table-of-contents)


### Record and replay

If `body` of the stubbed `response` contains a URL starting with http(s), stubby knows that it should record an HTTP response
from the provided URL (before rendering the stubbed response) and replay the recorded HTTP response on each subsequent call.

#### Example
```yaml
-  request:
      method: [GET]
      url: /maps/api/geocode/json
      query:
         address: "1600%20Amphitheatre%20Parkway,%20Mountain%20View,%20CA"
         sensor: false

   response:
      status: 200
      headers:
         content-type: application/json
      body: http://maps.googleapis.com
```
#### Example explained

Upon successful HTTP request verification, properties of stubbed `request` (`method`, `url`, `headers`, `post` and `query`) are used to construct
an HTTP request to the destination URL specified in `body` of the stubbed `response`.

In the above example, stubby will record HTTP response received after submitting an HTTP GET request to the url below:
`http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=1600+Amphitheatre+Parkway,+Mountain+View,+CA`

#### Please note
* Recorded HTTP response is not persistable, but kept in memory only. In other words, upon stubby shutdown the recording is lost
* Make sure to specify in `response` `body` only the URL, without the path info. Path info should be specified in `request` `url`

[Back to top](#table-of-contents)

## Supplying stubbed endpoints to stubby

There are two ways available (listed in no particular order):
1. Submit `POST` requests to `localhost:8889` at runtime (check the [admin_portal.html](admin_portal.md))
2. Load a YAML config data-file (using `-d` / `--data` flags) with the following structure for each stubbed endpoint:

* `description`: optional description shown in logs
* `uuid`: optional unique identifier
* `request`: describes the client's call to the server
  * `method`: `GET`/`POST`/`PUT`/`PATCH`/`DELETE`/etc.
  * `url`: the URI regex string. GET parameters should also be included inline here
  * `query`: a key/value map of query string parameters included with the request. Query param value can be regex.
  * `headers`: a key/value map of headers the server should respond to. Header value can be regex.
  * `post`: a string matching the textual body of the response. Post value can be regex.
  * `file`: if specified, returns the contents of the given file as the request post. If the file cannot be found at request time, **post** is used instead
* `response`: describes the server's response (or array of responses, refer to the earlier examples) to the client
  * `headers`: a key/value map of headers the server should use in it's response.
  * `latency`: the time in milliseconds the server should wait before responding. Useful for testing timeouts and latency
  * `file`: if specified, returns the contents of the given file as the response body. If the file cannot be found at request time, **body** is used instead
  * `body`: the textual body of the server's response to the client
  * `status`: the numerical HTTP status code (200 for OK, 404 for NOT FOUND, etc.)

[Back to top](#table-of-contents)

### Splitting main YAML config

There are situations where your main YAML config file will grow and become bloated due to large number of stubs,
e.g.: your application talks to many downstream services.

stubby4j supports splitting the main YAML config file into multiple sub-config YAML files, which allows for more logical &
cleaner stub code organisation (kudos fly to https://github.com/harrysun2006).

#### Example

Main `data.yaml`:

```yaml
includes:
   - service-1-stubs.yaml
   - service-2-stubs.yaml
   - ...
   - ...
   - service-N-stubs.yaml
```

#### Example explained

You define the stubbed endpoints for each service (or any other logical organisation of stubs that suits your needs) in
their own `some-name-that-suits-you-N.yaml` sub-config files.

When stubby parses the main `data.yaml` provided using `-d` / `--data` flags, all included sub-configs will be loaded
as if all the stubs were defined in one YAML.

__Please note__

You `cannot mix` in the __same__ YAML config the `includes` with sub-configs & defining stubs using `request`/`response`, e.g.: stubby4j __will fail__ to load the following YAML:

```yaml
includes:
   - service-1-stubs.yaml
   - service-2-stubs.yaml
   - service-3-stubs.yaml
     
-  request:
      method:
         -  GET
         -  POST
         -  PUT
      url: ^/resources/asn/.*$

   response:
      status: 200
      body: >
         {"status": "ASN found!"}
      headers:
         content-type: application/json
```  

[Back to top](#table-of-contents)

## Managing stubs configuration via the REST API

`stubby4j` enables you to manage your `request` / `response` definitions via the REST API exposed by the [Admin Portal](admin_portal.md). See the [available REST API summary](admin_portal.md#available-rest-api-summary)

[Back to Home](../README.md#http-endpoint-configuration-howto)
