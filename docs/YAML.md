## YAML Configuration Explained
<br />
When creating stubbed request/response data for stubby4j, the config data should be specified in valid YAML 1.1 syntax. Submit POST requests to ```http://<host>:<admin_port>/stubdata/new``` or load a data file (```-d``` or ```--data```) with the following structure for each endpoint:
<br />

## Stub request and its properties

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>request</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.request</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>Describes the client's call to the server </td>
</tr>
</table>

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>method</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.request.method[*]</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Holds HTTP method verbs</li>
<li>If multiple verbs are defined, YAML array should be used
</li></ul></td>
</tr>
</table>
```
-  request:
      method: GET
      
   request:
      method: [GET, HEAD]

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>url</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.request.url</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>URI string</li>
<li>If you include query string in stubbed URI, the HTTP request WILL NOT match since stubbed URI compared only to URI from HTTP request. If you want to make string query params match, include them in the <i><b>query</b></i> key</li>
<li>Supports regular expressions (similiar to <i>mod_rewrite</i> in Apache) for dynamic matching. The regular expression must be a valid Java regex and able to be compiled by <i>java.util.regex.Pattern</i></li> 
<li>When stubbing regular expression in <i><b>url</b></i>, string query params (if any) must be attached to the <i><b>url</b></i> unlike said in the first bullet point</li>
</ul></td>
</tr>
</table>
```
-  request:
      url: /some/uri


-  request:
      url: /some/uri
      query:
         param: true
         anotherParam: false


-  request:
      method: GET
      url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\?paramOne=[a-zA-Z]{3,8}&paramTwo=[a-zA-Z]{3,8}


-  request:
      method: GET
      url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\?view=full&status=active


-  request:
      method: GET
      url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$


-  request:
      method: GET
      url: ^/(account|profile)/user/session/[a-zA-Z0-9]{32}/?
```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>headers</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.request.headers[*]</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Key/value map of HTTP headers the server should read from the request</li>
<li>If stubbed headers are a subset of headers in HTTP request, then the match is successful (<i>left outer join</i> concept)</li>
</td>
</tr>
</table>
```
-  request:
      method: POST
      headers:
         content-type: application/json
         content-length: 80

   response:
      headers:
         content-type: application/json

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>query</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.request.query[*]</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Key/value map of query string params the server should read from the URI </li>
<li>The stubbed key (param name) must have the letter case as the query string param name, ie: HTTP request query string <i><b>paRamNaME=12 => paRamNaME: 12</b></i></li>
<li>The order of query string params does not matter. In other words the 
<i><b>server.com?something=1&else=2</b></i> is the same as <i><b>server.com?else=2&something=1</b></i></li>
<li>If stubbed query params are a subset of query params in HTTP request, then the match is successful (<i>left outer join</i> concept)</li>
<li>query param can also be an array with double/single quoted/un-quoted elements: <i><b>attributes=["id","uuid"]</b></i> or <i><b>attributes=[id,uuid]</b></i>. Please note no spaces between the CSV</li>
</td>
</tr>
</table>
```
-  request:
      url: /some/uri
      query:
         paramTwo: 12345
         paramOne: valueOne
      method: POST


-  request:
      method: GET
      url: /entity.find
      query:
         type_name: user
         client_id: id
         client_secret: secret
         attributes: '["id","uuid","created","lastUpdated","displayName","email","givenName","familyName"]'

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>post</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.request.post</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>String matching the textual body of the POST request.</li>
</td>
</tr>
</table>
```
-  request:
      method: POST
      post: >
         {
            "name": "value",
            "param": "description"
         }


-  request:
      url: /some/uri
      post: this is some post data in textual format
```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>file</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.request.file</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>If specified (an absolute path or path relative to the YAML in <i><b>-d</b></i> or <i><b>--data</b></i>), returns the contents of the given file as the stubbed <i><b>request</b></i> POST content</li>
<li>If the file was not provided, stubby fallsback to value from <i><b>post</b></i> property</li>
<li>If <i><b>post</b></i> key was not stubbed, it is assumed that stubbed POST body was not provided at all</li>
<li>Use file for large POST content that otherwise inconvenient to configure as a one-liner or you do not want to pollute YAML config</li>
<li>Please keep in mind: <i><b>SnakeYAML</b></i> library (used by stubby4j) parser ruins multi-line strings by not preserving system line breaks. If <i><b>file</b></i> property is stubbed, the file content is loaded as-is, in other words - it does not go through SnakeYAML parser. Therefore its better to load big POST content for <i><b>request</b></i> using <b><i>file</i></b> attribute. Keep in mind, stubby4j stub server is dumb and does not use smart matching mechanism (ie:. don't match line separators or don't match any white space characters) - whatever you stubbed, must be POSTed exactly for successful match</li>
</td>
</tr>
</table>
```
-  request:
      method: POST
      headers:
         content-type: application/json
      file: ../data/post-body-as-file.json     
```

## Stub response and its properties
<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>response</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.response</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>Describes stubby4j's response to the client</td>
</tr>
</table>

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>sequence</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.response.sequence[*].response</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>Sequence of multiple responses</td>
</tr>
</table>
```
-  request:
      method: [GET]
      url: /uri/with/sequenced/responses

   response:
      sequence:
         -  response:
               status: 200
               headers:
                  content-type: application/json
               body: OK

         -  response:
               status: 200
               headers:
                  content-type: application/json
               body: Still going strong!

         -  response:
               status: 500
               headers:
                  content-type: application/json
               body: OMFG!!!



-  request:
      method: [GET]
      url: /uri/with/single/sequenced/response

   response:
      sequence:
         -  response:
               status: 201
               headers:
                  content-type: application/json
               body: Still going strong!

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>status</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>YES</td>
</tr>
<td>JSONPath</td>
<td>$.response.status</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Numerical HTTP status code (200 for OK, 404 for NOT FOUND, etc.)</li></ul></td>
</tr>
</table>
```
  response:
      status: 200

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>headers</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.response.headers[*]</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Key/value map of HTTP headers the stubby4j should send with the response</li>
</td>
</tr>
</table>
```
  response:
      headers:
         content-type: application/json


   response:
      headers:
         content-type: application/pdf
         content-disposition: "attachment; filename=release-notes.pdf"
         pragma: no-cache
         
   response:
      status: 301
      headers:
         location: /some/other/uri
```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>latency</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.response.latency</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>Delay in milliseconds stubby4j should wait before responding to the client</li>
</td>
</tr>
</table>
```
  response:
      latency: 1000

```

<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>body</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.response.body</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>String matching the textual body of the response body</li>
</td>
</tr>
</table>
```
   response:
      body: OK
      status: 200
      
      
   response:
      latency: 1000
      body: >
         This is a text response, that can span across 
         multiple lines as long as appropriate indentation is in place.
      status: 200


   response:
      status: 200
      body: >
         {"status": "hello world"}
      headers:
         content-type: application/json
        
         
   response:
      headers:
         content-type: application/xml
         access-control-allow-origin: "*"
      latency: 1000
      body: >
         <?xml version="1.0" encoding="UTF-8"?>
            <Response>
            <Play loop="10">https://api.twilio.com/cowbell.mp3</Play>
         </Response>
      status: 200
```
<hr />

<table border="1" width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
<tr>
<td width="20">Key</td>
<td>file</td>
</tr>
<tr>
<tr>
<td width="20">Required</td>
<td>NO</td>
</tr>
<td>JSONPath</td>
<td>$.response.file</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<ul>
<li>If specified (an absolute path or path relative to the YAML in <i><b>-d</b></i> or <i><b>--data</b></i>), returns the contents of the given file as the HTTP response body</li>
<li>If the <i><b>file</b></i> was not provided, stubby fallsback to value from <i><b>body</b></i>property </li>
<li>If <i><b>body</b></i> was not provided, an empty string is returned by default</li>
<li>Can be ascii of binary file (PDF, images, etc.). Please keep in mind, that file is preloaded upon stubby4j startup and its content is kept in byte array in memory. In other words, response files are not read from the disk on demand, but preloaded.</li>
</td>
</tr>
</table>
```
-  response:
      status: 201
      headers:
         content-type: application/json
      file: ../data/response-body-as-file.json
```
