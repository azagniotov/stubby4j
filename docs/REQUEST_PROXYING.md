[Back to the main README.md](../README.md#request-proxying)

## Request proxying

### Table of contents

* [Summary](#summary)
* [Proxy configuration HOWTO](#proxy-configuration-howto)
   * [Supported YAML properties](#supported-yaml-properties)
* [Application of proxy config at runtime](#application-of-proxy-config-at-runtime)
* [Proxied request & response tracking](#proxied-request--response-tracking)
* [Managing proxy configuration via the REST API](#managing-proxy-configuration-via-the-rest-api)


### Summary

As of `v7.3.0` (incl.) of `stubby4j`, it is possible to configure a proxy/intercept where requests are proxied to another service (i.e.: a real, live service), when such requests did not match any of the configured stubs. 

On this page you will learn how to add a proxy configuration, described in YAML, to an existing stub `request`/`response` YAML configuration that you created as part of [Endpoint configuration HOWTO](../README.md#endpoint-configuration-howto).

Keep on reading to understand how to add proxy configurations to your `stubby4j` YAML config.


## Proxy configuration HOWTO

In `stubby4j` YAML config, the proxy configuration metadata is declared using the `proxy-config` property.

In order to enable request proxying behavior in `stubby4j`, you need to add at least one `proxy-config` object to your YAML config. This would be the default proxy config (in this guide it is denoted as `default proxy config` hereafter). The `default proxy config` serves as a catch-all for requests that don't match any of the `stubby4j`'s stubs. To have only one `proxy-config` declared it is the most basic setup.

In addition to the declared `default proxy config`, you can also add additional `proxy-config`s. To have multiple proxy configurations is useful when you do not want to apply the same catch-all proxying behavior to all non-matched requests. Instead, you want to have a fine-grained per-request control which remote host you want to route an unmatched request by enforcing a specific proxying behavior at runtime. How to select a specific proxy configuration at runtime per-request discussed further in the [Application of proxy config at runtime](#application-of-proxy-config-at-runtime) section.

First, let's understand how to declare a proxy configuration. The following is a fully-populated example with multiple `proxy-config` objects:

```yaml
- proxy-config:
    description: this is a default proxy config that serves as a catch-all for non-matched requests
    strategy: as-is
    properties:
      endpoint: https://jsonplaceholder.typicode.com


- proxy-config:
    uuid: some-very-unique-string
    description: this is a non-default proxy config which hits Google
    strategy: as-is
    properties:
      endpoint: https://google.com
      

- proxy-config:
    uuid: another-very-unique-string
    description: this is a non-default proxy config which hits Yahoo
    strategy: as-is
    properties:
      endpoint: https://yahoo.com
```

YAML proxy configuartion can be in the same YAML config as the stubs, i,e.: it is totally OK to mix configs for `request`/`response` ([Endpoint configuration HOWTO](../README.md#endpoint-configuration-howto)) & `proxy-config` in the same file. For example, the following is a totally valid YAML configuration:

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

This section explains the usage, intent and behavior of each YAML property on the `proxy-config` object. 

#### uuid

##### Property is `optional` when

Defining only a single `proxy-config` object in your YAML configuration. That single `proxy-config` object serves as a catch-all for all requests that don't match any of the stubby4j's stubs, it is the `default proxy config`.

When creating a `proxy-config` definition without an explicit `uuid` property, an `uuid` property will be configured internally with a value `default` (i.e.: `uuid: default`. You can however, explicitly define the `uuid` property even for the single `proxy-config` defined, but do not set it to anything other than value `default`.

If you do set the `uuid` property on the single declared `proxy-config` object to something other than `default`, `stubby4j` fails to parse YAML config upon start-up and throws an exception. 

##### Property is `required` when

Defining multiple `proxy-config` objects in your YAML configuration. The `uuid` property must have unique values across all defined `proxy-config` objects. Please note: you must always have defined a `default proxy config` (i.e.: with `uuid: default` or without `uuid` at all) when adding proxy configurations.

If you do set multiple `uuid` properties to have the same values across multiple `proxy-config` objects, `stubby4j` fails to parse YAML config upon start-up and throws an exception. 

#### description (`optional`)

This can be anything describing your proxy configuration. 

#### strategy (`required`)

Describes how the request to-be-proxied should be proxied. Currently only the following strategy values are supported:
* `as-is`: no changes/modifications will be applied to the request before proxying it
* `additive`: additive changes will be applied to the request before proxying it. The additive changes currently supported are setting of additional HTTP headers using the `headers` property on the `proxy-config` object.

In the future enhancements to the request proxying functionality, more strategies will be supported.

#### properties (`required`)

A map of key/value pairs describing the properties of the proxy, e.g.: endpoint, etc. Currently only `endpoint` property is supported. In the future enhancements to the request proxying functionality, more properties will be supported.

##### endpoint (`required`)

Must be defined under the `properties` property. Describes the target service endpoint where the request will be proxied to. This should be a protocol scheme + fully qualified domain name (i.e.: `FQDN`) without any URI paths:

* Correct: `https://jsonplaceholder.typicode.com`
* Incorrect: `jsonplaceholder.typicode.com`
* Incorrect: `https://jsonplaceholder.typicode.com/`
* Incorrect: `https://jsonplaceholder.typicode.com/posts/1`

#### headers (`optional`)

A map of key/value pairs describing an HTTP header name and its value. The `headers` property can be used when `strategy` property is set to `additive`. The headers will be added to the request being proxied in an additive manner, i.e.: they will not replace the headers already set on the request.

```yaml
- proxy-config:
    uuid: some-other-unique-name
    strategy: additive
    properties:
      endpoint: https://jsonplaceholder.typicode.com
    headers:
      content-type: application/json+special
      x-custom-header: something/unique
      x-custom-header-2: another/thing
```

## Application of proxy config at runtime

Request proxying happens when there is at least one `proxy config` object defined in the YAML config and an incoming HTTP request did not match any of the declared `stubby4j`'s stubs.

When a request did not match any of the stubs, first, `stubby4j` will check if the HTTP header `x-stubby4j-proxy-config-uuid` has been set on the incoming request. This header allows you to control at runtime which proxy configuration should be picked for proxying of the unmatched request. As the header value, you should set the value of `uuid` property of the desired `proxy-config` object.  

If the aforementioned header is not set or it is set but there is no matching `proxy-config` for the provided `uuid` value (e.g.: you passed in a wrong value), then the `default proxy config` will be used as a fallback.

Please note: you `do not` need to pass in the `x-stubby4j-proxy-config-uuid` header if you have a single `proxy-config` object (i.e.: `default proxy-config`) in your YAML configuration.

## Proxied request & response tracking

Before proxying the request, `stubby4j` will decorate the request being proxied with `x-stubby4j-proxy-request-uuid` header where its value will be an arbitrary UUID, generated at runtime.

Upon receiving a response from the proxy service, before `stubby4j` renders the response, the response will be decorated with `x-stubby4j-proxy-response-uuid` containing the `same` aforementioned UUID value (the same value passed in as `x-stubby4j-proxy-request-uuid` header).

This makes it easy & convenient to correlate proxy responses from `stubby4j` to the proxied requests in the proxy service logs. 

## Managing proxy configuration via the REST API

Just like with stubs management, `stubby4j` enables you to manage your `proxy-config` definitions via the REST API exposed by the [Admin Portal](ADMIN_PORTAL.md). See the [available REST API summary](ADMIN_PORTAL.md#available-rest-api-summary)



[Back to the main README.md](../README.md#request-proxying)
