[Back to the main README.md](../README.md#request-proxying)

## Request proxying

As of `v7.3.0` of `stubby4j`, a new feature is available that enables you to configure a proxy/intercept where requests are proxied to another service (i.e.: a real, live service), when such requests did not match any of the configured stubs. 

On this page you will learn how to add a proxy configuration described in YAML to an existing stub `request`/`response` YAML configuration that you created as part of [Endpoint configuration HOWTO](../README.md#endpoint-configuration-howto). Currently, it is possible to add multiple proxy configurations for requests that don't match any of the `stubby4j`'s stubs.

In order to enable request proxying behavior, you need to add at least one proxy configuration to your YAML config, the default proxy config (denoted with `default proxy config` hereafter). The `default proxy config` serves as a catch-all for requests that don't match any of the `stubby4j`'s stubs.

In addition to the `default proxy config`, you can add additional proxy configurations. This is useful when you do not want to apply the same catch-all proxy behavior, but instead have a fine-grained per-request control which remote host you want to route the unmatched request to. You can control at runtime using an HTTP header `x-stubby4j-proxy-config-uuid` (set on the HTTP request to `stubby4j`) which proxy configuration should be applied if your request was not matched to any of the stubs.

Keep on reading to understand how to add proxy configurations to your `stubby4j` YAML config.

### Table of contents

* [Proxy configuration HOWTO](#proxy-configuration-howto)
   * [Supported YAML properties](#supported-yaml-properties)
* [Application of proxy config at runtime](#application-of-proxy-config-at-runtime)
* [Proxied request & response tracking](#proxied-request--response-tracking)
* [Managing proxy configuration via the REST API](#managing-proxy-configuration-via-the-rest-api)


## Proxy configuration HOWTO

This section explains the usage, intent and behavior of each YAML property on the proxy configuration object. In `stubby4j` YAML config, the proxy configuration are denoted using the `proxy-config` key.

The following is a fully-populated multiple `proxy-config` configuration:

```yaml
- proxy-config:
    description: this is a default proxy config that serves as a catch-all for non-matched requests
    strategy: as-is
    properties:
      endpoint: https://jsonplaceholder.typicode.com

- proxy-config:
    uuid: some-very-unique-key
    description: this is a non-default proxy config which hits Google
    strategy: as-is
    properties:
      endpoint: https://google.com
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

#### uuid

##### Property is `optional` when

Defining only a single `proxy-config` object in your YAML configuration. That single `proxy-config` object serves as a catch-all for all requests that don't match any of the stubby4j's stubs, it is the `default proxy config`.

When creating a `proxy-config` definition without an explicit `uuid` property, an `uuid` property will be configured internally with a value `default` (i.e.: `uuid: default`. You can however, explicitly define the `uuid` property even for the only `proxy-config` defined, but do not set it to anything other than value `default`.

##### Property is `required` when

Defining multiple `proxy-config` objects in your YAML configuration. The `uuid` property must have unique values across all defined `proxy-config` objects. Please note: you must always have defined a `default proxy config` (i.e.: with `uuid: default` or without `uuid` at all) when adding proxy configurations.

#### description (`optional`)

This can be anything describing your proxy configuration. 

#### strategy (`required`)

Describes how the request to-be-proxied should be proxied. Currently only `as-is` strategy is supported, which means that request will be proxied without any changes/modifications before being sent to the proxy service. In the future enhancements to the request proxying functionality, more strategies will be supported.

#### properties (`required`)

Describes the properties of the proxy, e.g.: endpoint, headers, etc. Currently only `endpoint` property is supported. In the future enhancements to the request proxying functionality, more properties will be supported.

#### endpoint (`required`)

Describes the target service endpoint where the request will be proxied to. This should be a protocol scheme + fully qualified domain name (i.e.: `FQDN`) without any URI paths:
* Correct: `https://jsonplaceholder.typicode.com`
* Incorrect: `jsonplaceholder.typicode.com`
* Incorrect: `https://jsonplaceholder.typicode.com/`
* Incorrect: `https://jsonplaceholder.typicode.com/posts/1`

## Application of proxy config at runtime

Request proxying happens when there is at least one `proxy config` object defined in the YAML config and the incoming HTTP request did not match any of the stubby4j's stubs.

First, `stubby4j` will check if the HTTP header `x-stubby4j-proxy-config-uuid` has been set on the incoming request. If header is set, then the header value will be used to apply the respective proxy configuration to the request. Please note: the header value must be one of the configured unique `uuid` values in your `proxy-config` objects.

If the aforementioned header is not set or it is set but there is no matching `proxy-config` for the provided `uuid` value, then the `default proxy config` will be used as a fallback. Please note: you `do not` need to pass in the `x-stubby4j-proxy-config-uuid` header if you have only `default proxy-config` in your YAML configuration.

## Proxied request & response tracking

Before proxying the request, `stubby4j` will decorate the request being proxied with `x-stubby4j-proxy-request-uuid` header where its value will be a UUID, generated at runtime.

Upon receiving a response from the proxy service, before `stubby4j` renders the response, the response will be decorated with `x-stubby4j-proxy-response-uuid` containing the `same` aforementioned UUID value (the same value passed in as `x-stubby4j-proxy-request-uuid` header).

This makes it easy & convenient to correlate proxy responses from `stubby4j` to the proxied requests in the proxy service logs. 

## Managing proxy configuration via the REST API

Just like with stubs management, `stubby4j` enables you to manage your `proxy-config` definitions via the REST API exposed by the [Admin Portal](ADMIN_PORTAL.md). See the [available REST API summary](ADMIN_PORTAL.md#available-rest-api-summary)



[Back to the main README.md](../README.md#request-proxying)
