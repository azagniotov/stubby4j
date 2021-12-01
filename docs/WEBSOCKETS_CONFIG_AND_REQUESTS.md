
[Back to the main README.md](../README.md#websockets-configuration-and-requests)

## WebSockets configuration and requests

### Table of contents

* [Summary](#summary)
* [Available WebSockets endpoints](#available-websockets-endpoints)
* [WebSockets configuration HOWTO](#websockets-configuration-howto)
   * [Supported YAML properties](#supported-yaml-properties)


### Summary

The WebSocket protocol provides a way to exchange data between a client and `stubby4j` over a persistent connection. The data can be passed in both directions with low latency and overhead, and without breaking the connection. WebSockets provide a bidirectional, full-duplex communications channel that operates over HTTP through a single TCP socket connection. This means the `stubby4j` can independently send data to the client without the client having to request it, and vice versa.

As of `v7.5.0` (incl.) of `stubby4j`, you can stub a WebSocket with a `stubby4j` server, and use it to send and receive messages across the WebSocket connection. On this page you will learn how to add a websocket configuration, described in YAML, to an existing stub `request`/`response` YAML configuration that you created as part of [Endpoint configuration HOWTO](../README.md#endpoint-configuration-howto).

Keep on reading to understand how to add websocket configurations to your `stubby4j` YAML config.

## Available WebSockets endpoints 

When `stubby4j` starts, it exposes two WebSockets endpoints - secure (i.e.: over TLS) & insecure. When making websocket requests to the stubbed websocket configuration, the request should be sent over to the websocket context root path `/ws/` (i.e.: `protocol_scheme://addreess:port/ws/`), __plus__ the stubbed request URI path. For example:

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

YAML proxy configuartion can be in the same YAML config as the HTTP stubs, i,e.: it is totally OK to mix configs for `request`/`response`/`proxy-config` ([Endpoint configuration HOWTO](../README.md#endpoint-configuration-howto)) & `web-socket` in the same file. For example, the following is a totally valid YAML configuration:

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
(from: [main-test-stubs-with-proxy-config.yaml](../src/functional-test/resources/yaml/main-test-stubs-with-web-socket-config.yaml))


### Supported YAML properties

This section explains the usage, intent and behavior of each YAML property on the `web-socket` object. 

TBD

[Back to the main README.md](../README.md#websockets-configuration-and-requests)
