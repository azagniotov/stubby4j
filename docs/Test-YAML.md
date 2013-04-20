## YAML Configuration Explained
<br />
When creating stubbed request/response data for stubby4j, the config data should be specified in valid YAML 1.1 syntax. Submit POST requests to ```http://<host>:<admin_port>/stubdata/new``` or load a data file (```-d``` or ```--data```) with the following structure for each endpoint:
<br />

## Stub request and its properties


|||
|--------------|------------------------------------------
|Key           | request
|JSONPath      | `$.request`
|Required      | YES
|Description   | Describes the client's call to the server

<hr />

|||
|--------------|------------------------------------------
|Key           | method
|JSONPath      | `$.request.method[*]`
|Required      | YES
|Description   | * Holds HTTP method verbs
   "           + * If multiple verbs are defined, YAML array should be used
