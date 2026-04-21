package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final Map<String, Sensor> sensors;
    private final Map<String, Map<String, SensorReading>> allReadings;

    public SensorReadingResource(String sensorId, Map<String, Sensor> sensors, 
                                 Map<String, Map<String, SensorReading>> allReadings) {
        this.sensorId = sensorId;
        this.sensors = sensors;
        this.allReadings = allReadings;
    }

    @GET
    public Response getReadings() {
        Map<String, SensorReading> sensorReadings = allReadings.getOrDefault(sensorId, new HashMap<>());
        return Response.ok(sensorReadings.values()).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404).entity("{\"error\":\"Sensor not found\"}").build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is in MAINTENANCE mode and cannot accept new readings");
        }

        allReadings.get(sensorId).put(reading.getId(), reading);
        sensor.setCurrentValue(reading.getValue());

        return Response.status(201).entity(reading).build();
    }
}