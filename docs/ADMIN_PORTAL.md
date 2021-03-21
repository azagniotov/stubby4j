[Back to the main README.md](../README.md#the-admin-portal)

## stubby4j Admin Portal

### Table of contents

  * [The admin portal](#the-admin-portal)
  * [The status page](#the-status-page)
  * [Available REST API summary](#available-rest-api-summary)
  * [Scenarios: creating new/overwriting existing stubs & proxy configs](#scenarios-creating-newoverwriting-existing-stubs--proxy-configs)
  * [Scenarios: listing existing stubs & proxy configs as YAML string](#scenarios-listing-existing-stubs--proxy-configs-as-yaml-string)
  * [Scenarios: updating existing stubs & proxy configs](#scenarios-updating-existing-stubs--proxy-configs)
  * [Scenarios: deleting existing stubs & proxy configs](#scenarios-deleting-existing-stubs--proxy-configs)
  * [POST / PUT request body format](#post--put-request-body-format)

## The admin portal

Upon starting the `stubby4j` service, the admin portal runs on `<host>:<admin_port>` (e.g.: `localhost`:`8889`) or wherever you described through stubby's command line args.

The admin portal provides a web UI page (i.e.: `status` page) to view the stubbed data. In addition, the admin portal exposes a set of `REST`ful(ish) APIs that enable management of loaded in-memory stubs & proxy configs, which were loaded from the YAML config provided to `stubby4j` during start-up.


### The status page
You can view the configured stubs & proxy configs by navigating to `<host>:<admin_port>/status` from your browser

### Available REST API summary

> **CAVEATS**
>
> * Stubs can be updated/deleted by either:
>    * `stub_numeric_id`. The specific stub `stub_numeric_id` (resource-id-`<id>`) can be found when viewing stubs' YAML at `<admin_portal_host>:<port>/status`. Please note, deleting stubs by `stub_numeric_id` can get rather brittle when dealing with big or/and shared YAML configs. Therefore it is better to configure `uuid` property per stub in order to make the stub management easier & isolated.
>    * unique identifier (See [README.md "Stub/Feature UUID" section](../README.md#uuid-optional))
> * When defining only one proxy config (e.g.: a `catch-all` for all stubs), it must be configured without a `uuid` OR have a `uuid` with a value `default`
> * The `default` proxy config cannot be deleted via the `DELETE` REST API
> * Proxy configs can `only` be updated by a unique identifier, `uuid`
> * The `default` proxy config can be updated via the `PUT` REST API even if `uuid` has not been explicilty defined (it will be defined impliclty).
> * When updating stubs via `PUT` API, you can update only `one` stub at the time, i.e.: bulk updates are not supported yet.

------------------------------------------------------------------------------------------

#### Creating new/overwriting existing stubs & proxy configs

<details>
 <summary><code>POST</code> <code><b>/</b></code> <code>(overwrites all in-memory stub and/or proxy-config)</code></summary>

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `201`         | `text/plain;charset=UTF-8`        | `Configuration created successfully`                                |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |
> | `405`         | `text/html;charset=utf-8`         | None                                                                |

##### Example cURL

> ```javascript
>  curl -X POST -H "Content-Type: application/json" --data @post.json http://localhost:8889/
> ```

</details>

------------------------------------------------------------------------------------------

#### Listing existing stubs & proxy configs as YAML string

<details>
 <summary><code>GET</code> <code><b>/</b></code> <code>(gets all in-memory stub & proxy configs)</code></summary>

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/plain;charset=UTF-8`        | YAML string                                                         |

##### Example cURL

> ```javascript
>  curl -X GET -H "Content-Type: application/json" http://localhost:8889/
> ```

</details>

<details>
 <summary><code>GET</code> <code><b>/{stub_numeric_id}</b></code> <code>(gets stub by its resource-id-{stub_numeric_id} in the YAML config)</code></summary>

##### Parameters

> | name              | type              | description                                                 |
> |-------------------|-------------------|-------------------------------------------------------------|
> | `stub_numeric_id` | required          | The specific stub `stub_numeric_id` (resource-id-`<stub_numeric_id>`)    |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/plain;charset=UTF-8`        | YAML string                                                         |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |

##### Example cURL

> ```javascript
>  curl -X GET -H "Content-Type: application/json" http://localhost:8889/0
> ```

</details>

<details>
  <summary><code>GET</code> <code><b>/{uuid}</b></code> <code>(gets stub by its defined uuid property)</code></summary>

##### Parameters

> | name              | type              | description                                                 |
> |-------------------|-------------------|-------------------------------------------------------------|
> | `uuid`            | required          | unique identifier (See [README.md "Stub/Feature UUID" section](../README.md#uuid-optional))    |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/plain;charset=UTF-8`        | YAML string                                                         |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |

##### Example cURL

> ```javascript
>  curl -X GET -H "Content-Type: application/json" http://localhost:8889/some-unique-uuid-string
> ```

</details>


<details>
  <summary><code>GET</code> <code><b>/proxy-config/default</b></code> <code>(gets <b>default</b> proxy-config)</code></summary>

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/plain;charset=UTF-8`        | YAML string                                                         |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |

##### Example cURL

> ```javascript
>  curl -X GET -H "Content-Type: application/json" http://localhost:8889/proxy-config/default
> ```

</details>


<details>
  <summary><code>GET</code> <code><b>/proxy-config/{uuid}</b></code> <code>(gets proxy config by its uuid property)</code></summary>

##### Parameters

> | name              | type              | description                                                 |
> |-------------------|-------------------|-------------------------------------------------------------|
> | `uuid`            | required          | unique identifier (See [REQUEST_PROXYING.md "uuid"](REQUEST_PROXYING.md#uuid-required))    |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/plain;charset=UTF-8`        | YAML string                                                         |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |

##### Example cURL

> ```javascript
>  curl -X GET -H "Content-Type: application/json" http://localhost:8889/proxy-config/some-unique-uuid-string
> ```

</details>

------------------------------------------------------------------------------------------


#### Updating existing stubs & proxy configs

<details>
  <summary><code>PUT</code> <code><b>/{stub_numeric_id}</b></code> <code>(updates stub by its resource-id-{stub_numeric_id} in the config)</code></summary>

##### Parameters

> | name              | type              | description                                                 |
> |-------------------|-------------------|-------------------------------------------------------------|
> | `stub_numeric_id` | required          | The specific stub `stub_numeric_id` (resource-id-`<stub_numeric_id>`)    |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `201`         | `text/plain;charset=UTF-8`        | `Stub request index#<stub_numeric_id> updated successfully"`        |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |
> | `405`         | `text/html;charset=utf-8`         | None                                                                |

##### Example cURL

> ```javascript
>  curl -X PUT -H "Content-Type: application/json" --data @put.json http://localhost:8889/0
> ```

</details>

<details>
  <summary><code>PUT</code> <code><b>/{uuid}</b></code> <code>(updates stub by its defined uuid property)</code></summary>

##### Parameters

> | name              | type              | description                                                 |
> |-------------------|-------------------|-------------------------------------------------------------|
> | `uuid` | required          | unique identifier (See [README.md "Stub/Feature UUID" section](../README.md#uuid-optional))    |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `201`         | `text/plain;charset=UTF-8`        | `Stub request uuid#<uuid> updated successfully`                     |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |
> | `405`         | `text/html;charset=utf-8`         | None                                                                |

##### Example cURL

> ```javascript
>  curl -X PUT -H "Content-Type: application/json" --data @put.json http://localhost:8889/some-unique-uuid-string
> ```

</details>

<details>
  <summary><code>PUT</code> <code><b>/proxy-config/default</b></code> <code>(updates <b>default</b> proxy-config)</code></summary>

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `201`         | `text/plain;charset=UTF-8`        | `Proxy config uuid#default updated successfully`                    |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |
> | `405`         | `text/html;charset=utf-8`         | None                                                                |

##### Example cURL

> ```javascript
>  curl -X PUT -H "Content-Type: application/json" --data @put.json http://localhost:8889/proxy-config/default
> ```

</details>

<details>
  <summary><code>PUT</code> <code><b>/proxy-config/{uuid}</b></code> <code>(updates proxy-config by its uuid property)</code></summary>

##### Parameters

> | name              | type              | description                                                 |
> |-------------------|-------------------|-------------------------------------------------------------|
> | `uuid` | required          | unique identifier (See [REQUEST_PROXYING.md "uuid"](REQUEST_PROXYING.md#uuid-required))    |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `201`         | `text/plain;charset=UTF-8`        | `Proxy config uuid#<uuid> updated successfully`                     |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |
> | `405`         | `text/html;charset=utf-8`         | None                                                                |

##### Example cURL

> ```javascript
>  curl -X PUT -H "Content-Type: application/json" --data @put.json http://localhost:8889/proxy-config/some-unique-uuid-string
> ```

</details>

------------------------------------------------------------------------------------------

#### Deleting existing stubs & proxy configs

<details>
  <summary><code>DELETE</code> <code><b>/</b></code> <code>(deletes all in-memory stub & proxy configs)</code></summary>

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/plain;charset=UTF-8`        | `All in-memory YAML config was deleted successfully`                |

##### Example cURL

> ```javascript
>  curl -X DELETE -H "Content-Type: application/json" http://localhost:8889/
> ```

</details>

<details>
  <summary><code>DELETE</code> <code><b>/{stub_numeric_id}</b></code> <code>(deletes stub by its resource-id-{stub_numeric_id} in the config)</code></summary>

##### Parameters

> | name              | type              | description                                                 |
> |-------------------|-------------------|-------------------------------------------------------------|
> | `stub_numeric_id` | required          | The specific stub `stub_numeric_id` (resource-id-`<stub_numeric_id>`)    |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/plain;charset=UTF-8`        | `Stub request index#<stub_numeric_id> deleted successfully`         |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |

##### Example cURL

> ```javascript
>  curl -X DELETE -H "Content-Type: application/json" http://localhost:8889/0
> ```

</details>


<details>
  <summary><code>DELETE</code> <code><b>/{uuid}</b></code> <code>(updates stub by its defined uuid property)</code></summary>

##### Parameters

> | name              | type              | description                                                 |
> |-------------------|-------------------|-------------------------------------------------------------|
> | `uuid` | required          | unique identifier (See [README.md "Stub/Feature UUID" section](../README.md#uuid-optional))    |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/plain;charset=UTF-8`        | `Stub request uuid#<uuid> deleted successfully`                     |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |

##### Example cURL

> ```javascript
>  curl -X DELETE -H "Content-Type: application/json" http://localhost:8889/some-unique-uuid-string
> ```

</details>


<details>
  <summary><code>DELETE</code> <code><b>/proxy-config/{uuid}</b></code> <code>(deletes proxy-config by its uuid property)</code></summary>

##### Parameters

> | name              | type              | description                                                 |
> |-------------------|-------------------|-------------------------------------------------------------|
> | `uuid` | required          | unique identifier (See [REQUEST_PROXYING.md "uuid"](REQUEST_PROXYING.md#uuid-required))    |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `text/plain;charset=UTF-8`        | `Proxy config uuid#<uuid> deleted successfully`                     |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |

##### Example cURL

> ```javascript
>  curl -X DELETE -H "Content-Type: application/json" http://localhost:8889/proxy-config/some-unique-uuid-string
> ```

</details>

------------------------------------------------------------------------------------------

#### `POST` / `PUT` request body format

To manage the stubbed data via the `POST`/`PUT` API, structure the request payload as either a JSON array or YAML list `(-)` syntax.

##### JSON support
`JSON` is a subset of YAML 1.2, `SnakeYAML` that `stubby4j` leverages for YAML & JSON parsing implements YAML 1.1 (https://yaml.org/spec/1.1/)

##### `POST` / `PUT` JSON payload examples

Single stub payload

<details>
  <summary>Click to expand</summary>

```json
[
  {
    "request": {
      "url": "^/resources/something/new",
      "query": {
        "someKey": "someValue"
      },
      "method": [
        "GET"
      ]
    },
    "response": {
      "body": "OK",
      "headers": {
        "content-type": "application/xml"
      },
      "status": 201
    }
  }
]
```
</details>

Multiple stub payload

<details>
  <summary>Click to expand</summary>

```json
[
  { 
    "description": "this is a feature describing something",
    "request": {
      "url": "^/path/to/something$",
      "post": "this is some post data in textual format",
      "headers": {
         "authorization-basic": "bob:password"
      },
      "method": "POST"
    },
    "response": {
      "status": 200,
      "headers": {
        "Content-Type": "application/json"
      },
      "latency": 1000,
      "body": "Your request was successfully processed!"
    }
  },
  {
    "request": {
      "url": "^/path/to/anotherThing",
      "query": {
         "a": "anything",
         "b": "more"
      },
      "headers": {
        "Content-Type": "application/json"
      },
      "method": "GET"
    },
    "response": {
      "status": 204,
      "headers": {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
      },
      "file": "path/to/page.html"
    }
  },
  {
    "request": {
      "url": "^/path/to/thing$",
      "headers": {
        "Content-Type": "application/json"
      },
      "post": "this is some post data in textual format",
      "method": "POST"
    },
    "response": {
      "status": 304,
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }
]
```
</details>


Single stub with multiple proxy configs payload

<details>
  <summary>Click to expand</summary>

```json
[
  {
    "request": {
      "url": "/resources/something/new",
      "query": {
        "someKey": "someValue"
      },
      "method": [
        "GET"
      ]
    },
    "response": {
      "body": "OK",
      "headers": {
        "content-type": "application/xml"
      },
      "status": 201
    }
  },
  {
    "proxy-config": {
      "description": "this would be the default proxy config",
      "strategy": "as-is",
      "properties": {
        "endpoint": "https://google.com"
      }
    }
  },
  {
    "proxy-config": {
      "uuid": "some-unique-name-1",
      "strategy": "as-is",
      "properties": {
        "endpoint": "https://yahoo.com"
      }
    }
  },
  {
    "proxy-config": {
      "description": "this would be the 2nd description",
      "uuid": "some-unique-name-2",
      "strategy": "as-is",
      "properties": {
        "endpoint": "https://microsoft.com"
      }
    }
  }
]
```
</details>




[Back to the main README.md](../README.md#the-admin-portal)
