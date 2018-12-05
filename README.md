## Getting started

Execute the following in the Jennifer management screen.

 1. Extension & Notice > Adapter and Plugin.
 2. Select the Labs tab.
 3. Click the Add button.
 4. Select the 'API' type.
 5. Enter the 'prometheus' ID.
 6. Enter the path to the 'dist/prometheus_jennifer-1.1.0.jar' file or upload it yourself.
 7. set token and url to options  
 ![screen shot 2018-11-22 at 18 36 00](https://user-images.githubusercontent.com/2956728/49141954-1bcf7180-f33b-11e8-8494-0f3ab44bd978.png)
 
## How to use options

Plugin options are shown in the table below.

| Key           | Default Value |
| ------------- |:-------------|
| url | http://127.0.0.1:7900 |
| token | dfsdfsfdsfsf |

## Grafana
![screen shot 2018-12-05 at 16 17 21](https://user-images.githubusercontent.com/2956728/49496497-7cb1f900-f8a9-11e8-8070-d20f86c89cdb.png)

## Prometheus EndPoing
| endpoint | description | applied version |
| ------------- |-------------|-------------|
| http://{{ Jennifer Host }}/plugin/realtimeapi/prometheus?token={{ PLUGIN TOKEN }}| realtime data | v1.0.0 |
| http://{{ Jennifer Host }}/plugin/dbmetrics/prometheus?token={{ PLUGIN TOKEN }}| dbmetrics data(1min avg.) | v1.0.0 |

## Release Note
### 2018-11-30: version v1.0.0 released  
### 2018-12-05: version v1.1.0 released  
1. Add dbmetrics data 
2. Source refractory
3. Name change "application" to "jdomain" 

## Supported version
Different versions of the server support different plug-in versions.
 
| Plugin version | Jennifer server version | Java version |
| ------------- |-------------|-------------|
| 1.1.0       | Greater than or equal to version 5.4.0.0 | 1.8 |
| 1.0.0       | Greater than or equal to version 5.4.0.0 | 1.8 |