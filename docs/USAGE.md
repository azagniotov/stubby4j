## Usage
_______________

```
java -jar stubby4j-x.x.x.jar [-a <arg>] [-d <arg>] [-h] [-k <arg>]
       [-l <arg>] [-m] [-p <arg>] [-s <arg>]
       
 -a,--admin <arg>      Port for admin portal. Defaults to 8889.
 -d,--data <arg>       Data file to pre-load endpoints. Valid YAML 1.1 expected.
 -h,--help             This help text.
 -k,--keystore <arg>   Keystore file for enabling SSL. The default SSL port is 7443
 -l,--location <arg>   Hostname at which to bind stubby.
 -m,--mute             Prevent stubby from printing to the console
 -p,--password <arg>   Password for the provided keystore file.
 -s,--stubs <arg>      Port for stub portal. Defaults to 8882.
```