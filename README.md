# Smart Campus Sensor & Room Management API

A RESTful API built with JAX-RS (Jersey) and Maven for managing rooms, sensors, and sensor readings in a smart campus environment.

**Base URL:** `http://localhost:8080/api/v1`

## Features Implemented

- Room CRUD operations with safety constraints
- Sensor management with room validation
- Sensor readings with sub-resource locator pattern
- Filtering sensors by type
- Proper error handling (409, 422, 403, 500)
- Request/Response logging
- HATEOAS-style discovery endpoint

## Project Structure
```bash
smart-campus-api/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── smartcampus/
│                   ├── SmartCampusApplication.java
│                   ├── model/
│                   │   ├── Room.java
│                   │   ├── Sensor.java
│                   │   └── SensorReading.java
│                   ├── resource/
│                   │   ├── DiscoveryResource.java
│                   │   ├── RoomResource.java
│                   │   ├── SensorResource.java
│                   │   └── SensorReadingResource.java
│                   ├── exception/
│                   │   ├── RoomNotEmptyException.java
│                   │   ├── LinkedResourceNotFoundException.java
│                   │   └── SensorUnavailableException.java
│                   ├── mapper/
│                   │   ├── RoomNotEmptyExceptionMapper.java
│                   │   ├── LinkedResourceNotFoundExceptionMapper.java
│                   │   ├── SensorUnavailableExceptionMapper.java
│                   │   └── GlobalExceptionMapper.java
│                   └── filter/
│                       └── LoggingFilter.java
├── pom.xml
└── README.md
```

## How to Run

1. Open the project in **NetBeans**
2. Right-click the project → **Clean and Build**
3. Right-click the project → **Run**
4. The server will start on Tomcat (port 8080)

**API Base URL:** `http://localhost:8080/api/v1`

## API Endpoints

Discovery
```
GET http://localhost:8080/api/v1
```
Rooms
```
GET     /rooms                  - Get all rooms
POST    /rooms                  - Create new room
GET     /rooms/{roomId}         - Get room by ID
DELETE  /rooms/{roomId}         - Delete room (blocked if sensors exist)
```
Sensors
```
GET     /sensors                        - Get all sensors
GET     /sensors?type=Temperature       - Filter by type
POST    /sensors                        - Create sensor (requires valid roomId)
```
Sensor Readings
```
GET     /sensors/{sensorId}/readings    - Get readings
POST    /sensors/{sensorId}/readings    - Add new reading
```
## Sample cURL Commands

Discovery
```
curl -v http://localhost:8080/api/v1
```
Create Room
```
curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"ENG-201\",\"name\":\"Engineering Lab\",\"capacity\":40}"
```
Create Sensor
```
curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"roomId\":\"ENG-201\"}"
```
Add Reading
```
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d "{\"value\":24.5}"
```
#Delete Room (should return 409 if sensor exists)
```
curl -X DELETE http://localhost:8080/api/v1/rooms/ENG-201
