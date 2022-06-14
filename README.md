# del-container
Digital Enhanced Living service container app  
_This container application is a research prototype and is currently under development. New features are constantly being added which may break existing functionality. Please refer to the development\_v2 branch for the latest changes._

## Table of Contents
1. [About](#about)
2. [Architecture](#architecture)
3. [Container APIs](#container-apis)
4. [Currently Available Data](#currently-available-data)
5. [Examples](#examples)

## About
The Digital Enhanced Living (DEL) container (del-container) is the container application and one of the user modules for the DEL platform which handles micro health services. The service manages several micro applications and allows a user to use several health services from one single app. The container exposes several APIs that can be used to develop mini health applications for the container. Other services contributing to the platform include [__del-auth__](https://github.com/benphilip1991/del-auth), [__del-web__](https://github.com/benphilip1991/del-web) and [__del-api__](https://github.com/benphilip1991/del-api). Once developed, registration of apps is much like other commercial app stores - mini health applications for the platform can be registered with the del-api service which then becomes available to users on the platform. More details can be found on the service page.  
_Please note that the repositories may be private at the moment and will be made public as they are developed._  
_The [__del-api__](https://github.com/benphilip1991/del-api) service is required for the app to function. Once launched, the appropriate IP address/hostname and port have to be configured in the constants class (com.del.delcontainer.utils.Constants) in the container._  

## Architecture
The container is based on the following architecture -  
<p align="center"><img src="./assets/del-container-arch.png" width="80%" height="80%"></p>

The figure shows four main components - the Application Manager, Data Manager, Conversation Manager and the Device Manager. These components are responsible for managing active mini-apps, handling the conversations with the bot, managing connected devices and user data.
Health data can be fetched from several sources including manual entry, from connected devices and even from other services. These can be divided into personal and environmental factors and are highlighted in the following figure -  

<p align="center"><img src="./assets/datapoints.png" width="80%" height="80%"></p>

The following sequence diagram outlines a high level control and data flow within the container -  

<p align="center"><img src="./assets/mini-app-data-flow.png" width="80%" height="80%"></p>



_Please note that the current prototype implementation does not support all these metrics. They will be added as the container is developed further._

## Container APIs
As of now, the container exposes one interface for registering application details, data requests and callbacks. All mini applications must contain the function **setAppId** that accepts a string ID passed by the container when the mini app is launched. This ID must then be used for registering requests or calling any other functions offered by the container.

```
// Sample - setAppId accepts the id injected by the container 
// which is then used later
function setAppId(appId) {
    this.appId = appId;
    initApp();
}
```

The mini apps can request available data from the container (see [Currently Available Data](#currently-available-data)) and these requests can be registered with the container with a call to the `setCallbackRequest` function in DelUtils that accepts a JSON string with the app id injected by the container on app launch along with an array (**requests**) of JSON objects specifying the **resource** and the **callback** function the container should call once the data is available. The following example shows a request object - 
```
{
    "resource" : "access_pedometer",
    "callback" : "processStepCount"
}
```

In the above example, once the step count is available, the container will attempt to call the _processStepCount_ function in the mini app and pass it two parameters - the **data type** and the **data**, which can then be used as required. The following example shows a simplified flow of the above steps performed in a mini app - 

```
// Expose function to accept app id from the container
function setAppId(appId) {
    this.appId = appId;
    initApp();
}

// Initialize app and register requests for location and step count
function initApp() {
    let appRequest = {
        "appId" : this.appId,
        "requests" : [
            {
                "resource" : "access_pedometer",
                "callback" : "processStepCount"
            },
            {
                "resource" : "access_location",
                "callback" : "processLocation"
            }
        ]
    };
    DelUtils.setCallbackRequest(JSON.stringify(appRequest));
}

// Use step count data
function processStepCount(dataType, steps) {
    if ("step_count" == dataType) {
        // Do something
    }
}

// Use the location data
function processLocation(dataType, location) {
    if ("location" == dataType) {
        // Do something
    }
}
```  

Data specific to a mini app can be managed in separate files on the container through the following functions - 
|Function     | Description     |
|-------------|-----------------|
| setAppData(appId, content)   | _Store app-specific data in a separate file_   |
| getAppData(appId)   | _Get previously stored app-specific data_   |


The following example shows their implementation - 
```
// Storing app specific data in file
function storeWorkoutData() {
    let appData = {
        // Application specific data stored in a separate file
    };

    DelUtils.setAppData(appId, appData);
}

// Getting app-specific data from file
function fetchWorkoutData() {
    let workoutData = DelUtils.getAppData(appId);
    // parse workout data and use as required
}
```

Smartphone sensors are often used in the micro-mHealth apps with the initialization process setting up the data provider tasks on the container. However, this does not implicitly enable sensor logs (i.e., storing data in the database). If needed, sensor data can be stored in the app database and later fetched using the following functions - 
|Function     | Description     |
|-------------|-----------------|
| setSensorLoggerRequest(requestDefinition)   | _Request the container to set logging requests_   |
| getSensorData(appId, type)   | _Get sensor data stored previously_  |
| getSensorData(appId, type, start-date, end-date)   | _Get sensor data stored previously filtered by date_   |

Setting the sensor logger request notifies the container data provider to log the requested sensor data in the database. The following figure shows an overview of the events starting/stopping this activity.
<p align="center"><img src="./assets/del-sensor-log-flow.png" width="80%" height="80%"></p>

Like the initial resource requests, sensor logs also require an object with two properties **resource** (see [Currently Available Data](#currently-available-data)) and **toggle** (true or false to enable or disable log requests) - 
```
{
    "resource": <requested resource>,
    "toggle"  : <true / false>
}
```

The following snippet shows how sensor logs are enabled for two sensors - 
```
function startWorkout() {
    // Log requests - set toggle to true to enable
    let loggerRequests = {
        "appId": this.appId,
        "requests": [{
            "resource": "access_pedometer",
            "toggle": true
        }, {
            "resource": "access_location",
            "toggle": true
        }]
    };

    // Start recording data
    DelUtils.setSensorLoggerRequest(JSON.stringify(loggerRequests));
}
```

Stored data can then be fetched using the overloaded `getSensorData` functions. The following example shows the use of the function to fetch stored location details -   
```
function getWorkoutData() {
    var appData = DelUtils.getSensorData(appId,
    "access_location",
    startTime,
    endTime);

    // use appData as required
}
```

Similarly, a mini app can also push a notification to the user through the `createNotification` function - 
|Function          | Description      |
|------------------|------------------|
|createNotification(appId, notificationMessage)     | _Push a notification through the container_   |

The following example shows the use of the notification function - 
```
function pushAppNotification() {
    // App logic
    let notificationMessage = "Some notification...";
    DelUtils.createNotification(this.appId, notificationMessage);
}
```

_More details to come soon!_  


## Currently Available Data
The project is currently a research prototype and offers limited functionality for testing and usability evaluations. More features would be added as the platform is developed further. Currently, the container provides access to the following data - 

|Data Type           | Resource Key     | Update Interval |
|--------------------|------------------|------------------|
| Location           | `access_location` | _5s_   |
| Step Count         | `access_pedometer` | _3s_  |
| Heart Rate         | `access_heart_rate` | _5s_ |
| Accelerometer (Raw Data)      | `access_accelerometer` | _250ms_ |

The step count and heart rate are currently provided as numbers while the location and raw accelerometer data are injected as a JSON string with the relevant details. The location object provides the _latitude_, _longitude_ and _accuracy_, and the accelerometer data comprises the raw acceleration in the _X_, _Y_ and _Z_ planes.
The following example demonstrates how this data can be extracted and used - 
```
// Handle accelerometer data
function accelerometerDataCallback(dataType, accelerometerData) {
    var accData = JSON.parse(accelerometerData);
    if ("accelerometer" == dataType && Object.keys(accData).length > 0) {
        var X = accData.X;
        var Y = accData.Y;
        var Z = accData.Z;

        // do something
    }
}


// Handle location data
function locationCallback(dataType, locationData) {
    var coordinates = JSON.parse(location);
    if (dataType == "location" && Object.keys(coordinates).length > 0) {
        var lat = coordinates.latitude;
        var long = coordinates.longitude;
        var accuracy = coordinates.accuracy;
            
        // do something
    }
}
```

_Please note that platform only supports a Zephyr HXM heart rate monitor. More devices will be added shortly_  
_More details to come soon!_  


## Examples
The following GIFs show a sample mini-health-app on the container.  
_The design has been updated since the GIFs were recorded. This will be updated soon!_  

  
Installation
<p style="align:left"><img src="./assets/install.gif" width="25%" height="25%"></p>  
  
Running  
<p style="align:right"><img src="./assets/run.gif" width="25%" height="25%"></p>

Sample App Demo  
<video src="https://user-images.githubusercontent.com/33706460/151330252-6893c4c0-802a-44af-91c2-71b09c0741aa.MP4" width="25%" height="25%">


### Sample Micro mHealth Apps
The following images show three single-feature micro-mHealth apps that were inspired by commercially available health and fitness apps.  
_Please note that these apps need to be registered with [__del-api__](https://github.com/benphilip1991/del-api) to work_

Exercise Tracker
<p style="align: left"><img src="./assets/micro_apps/MyMaps.png" width="25%" height="25%"></p>

Food Logger
<p style="align: left">
    <img src="./assets/micro_apps/FoodLogger.png" width="25%" height="25%">&nbsp;&nbsp;&nbsp;&nbsp;
    <img src="./assets/micro_apps/FoodLogger_Breakfast.png" width="25%" height="25%">
</p>

Mood Tracker
<p style="align: left">
    <img src="./assets/micro_apps/MyMoods_Stats.png" width="25%" height="25%">&nbsp;&nbsp;&nbsp;&nbsp;
    <img src="./assets/micro_apps/MyMoods_Daily_Mood.png" width="25%" height="25%">&nbsp;&nbsp;&nbsp;&nbsp;
    <img src="./assets/micro_apps/MyMoods_Daily Activities.png" width="25%" height="25%">
</p>