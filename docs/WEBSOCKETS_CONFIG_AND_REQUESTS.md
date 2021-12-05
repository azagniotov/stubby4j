
[Back to the main README.md](../README.md#websockets-configuration-and-requests)

## WebSockets configuration and requests

### Table of contents

* [Summary](#summary)
* [Available WebSockets endpoints](#available-websockets-endpoints)
* [WebSockets configuration HOWTO](#websockets-configuration-howto)
   * [Supported YAML properties](#supported-yaml-properties)
* [Managing websocket configuration via the REST API](#managing-websocket-configuration-via-the-rest-api)


### Summary

The WebSocket protocol provides a way to exchange data between a client and `stubby4j` over a persistent connection. The data can be passed in both directions with low latency and overhead, and without breaking the connection. WebSockets provide a bidirectional, full-duplex communications channel that operates over HTTP through a single TCP socket connection. This means the `stubby4j` can independently send data to the client without the client having to request it, and vice versa.

As of `v7.5.0` (incl.) of `stubby4j`, you can stub a WebSocket with the `stubby4j` app, and use it to send and receive messages across the WebSocket connection. On this page you will learn how to add a new websocket configuration to `stubby4j` stubs config, described in YAML.

Keep on reading to understand how to add websocket configurations to your `stubby4j` YAML config.

## Available WebSockets endpoints 

`stubby4j` versions `v7.5.x` only supports WebSocket protocol over the `HTTP/1.1` by the virtue of relying on jetty `v9.x.x` which does not support bootstrapping WebSockets with `HTTP/2`. Support for [RFC8441](https://datatracker.ietf.org/doc/html/rfc8441) WebSocket over HTTP/2 is available in [jetty v10.x.x and v11.x.x](https://webtide.com/jetty-10-and-11-have-arrived/). In the future versions, `stubby4j` will be powered by jetty `v11.x.x`.   

When `stubby4j` application starts, it exposes two WebSockets endpoints over `HTTP/1.1` - secure (i.e.: over TLS) & insecure. When making websocket requests to the stubbed websocket configuration, the request should be sent over to the websocket context root path `/ws/` (i.e.: `protocol_scheme://addreess:port/ws/`), __plus__ the stubbed request URI path. For example:

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

The following is a fully-populated example with multiple `web-socket` objects:

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


### Supported YAML properties

This section explains the usage, intent and behavior of each YAML property on the `web-socket` object. 

#### uuid (`optional`)

Unique identifier so it would be easier to [manage websocket configuration via the REST API](#managing-websocket-configuration-via-the-rest-api) at runtime 

#### description (`optional`)

This can be anything describing your websocket configuration. 

### on-open

The object `on-open` describes the behavior of the `stubby4j` websocket server when the connection between the server and your client is opened. With the `on-open` object you can configure what connection open events your client should receive and in what manner. The `on-open` object is optional __iif__ the object `on-message` (discussed further) has been declared in a `web-socket` config.

The `on-open` object supports the following properties: `policy`, `message-type`, `body`, `file`, `delay`

Keep on reading to understand their usage, intent and behavior.

### on-open object properties
  
#### policy (`required`)
  
Defines the behavior of the server, when it sends events to the connected client using the defined event metadata. Currently, the following five types of policies are supported:

* `once`: the server sends an event _once only_
* `push`: the server sends an event _in a periodic manner_. This property should be used together with the defined `delay` (discussed further) which describes the delay in milliseconds between the subsequent pushed events. The server keeps continuously pushing events until the connected client disconnects.
* `fragmentation`: the server sends an event payload in a form sequential fragmented frames one after another _in a periodic manner_, instead of sending the payload as a whole blob, which is the behavior of policies `once`, `push` and `disconnect`. 
   
   For example, let's say you want to configure `stubby4j` to stream a large file to the connected client. This property should be used together with the defined `delay` (discussed further) which describes the delay in milliseconds between the subsequent pushed frames.
* `ping`: the server sends a `Ping` event (without a payload) as per WebSocket spec [RFC6455#section-5.5.2](https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.2) _in a periodic manner_. This property should be used together with the defined `delay` (discussed further) which describes the delay in milliseconds between the subsequent pings. The server keeps continuously pinging until the connected client disconnects.

   The behavior of `ping` is similar to the `push` behavior, the difference here is that `stubby4j` sends a special binary data frame of type `Ping` instead of a text in UTF-8 or any other binary data. 
* `disconnect`: the server sends an event _once only_, followed by a WebSocket `Close` frame wit status code `1000`. In other words, the server gracefully closes the connection to the remote client endpoint

#### message-type (`required`)

Defines the payload type of the event, which the server sends to the connected client using the defined event metadata. 

Currently, the following two types of message types are supported:

* `text`: the event payload sent in a UTF-8 text format
* `binary`: the event payload sent as bytes

Please note, in case of `policy` of type `ping`, even if you defined `message-type: text`, the event payload will always be a binary data frame of type `Ping`, as per WebSocket spec [RFC6455#section-5.5.2](https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.2)

#### delay (`optional`)

Describes the delay in milliseconds between the subsequent server events to the connected client. Should be used in conjunction with defined policies of type `push`, `fragmentation` and `ping`.

If one of the aforementioned policy types is defined and the `delay` is not specified, the delay will be zero, i.e.: no delay between the subsequent server events. The `delay` takes no affect with the policies of type `once` and `disconnect`.


## Managing websocket configuration via the REST API

Just like with stubs management, `stubby4j` enables you to manage your `web-socket` definitions via the REST API exposed by the [Admin Portal](ADMIN_PORTAL.md). See the [available REST API summary](ADMIN_PORTAL.md#available-rest-api-summary)

[Back to the main README.md](../README.md#websockets-configuration-and-requests)
