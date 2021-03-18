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

Upon starting the `stubby4j` service, the admin portal runs on `<admin_portal_host>:<port>` (e.g.: `localhost`:`8889`) or wherever you described through stubby's command line args.

The admin portal provides a web UI page (i.e.: `status` page) to view the stubbed data. In addition, the admin portal exposes a set of `REST`ful(ish) APIs that enables management of in-memory stubs & proxy configs, loaded from the YAML config provided to `stubby4j` during start-up.


### The status page
You can view the configured stubs & proxy configs by navigating to `<admin_portal_host>:<port>/status` from your browser

### Available REST API summary

##### Caveats
* Stubs can be updated/deleted by either:
  * `stub_numeric_id`. The specific stub `stub_numeric_id` (resource-id-`<id>`) can be found when viewing stubs' YAML at `<admin_portal_host>:<port>/status`. Please note, deleting stubs by `stub_numeric_id` can get rather brittle when dealing with big or/and shared YAML configs. Therefore it is better to configure `uuid` property per stub in order to make the stub management easier & isolated.
  * unique identifier (See [README.md "Stub/Feature UUID" section](README.md#uuid-optional))
* Proxy configs can `only` be updated by a unique identifier, `uuid`, if this property has been configured
* When proxy configs are configured, the `default` proxy config cannot be deleted via the `DELETE` REST API

#### Scenarios: creating new/overwriting existing stubs & proxy configs
| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|-----------|---------------|-------------------------------------|
|`POST`    | `/`          |`201`|`405`|overwrites all in-memory `stub` and/or `proxy-config`|

#### Scenarios: listing existing stubs & proxy configs as YAML string
| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|------|-----------|----------------------------------------------|
|`GET`    | `/`          |`200`|`400`|gets all in-memory `stub` & `proxy-config` configs|
|`GET`    | `/:stub_numeric_id`          |`200`|`400`|gets `stub` by its resource-id-`<id>` in the YAML config|
|`GET`    | `/:uuid`          |`200`|`400`|gets `stub` by its `uuid` property |
|`GET`    | `/proxy-config/default`          |`200`|`400`|gets `default` `proxy-config`|
|`GET`    | `/proxy-config/:uuid`          |`200`|`400`|gets `proxy-config` by its `uuid` property|

#### Scenarios: updating existing stubs & proxy configs

| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|-----------|---------|-------------------------------------------|
|`PUT`    | `/:stub_numeric_id`          |`201`|`400`|updates `stub` by its resource-id-`<id>` in the config|
|`PUT`    | `/:uuid`          |`201`|`400`|updates `stub` by its `uuid` property |
|`PUT`    | `/proxy-config/default`          |`201`|`400`|updates `default` `proxy-config`|
|`PUT`    | `/proxy-config/:uuid`          |`201`|`400`|updates `proxy-config` by its `uuid` property|

#### Scenarios: deleting existing stubs & proxy configs

| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|-----------|--------|--------------------------------------------|
|`DELETE`    | `/`          |`200`|`400`|deletes all in-memory `stub` & `proxy-config` |
|`DELETE`    | `/:stub_numeric_id`          |`200`|`400`|deletes `stub` by its resource-id-`<id>` in the config|
|`DELETE`    | `/:uuid`          |`200`|`400`|deletes `stub` by its `uuid` property |
|`DELETE`    | `/proxy-config/:uuid`          |`200`|`400`|deletes `proxy-config` by its `uuid` property|

#### `POST` / `PUT` request body format

To manage the stubbed data via the `POST`/`PUT` API, use either a JSON array or YAML list `(-)` syntax.

##### JSON support
`JSON` is a subset of YAML 1.2, `SnakeYAML` that `stubby4j` leverages for YAML & JSON parsing implements YAML 1.1 (https://yaml.org/spec/1.1/)

##### `POST` / `PUT` JSON payload examples

Single stub definition

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

Multiple stub definitions

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

Stub definition with multiple proxy configs

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


[Back to the main README.md](README.md)
