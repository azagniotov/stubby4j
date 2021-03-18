Upon starting the `stubby4j` service, the admin portal runs on `<admin_portal_host>:<port>` (e.g.: `localhost`:`8889`) or wherever you described through stubby's command line args.

The admin portal provides a web UI page (i.e.: `status` page) to view the stubbed data. In addition, the admin portal exposes a set of `REST`ful(ish) APIs that enables management of in-memory stubs & proxy configs, loaded from the YAML config provided to `stubby4j` during start-up.


### The status page
You can view the configured stubs & proxy configs by navigating to `<admin_portal_host>:<port>/status` from your browser

### Available REST API summary

#### Scenarios: creating new/overwriting existing stubs & proxy configs
| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|-----------|---------------|-------------------------------------|
|`POST`    | `/`          |`201`|`405`|overwrites all in-memory `stub` and/or `proxy-config`|

#### Scenarios: listing existing stubs & proxy configs as YAML string
| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|------|-----------|----------------------------------------------|
|`GET`    | `/`          |`200`|`400`|gets all in-memory `stub` & `proxy-config` configs|
|`GET`    | `/:stub_numeric_id`          |`200`|`400`|gets `stub` by its index in the YAML config|
|`GET`    | `/:uuid`          |`200`|`400`|gets `stub` by its `uuid` property |
|`GET`    | `/proxy-config/default`          |`200`|`400`|gets `default` `proxy-config`|
|`GET`    | `/proxy-config/:uuid`          |`200`|`400`|gets `proxy-config` by its `uuid` property|

#### Scenarios: updating existing stubs & proxy configs

* Stubs can be updated by either (a) `stub_numeric_id` or (b) unique identifier (See [Stub/Feature UUID](README.md#uuid-optional))
  * The specific stub `stub_numeric_id` (resource-id-`<id>`) can be found when viewing stubs' YAML at `<admin_portal_host>:<port>/status`.  
* Proxy configs can `only` be updated by a unique identifier, `uuid`, if this property has been configured


| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|-----------|---------|-------------------------------------------|
|`PUT`    | `/:stub_numeric_id`          |`201`|`400`|updates `stub` by its resource-id-`<id>` in the config|
|`PUT`    | `/:uuid`          |`201`|`400`|updates `stub` by its `uuid` property |
|`PUT`    | `/proxy-config/default`          |`201`|`400`|updates `default` `proxy-config`|
|`PUT`    | `/proxy-config/:uuid`          |`201`|`400`|updates `proxy-config` by its `uuid` property|

#### Scenarios: deleting existing stubs & proxy configs

* When `proxy-config`s are configured, the `default` proxy config cannot be deleted via the API

| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|-----------|--------|--------------------------------------------|
|`DELETE`    | `/`          |`200`|`400`|deletes all in-memory `stub` & `proxy-config` |
|`DELETE`    | `/:stub_numeric_id`          |`200`|`400`|deletes `stub` by its index in the config|
|`DELETE`    | `/:uuid`          |`200`|`400`|deletes `stub` by its `uuid` property |
|`DELETE`    | `/proxy-config/:uuid`          |`200`|`400`|deletes `proxy-config` by its `uuid` property|

#### `POST` / `PUT` payload examples

To manage the stubbed data via the `POST`/`PUT` API, use either a JSON array or YAML list `(-)` syntax.

##### JSON support
`JSON` is a subset of YAML 1.2, `SnakeYAML` that `stubby4j` leverages for YAML & JSON parsing implements YAML 1.1 (https://yaml.org/spec/1.1/)
