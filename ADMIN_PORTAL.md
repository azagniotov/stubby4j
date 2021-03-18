The admin portal is a `REST`ful(ish) APIs running on `<admin_portal_host>:<port>` (e.g.: `localhost`:`8889`) or wherever you described through stubby's command line args.

### Available REST API summary

#### Scenarios: creating new/overwriting existing stubs & proxy configs
| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|-----------|---------------|-------------------------------------|
|`POST`    | `/`          |`201`|`405`|overwrites all in-memory `stub` and/or `proxy-config`|

#### Scenarios: listing existing stubs & proxy configs as YAML string
| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|------|-----------|----------------------------------------------|
|`GET`    | `/`          |`200`|`400`|gets all in-memory `stub` & `proxy-config`|
|`GET`    | `/:stub_numeric_id`          |`200`|`400`|gets `stub` by its index in the YAML config|
|`GET`    | `/:uuid`          |`200`|`400`|gets `stub` by its `uuid` property |
|`GET`    | `/proxy-config/default`          |`200`|`400`|gets `default` `proxy-config`|
|`GET`    | `/proxy-config/:uuid`          |`200`|`400`|gets `proxy-config` by its `uuid` property|

#### Scenarios: updating existing stubs & proxy configs
| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|-----------|---------|-------------------------------------------|
|`PUT`    | `/:stub_numeric_id`          |`201`|`400`|updates `stub` by its index in the config|
|`PUT`    | `/:uuid`          |`201`|`400`|updates `stub` by its `uuid` property |
|`PUT`    | `/proxy-config/default`          |`201`|`400`|updates `default` `proxy-config`|
|`PUT`    | `/proxy-config/:uuid`          |`201`|`400`|updates `proxy-config` by its `uuid` property|

#### Scenarios: deleting existing stubs & proxy configs
| verb     | resource        |success|error|scenario                                         |
|------------------------|-------------|-----------|--------|--------------------------------------------|
|`DELETE`    | `/`          |`200`|`400`|deletes all in-memory `stub` & `proxy-config` |
|`DELETE`    | `/:stub_numeric_id`          |`200`|`400`|deletes `stub` by its index in the config|
|`DELETE`    | `/:uuid`          |`200`|`400`|deletes `stub` by its `uuid` property |
|`DELETE`    | `/proxy-config/:uuid`          |`200`|`400`|deletes `proxy-config` by its `uuid` property|

