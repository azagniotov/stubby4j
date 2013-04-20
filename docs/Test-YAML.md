## YAML Configuration Explained
<br />
When creating stubbed request/response data for stubby4j, the config data should be specified in valid YAML 1.1 syntax. Submit POST requests to ```http://<host>:<admin_port>/stubdata/new``` or load a data file (```-d``` or ```--data```) with the following structure for each endpoint:
<br />

## Stub request and its properties

------------------------------------------------------------------------------------------------------
| Key           |	request
| Required      |	YES
| JSONPath      | $.request
| Description 	 | Describes the client's call to the server
------------------------------------------------------------------------------------------------------

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
