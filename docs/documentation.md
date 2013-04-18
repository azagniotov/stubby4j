
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

<br />

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
<td>```$.request.method[*]```</td>
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
<li>If stubbed headers are a subset of headers in HTTP request, then the match is successful (left outer join concept)</li>
</td>
</tr>
</table>
```
-  request:
      method: POST
      headers:
         content-type: application/json

   response:
      headers:
         content-type: application/json

```