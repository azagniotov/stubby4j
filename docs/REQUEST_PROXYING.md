[Back to the main README.md](../README.md#request-proxying)

## Request proxying

`stubby4j` can proxy requests to other services. This supports an ability to configure a proxy/intercept where requests are proxied to another service (i.e.: a real, live service), when such requests did not match any of the configured stubs. 

In order to configure a proxying behavior, you need to add a YAML configuration describing a proxy to an existing YAML stub `request`/`response` configuration. (See [Endpoint configuration HOWTO](../README.md#endpoint-configuration-howto)). Keep on reading to understand how to configure a proxy.

As of stubby4j `v7.3.0` it is possible to configure only `one` proxy configuration that serves as a catch-all for all requests that don't match any of the `stubby4j`'s stubs. In the future enhancements to the request proxying functionality, multiple proxy configurations will be supported.

### Table of contents

* [Proxy configuration HOWTO](#proxy-configuration-howto)
   * [Supported YAML properties](#supported-yaml-properties)
* [Proxied request & response tracking](#proxied-request--response-tracking)
* [Managing proxy configuration via the REST API](#managing-proxy-configuration-via-the-rest-api)


## Proxy configuration HOWTO

This section explains the usage, intent and behavior of each YAML property on the proxy configuration object. In `stubby4j` YAML config, the proxy configuration are denoted using the `proxy-config` key.

The following is a fully-populated `proxy-config` configuration:

```yaml
- proxy-config:
    uuid: default
    description: this is a default proxy config that serves as a catch-all for non-matched requests
    strategy: as-is
    properties:
      endpoint: https://jsonplaceholder.typicode.com
```

YAML proxy configuartion can be in the same YAML config as the stubs, i,e.: it is totaly OK to mix `request`/`response` ([Endpoint configuration HOWTO](../README.md#endpoint-configuration-howto)) and `proxy-config` in the same file. For example, the following is a totally valid YAML configuration:
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

- proxy-config:
    uuid: some-other-unique-name
    strategy: as-is
    properties:
      endpoint: https://jsonplaceholder.typicode.com
```

Alternatively, you may consider [splitting main YAML config](../README.md#splitting-main-yaml-config) in order to have logical separation in your YAML configuration between stubs and proxy config:

```yaml
includes:
  - include-all-test-stubs.yaml
  - include-proxy-config.yaml
```
(from: [main-test-stubs-with-proxy-config.yaml](../src/functional-test/resources/yaml/main-test-stubs-with-proxy-config.yaml))


### Supported YAML properties

#### uuid (`required`)

At this stage, explicitly setting this property to anything but `default` value reserved for future enhancements to the request proxying functionality.

When no explicit `uuid` is configured, an `uuid` will be configured internally using the value `default`. If you do choose to configure it, do not set it to anything other than `default` value.

#### description (`optional`)

This can be anything describing your proxy configuration. 

#### strategy (`required`)

Describes how the request to-be-proxied should be proxied. Currently only `as-is` startegy is supported, which means that request will be proxied without any changes/modifications before being sent to the proxy service. In the future enhancements to the request proxying functionality, more strategies will be supported.

#### properties (`required`)

Describes the properties of the proxy, e.g.: endpoint, headers, etc. Currently only `endpoint` property is supported. In the future enhancements to the request proxying functionality, more properties will be supported.

#### endpoint (`required`)

Describes the target service endpoint where the request will be proxied to. This should be a protocol scheme + fully qualified domain name (i.e.: `FQDN`) without any URI paths:
* Correct: `https://jsonplaceholder.typicode.com`
* Incorrect: `jsonplaceholder.typicode.com`
* Incorrect: `https://jsonplaceholder.typicode.com/`
* Incorrect: `https://jsonplaceholder.typicode.com/posts/1`

## Proxied request & response tracking

Before proxying the request, `stubby4j` will decorate the request being proxied with `x-stubby4j-proxy-request-uuid` header where its value will be a UUID, generated at runtime.

Upon receiving a response from the proxy service, before `stubby4j` renders the response, the response will be decorated with `x-stubby4j-proxy-response-uuid` containing the `same` aforementioned UUID value (the same value passed in as `x-stubby4j-proxy-request-uuid` header).

This makes it easy & convenient to correlate proxy responses from `stubby4j` to the proxied requests in the proxy service logs. 

## Managing proxy configuration via the REST API

Just like with stubs management, `stubby4j` enables you to manage your `proxy-config` definitions via the REST API exposed by the [Admin Portal](ADMIN_PORTAL.md). See the [available REST API summary](ADMIN_PORTAL.md#available-rest-api-summary)



[Back to the main README.md](../README.md#request-proxying)
