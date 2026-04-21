package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // Shared with RoomResource
    public static final Map<String, Sensor> sensors = new HashMap<>();
    private static final Map<String, Map<String, com.smartcampus.model.SensorReading>> readings = new HashMap<>();

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getRoomId() == null || RoomResource.getRoomById(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException("Room with ID " + sensor.getRoomId() + " does not exist");
        }

        if (sensors.containsKey(sensor.getId())) {
            return Response.status(400).entity("{\"error\":\"Sensor ID already exists\"}").build();
        }

        sensors.put(sensor.getId(), sensor);
        readings.put(sensor.getId(), new HashMap<>());

        // Link sensor to room
        RoomResource.getRoomById(sensor.getRoomId()).addSensorId(sensor.getId());

        return Response.status(201).entity(sensor).build();
    }

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> result;
        if (type != null && !type.isEmpty()) {
            result = sensors.values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        } else {
            result = sensors.values();
        }
        return Response.ok(result).build();
    }

    // Sub-resource locator for readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId, sensors, readings);
    }
}