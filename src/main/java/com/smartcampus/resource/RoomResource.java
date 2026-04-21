package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private static final Map<String, Room> rooms = new HashMap<>();
    public static final Map<String, Sensor> sensors = new HashMap<>(); // shared with SensorResource

    @GET
    public Response getAllRooms() {
        return Response.ok(rooms.values()).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || rooms.containsKey(room.getId())) {
            return Response.status(400).entity("{\"error\":\"Room ID already exists or invalid\"}").build();
        }
        rooms.put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(404).entity("{\"error\":\"Room not found\"}").build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(404).entity("{\"error\":\"Room not found\"}").build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room with active sensors");
        }
        rooms.remove(roomId);
        return Response.noContent().build();
    }

    public static Room getRoomById(String roomId) {
        return rooms.get(roomId);
    }
}