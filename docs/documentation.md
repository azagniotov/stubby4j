
## stubby4j documentation
<br />

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
<td>Description</td>
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
<td>Description</td>
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
<td>Description</td>
<td>
<ul>
<li>URI string</li>
<li>If you include query string in stubbed URI, the HTTP request WILL NOT match since stubbed URI compared only to URI from HTTP request. If you want to make string query params match, include them in the <i><b>query</b></i> key</li></ul></td>
</tr>
</table>
```
-  request:
      url: /some/uri
      
   request:
      url: /some/uri
      query:
         param: true
         anotherParam: false
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
<td>$.request.headers[*]<br />$.response.headers[*]</td>
</tr>
<tr>
<td>Description</td>
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
<td>Description</td>
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
