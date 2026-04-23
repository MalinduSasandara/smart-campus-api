package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "v1");
        metadata.put("title", "Smart Campus Sensor & Room Management API");
        metadata.put("contact", "w2121320@westminster.ac.uk");
        metadata.put("resources", Map.of(
            "rooms", "/api/v1/rooms",
            "sensors", "/api/v1/sensors"
        ));

        return Response.ok(metadata).build();
    }
}