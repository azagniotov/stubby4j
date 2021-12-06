
[Back to the main README.md](../README.md#websockets-configuration-howto)

## WebSockets configuration HOWTO

### Table of contents

* [Summary](#summary)
* [Available WebSockets endpoints](#available-websockets-endpoints)
* [WebSockets configuration HOWTO](#websockets-configuration-howto)
   * [Fully-populated example with multiple web-socket objects](#fully-populated-example-with-multiple-web-socket-objects)
   * [Supported YAML properties](#supported-yaml-properties)
* [Managing websocket configuration via the REST API](#managing-websocket-configuration-via-the-rest-api)


### Summary

As of `v7.5.0` (incl.) of `stubby4j`, you can stub a WebSocket with the `stubby4j` app, and use it to send and receive messages across the WebSocket connection. On this page you will learn how to add a new websocket configuration to `stubby4j` stubs config, described in YAML.

The WebSocket protocol provides a way to exchange data between a client and `stubby4j` over a persistent connection. The data can be passed in both directions with low latency and overhead, and without breaking the connection. WebSockets provide a bidirectional, full-duplex communications channel that operates over HTTP through a single TCP socket connection. This means the `stubby4j` can independently send data to the client without the client having to request it, and vice versa.

Do note, that `stubby4j` versions `v7.5.x` only supports WebSocket protocol over the `HTTP/1.1`, by the virtue of relying on jetty `v9.x.x` which does not support bootstrapping WebSockets with `HTTP/2`. Support for [RFC8441](https://datatracker.ietf.org/doc/html/rfc8441) WebSocket over HTTP/2 is available in [jetty v10.x.x and v11.x.x](https://webtide.com/jetty-10-and-11-have-arrived/). In the future versions of `stubby4j`, the library will be powered by jetty `v11.x.x`.   

Keep on reading to understand how to add websocket configurations to your `stubby4j` YAML config.

## Available WebSockets endpoints 

When `stubby4j` application starts, it exposes two WebSockets endpoints over `HTTP/1.1`: secure (i.e.: over TLS) & insecure (i.e.: no TLS). When making websocket requests to the stubbed websocket configuration, the request should be sent over to the websocket context root path `/ws/` (i.e.: `protocol_scheme://addreess:port/ws/`), __plus__ the stubbed request URI path. For example:

* Insecure: `ws://<STUBS_HOSTNAME>:<STUBS_PORT>/ws/<URI_PATH>` => `ws://localhost:8882/ws/demo/web-socket/1`
* Secure: `wss://<STUBS_HOSTNAME>:<STUBS_TLS_PORT>/ws/<URI_PATH>` => `wss://localhost:7443/ws/demo/web-socket/1`

To note, if you want to make secure websocket requests , you have to add to your client certificate trust-store stubby4j's self-signed certificate (if you have not provided your own), see [Client-side TLS configuration](../README.md#client-side-tls-configuration) and [Making requests over TLS](../README.md#making-requests-over-tls) for more information 

Now let's understand how to declare a websocket configuration. 

## WebSockets configuration HOWTO

In `stubby4j` YAML config, the websocket configuration metadata is declared using the `web-socket` property. You can have multiple `web-socket` configured, where each `web-socket` must be uniquely identified by a request URI path, used for connecting to the websocket, for example:

```yaml
- web-socket:
    url: /demo/web-socket/1
    ...
    ...
    
- web-socket:
    url: /demo/web-socket/2
    ...
    ...
```

### Fully-populated example with multiple `web-socket` objects

<details>
  <summary><code>Click to expand</code></summary>
 <br />

```yaml
- web-socket:
    description: this is a web-socket config
    url: /demo/web-socket/1
    sub-protocols: echo, mamba, zumba

    on-open:
      policy: once
      message-type: text
      body: You have been successfully connected
      delay: 200

    on-message:
      - client-request:
          message-type: text
          body: do push
        server-response:
          policy: push
          message-type: text
          body: pushing
          delay: 50

      - client-request:
          message-type: text
          body: hello
        server-response:
          policy: once
          message-type: text
          body: bye-bye
          delay: 250

      - client-request:
          message-type: text
          body: disconnect with a message
        server-response:
          policy: disconnect
          message-type: text
          body: bon-voyage
          delay: 250

- web-socket:
    url: /demo/web-socket/2
    sub-protocols: echo, mamba, zumba

    on-open:
      policy: once
      message-type: text
      body: You have been successfully connected

    on-message:
      - client-request:
          message-type: text
          body: send-big-json
        server-response:
          policy: once
          message-type: binary
          file: ../json/response/json_response_1.json
          delay: 250

      - client-request:
          message-type: text
          body: push-pdf-to-me
        server-response:
          policy: push
          message-type: binary
          file: ../binary/hello-world.pdf
          delay: 500


- web-socket:
    url: /demo/web-socket/3
    sub-protocols: echo, mamba, zumba

    on-open:
      policy: once
      message-type: binary
      file: ../binary/hello-world.pdf
      delay: 200


- web-socket:
    url: /demo/web-socket/4

    on-message:
      - client-request:
          message-type: binary
          file: ../binary/hello-world.pdf
        server-response:
          policy: once
          message-type: binary
          file: ../binary/hello-world.pdf
          delay: 500

      - client-request:
          message-type: binary
          file: ../json/response/json_response_6.json
        server-response:
          policy: once
          message-type: binary
          file: ../binary/hello-world.pdf
          delay: 500

- web-socket:
    url: /demo/web-socket/5

    on-message:
      - client-request:
          message-type: text
          body: send-fragmentation-pls
        server-response:
          policy: fragmentation
          message-type: binary
          file: ../json/response/json_response_1.json
          delay: 10
```

</details>

[Back to top](#table-of-contents)

YAML websocket configuartion can be in the same YAML config as the other HTTP stub types supported by `stubby4j`, i,e.: it is totally OK to mix configs for `request`/`response`/`proxy-config` ([Endpoint configuration HOWTO](../README.md#endpoint-configuration-howto)) & `web-socket` in the same file. For example, the following is a totally valid YAML configuration:

```yaml
- proxy-config:
    uuid: default
    description: this is a default catch-all config
    strategy: as-is
    properties:
      endpoint: https://jsonplaceholder.typicode.com


- request:
    method:
      - GET
    url: /resources/user/1

  response:
    status: 200
    body: >
      {"status": "OK"}
    headers:
      content-type: application/json


- web-socket:
    url: /demo/web-socket/5

    on-message:
      - client-request:
          message-type: text
          body: send-fragmentation-pls
        server-response:
          policy: fragmentation
          message-type: binary
          file: ../json/response/json_response_1.json
          delay: 10
```

Alternatively, you may consider [splitting main YAML config](../README.md#splitting-main-yaml-config) in order to have logical separation in your YAML configuration between HTTP stubs and websocket configs:

```yaml
includes:
  - include-all-test-stubs.yaml
  - include-web-socket-config.yaml
```
(from: [main-test-stubs-with-web-socket-config.yaml](../src/functional-test/resources/yaml/main-test-stubs-with-web-socket-config.yaml))

[Back to top](#table-of-contents)

### Supported YAML properties

This section explains the usage, intent and behavior of each YAML property on the `web-socket` object. 

#### uuid (`optional`)

Unique identifier so it would be easier to [manage websocket configuration via the REST API](#managing-websocket-configuration-via-the-rest-api) at runtime 

#### description (`optional`)

This can be anything describing your websocket configuration. 

#### url (`required`)

Defines a unique (across all defined `web-scoket` objects in the YAML config) websocket URI path that client should connect to. The `url` should not include the websocket context root path `/ws/`. For example, if a client plans to open a webscoket connection to `ws://localhost:8882/ws/demo/web-socket/1`, then the value of the `url` property should be `/demo/web-socket/1`

```yaml
- web-socket:
    url: /demo/web-socket/1
    ...
    ...
```

#### sub-protocols (`optional`)

Defines a comma-separated arbitrary sub-protocol names. Defaults to empty string.

```yaml
- web-socket:
    url: /demo/web-socket/`
    sub-protocols: echo, mamba, zumba
    ...
    ...
```
If the `sub-protocols` is specified, then each value in the comma-separated value will be matched against the value of `Sec-WebSocket-Protocol` header in the client's request during the handshake with the connecting client. The server will include the same field and one of the matched subprotocol values in its response for the connection to be established. See [RFC6455#section-1.9](https://datatracker.ietf.org/doc/html/rfc6455#section-1.9) about subprotocols using the WebSocket protocol

If the the `sub-protocols` is specified, then the value was not matched against the value of `Sec-WebSocket-Protocol` header in the client's request, the `stubby4j` server will respond with `403` error.


##### What are the sub-protocols?

WebSocket protocol defines a mechanism to exchange arbitrary messages. What those messages mean, _what kind_ of messages a client can expect at any particular point in time or what messages they are allowed to send is entirely up to the implementing application.

Sub-protocol can help to reach an agreement between the server and client about these things, i.e.: it is like a protocol specification. The `sub-protocols` property lets clients formally exchange this information. You can just make up any name for any protocol you want. The server can simply check that the client's request appears to adhere to that protocol during the handshake.

[Back to top](#table-of-contents)

### on-open

The object `on-open` describes the behavior that `stubby4j` websocket server initiates first when the WebSocket connection between the server and a client is established. With the `on-open` object you can configure _what events_ your client should receive and in _what manner_, upon established connection without the need for the client to request them first. 

Do note the difference between the `on-open` server behavior VS the [on-message](#on-message) (discussed further) server behavior: with the behavior defined in [on-message](#on-message), the server requires an explicit request (i.e.: a trigger) from the connected client to start sending events.

To note:

* `on-open` object is __optional__ when the object [on-message](#on-message) (discussed further) has been declared in this `web-socket` config
* `on-open` object is __required__ when the object [on-message](#on-message) is not declared in this `web-socket` config

[Back to top](#table-of-contents)

### on-open object properties

The `on-open` object supports the following properties: `policy`, `message-type`, `body`, `file`, `delay`. Keep on reading to understand their usage, intent and behavior.
  
#### policy (`optional`)
  
Defines the behavior of the server, when it sends events to the connected client using the defined event metadata. Defaults to `once`. Currently, the following five types of policies are supported:

* `once`: the server sends an event _once only_
* `push`: the server sends an event _in a periodic manner_. This property should be used together with the defined [delay](#delay-optional) property. The `stubby4j` server will keep continuously pushing events to the connected client.
* `fragmentation`: the server sends an event payload in a form sequential fragmented frames one after another _in a periodic manner_, instead of sending the payload as a whole blob, which is the behavior of policies `once`, `push` and `disconnect`. 
   
   For example, let's say you want to configure `stubby4j` to stream a large file to the connected client. This property should be used together with the defined [delay](#delay-optional) property.
* `ping`: the server sends a `Ping` event (without a payload) as per WebSocket spec [RFC6455#section-5.5.2](https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.2) _in a periodic manner_. This property should be used together with the defined [delay](#delay-optional) property. The `stubby4j` server will keep continuously pinging the connected client.

   The behavior of `ping` is similar to the `push` behavior, the difference here is that `stubby4j` sends a special binary data frame of type `Ping` instead of a text in UTF-8 or any other binary data. 
* `disconnect`: the server sends an event _once only_, followed by a WebSocket `Close` frame wit status code `1000`. In other words, the server gracefully closes the connection to the remote client endpoint

[Back to top](#table-of-contents)

#### message-type (`optional`)

Defines the payload type of the event, which the server sends to the connected client using the defined event metadata. Defaults to `text`

Currently, the following two types of message types are supported:

* `text`: the event payload sent in a UTF-8 text format
* `binary`: the event payload sent as bytes

Please note, in case of `policy` of type `ping`, even if you defined `message-type: text`, the event payload will always be a binary data frame of type `Ping`, as per WebSocket spec [RFC6455#section-5.5.2](https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.2)

[Back to top](#table-of-contents)

#### delay (`optional`)

Describes the delay (in milliseconds) between the subsequent server events to the connected client. Defaults to zero. This property should be used in conjunction with defined `policy` of type `push`, `fragmentation` or `ping`.

If one of the aforementioned policy types is defined and the `delay` is not specified, there will be no delay between the subsequent server events to the connected client. The `delay` takes no affect with the policies of type `once` and `disconnect`.

[Back to top](#table-of-contents)

#### body (`optional`)

Contains contents of the server event payload to the connected client. Defaults to empty string. Depending on the defined `message-type`, the payload will be sent as text in UTF-8 format or bytes.

```yaml
- web-socket:
    ...
    ...

    on-open:
      ...
      message-type: binary
      body: >
         [{
            "name":"John",
            "email":"john@example.com"
         },{
            "name":"Jane",
            "email":"jane@example.com"
         }]
      ...
 ```

[Back to top](#table-of-contents)

#### file (`optional`)


Holds a path to a local file (it can be an absolute or relative path to the main YAML specified in `-d` or `--data`). This property allows you to split up your config across multiple files instead of making one huge bloated `web-config` YAML. Depending on the defined `message-type`, the payload will be sent as text in UTF-8 format or bytes.

For example, let's say you want the server to render a large JSON response body to the connected client, so instead of dumping a lot of text under the `body` property, you could specify a local file with the payload content using the `file` property (btw, the `file` property can also refer to binary files):

```yaml
- web-socket:
    ...
    ...

    on-open:
      ...
      message-type: binary
      file: ../json/extremelyLongJsonFile.json
      ...
 ```

Please note:

* If both `file` & `body` properties are supplied, the `file` takes precedence & replaces `body` with the contents from the provided file
* If the local file could not be loaded using the path form `file` property, `stubby4j` falls back to the value stubbed in `body`. If `body` was not stubbed, an empty string is returned by default
* Local file path specified in `file` it can be ASCII of binary file (PDF, images, etc.). Please keep in mind, that contents of the local file is preloaded upon `stubby4j` startup and its content is kept as a byte array in memory.


[Back to top](#table-of-contents)

### on-message

The object `on-message` describes the behavior of the `stubby4j` websocket server upon receiving a request from the connected client. With the `on-message` object you can configure _what events_ your client should receive and in _what manner_, upon receiving a client request.

Do note the difference between the `on-message` server behavior VS the [on-open](#on-open) server behavior: with the behavior defined in [on-open](#on-open), the server does not require an explicit request (i.e.: a trigger) from the connected client and starts sending events the moment the connection was established.

To note:

* `on-message` object is __optional__ when the object [on-open](#on-open) (discussed earlier) has been declared in this `web-socket` config
* `on-message` object is __required__ when the object [on-open](#on-open) is not declared in this `web-socket` config


[Back to top](#table-of-contents)

### on-message object properties

The `on-message` object supports the following properties: `client-request` and `server-response`. Keep on reading to understand their usage, intent and behavior.

#### client-request (`required`)

The `client-request` object describes the metadata of the incoming client request, in order to trigger `stubby4j` server websocket response described by the following `server-response` object. For example: 

```yaml
- web-socket:
    ...

    on-message:
      - client-request:
          message-type: text
          body: send-me-a-message
        server-response:
          ...
          ...
          ...

```

After WebSocket connection has been established and the server receives text message `send-me-a-message` from the connected client, the `stubby4j` server will render a response as described by the `server-response` object. 

The `client-request` object supports the following properties: `message-type`, `body` and `file`. Those are the same YAML properties as the properties discussed in [on-open object properties](#on-open-object-properties), the difference is here it is the message type and the request payload that the client must send to the server. So please refer to the aforementioned section to understand their usage, intent and behavior.

[Back to top](#table-of-contents)

#### server-response (`required`)

With the `server-response` object you can configure _what events_ your client should receive and in _what manner_ when the metadata of the incoming client request was matched the stubbed metadata in `client-request`.

The `server-response` object supports the following properties: `policy`, `message-type`, `body`, `file`, `delay`. Those are the same YAML properties as the properties discussed in [on-open object properties](#on-open-object-properties). So please refer to the aforementioned section to understand their usage, intent and behavior.

```yaml
- web-socket:
    ...
    ...

    on-message:
      - client-request:
          ...
          ...
        server-response:
          policy: fragmentation
          message-type: binary
          file: ../json/response/json_response_1.json
          delay: 10
 ```

[Back to top](#table-of-contents)

## Managing websocket configuration via the REST API

Just like with stubs management, `stubby4j` enables you to manage your `web-socket` definitions via the REST API exposed by the [Admin Portal](ADMIN_PORTAL.md). See the [available REST API summary](ADMIN_PORTAL.md#available-rest-api-summary)

[Back to the main README.md](../README.md#websockets-configuration-howto)
