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
```
# Report
1.	Question: In your report, explain the default life cycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions

•	JAX-RS default instantiates a single resource class each time a new HTTP request (request-scoped lifecycle) is received. This is the default specification and is the default used by Jersey unless it is an explicit application of @Singleton.
•	Since every request has its own resource object, any instance fields within a resource class are not shared across requests. Assuming mutable data (such as a HashMap<String, Room) are stored in a plain instance field, every request would begin with an empty map - data would be lost on the fly.
•	To ensure the integrity of data in a multi-threaded environment, the class level Hash Map is synchronized by being made static. This method is essential as Jersey container handles requests in parallel, with a thread pool. The API guarantees that numerous concurrent JAX-RS resource instances can communicate with the shared static state with the possibility of race conditions or data corruption. The approach creates a single source of truth in memory that ensures the same state across all parts of the application lifecycle without requiring an external database.

2.	Question: Why is the provision of “Hypermedia” (links and navigation within responses) considering a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

•	Hypermedia as the Engine of Application State (HATEOAS) is a usage of making navigational links within API responses, such that a client can dynamically find what actions it can perform without reference to documentation.
•	Why it is thought to be advanced REST: The original dissertation by Roy Fielding has HATEOAS as the highest level of the REST maturity model (Level 3 in the Richardson model). It breaks the relationship between clients and server URLs completely - clients take links and do not build hardcoded paths.
•	In static documentation, when the server alters a URL (e.g. /rooms to /campus-rooms), all clients will crash. Using HATEOAS, the clients are only aware of the discovery endpoint (GET /api/v1), and any additional paths are provided dynamically in responses. This allows the API to be self-describing, simpler to version and much less fragile to client developers.

3.	Question: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing

•	Returning IDs only saves network bandwidth at the cost of the client making N+1 requests (requesting the list and requesting each object detail separately) to amplify latency and load on the server. Sending full objects is more effective in the first processing of a client but it has bigger payloads. This implementation gives back full objects to favor a "chunky" over a "chatty" interface.

4.	Question: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple time.

•	Yes, it is. RoomResource DELETE /{roomId} is an idempotent operation. A side effect of an idempotent operation is that the effects of two or more identical requests are the same. When the first deletion is performed, the given Room is deleted. When the client resends the same request, the server state will not change: the resource will still not be present. Even though the answer can vary between 204 No Content and 404 Not Found, the server is in a stable state, thus meeting the idempotency contract.

5.	Question: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

•	The explicit method of using the @Consumes(Mediatype). The POST method on SensorResource has an annotation of APPLICATION_JSON) that has a strict Content-Type contract. When a client sends a payload in a format not supported by the runtime like application/xml or text/plain, then the JAX-RS runtime will perform an early validation and abort the request pipeline. The server will automatically send an HTTP 415 Unsupported Media Type reply. This is used to guarantee that the application does not deserialize incompatible or malformed data structures and so, internal Sensor POJ can be only populated with valid, structured JSON data, ensuring type safety at the service entry point.

6.	Question: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

•	Query parameters are semantically designed to be used to filter and search for a collection like (?type=CO2), whereas path segments are designed to identify a specific resource. Optional composable filters such as query parameters such as (?type=CO2 status=ACTIVE) can be used, without generating a giant, confusing hierarchy of URLs.

7.	Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con troller class?

•	This trend avoids God Classes by delegating reason to special resource classes. An example is SensorResource, which merely processes simple sensor data, and the sub-resource locator which transfers reading-specific logic to SensorReadingResource. This enhances maintainability, simplifies the navigability of the code and adheres to the Single Responsibility Principle.

8.	Question: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

•	In case a client tries to register a sensor, which is not part of an existing room, the LinkedResourceNotFoundExceptionMapper sends an HTTP 422 Unprocessable Entity. It is more semantically correct than a typical 404 Not Found. Although a 404 is an indication that the endpoint URL is not present, a 422 is an indication that the server has received and analyzed the request, but the business logic validation has failed because of an invalid internal reference. This offers the client developer accurate diagnostic data as to the type of failure, whether a navigation error or a data integrity error.

9.	Question: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

•	One of the most severe information disclosure vulnerabilities is exposing the raw Java stack traces to external consumers. A stack trace contains information about how the implementation was made, like which version of libraries were used (e.g., Jersey or Jackson), the internal naming conventions of the packages (such as com.smartcampus), and the file names on the server side, all of which can be used by an attacker to fingerprint the system and target a specific vulnerability. The GlobalExceptionMapper serves as an important "Safety Net," catching all unprocessed Throwables and cleansing up the results in a generic HTTP 500 Internal Server Error. This makes the sensitive backend architecture transparent to external parties, greatly increasing the security posture of the API.

10.	Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single re-source method?

•	By implementing a bespoke LoggingFilter, which uses both ContainerRequestFilter and ContainerResponseFilter, the application can deal with cross cutting concerns in a central location. This can only be much more beneficial than adding manual Logger statements on a case-by-case basis to each individual resource method, since it follows the DRY (Don't Repeat Yourself) principle completely. Centralized filtering provides full observability; all requests and responses are recorded with similar metadata (Method, URI, and Status Code), even when the call is made to a different resource. This leads to a more maintainable and cleaner codebase that only implements business logic and offers strong diagnostic telemetry to the whole system.



