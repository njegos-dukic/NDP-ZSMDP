package mdp.rest.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import mdp.logger.MDPLogger;
import mdp.rest.model.StationArrivals;
import mdp.rest.model.Timetable;
import mdp.rest.model.Timetables;

@Path("/timetables")
public class TimetableController {

	public static String RESOURCES_ROOT = new TimetableController().getClass().getClassLoader().getResource("../resources/").getPath().toString();
	public static String RESOURCES_FILENAME = "app.properties";

	private static Properties prop = new Properties();

	static {
		InputStream input = null;

		try {
			input = new FileInputStream(RESOURCES_ROOT + RESOURCES_FILENAME);
			prop.load(input);
		} catch (FileNotFoundException e1) {
			MDPLogger.log(Level.INFO, "Propperties file not found.");
		} catch (IOException e) {
			MDPLogger.log(Level.INFO, "Error opening properties file.");
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTimetables() {
		ArrayList<Timetable> requestedTimetables = new ArrayList<>();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		RedisClient redisClient = RedisClient.create(prop.getProperty("REDIS_ADDRESS"));
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		try {
			List<String> timetables = connection.async().keys(prop.getProperty("REDIS_TIMETABLES_REGEX")).get();

			for (String timetableId : timetables)
				requestedTimetables.add(
					gson.fromJson(connection.async().hgetall(timetableId).get().get(prop.getProperty("REDIS_TIMETABLE_PREFIX")), Timetable.class));
		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Error fetching timetables.");
		}

		Timetables response = new Timetables();
		response.setTimetables(requestedTimetables);

		return Response.status(Response.Status.OK).entity(response).build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{station}")
	public Response getTimetableById(@PathParam("station") String station) {

		ArrayList<Timetable> requestedTimetables = new ArrayList<>();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		RedisClient redisClient = RedisClient.create(prop.getProperty("REDIS_ADDRESS"));
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		try {
			List<String> timetables = connection.async().keys(prop.getProperty("REDIS_TIMETABLES_REGEX")).get();

			for (String timetableId : timetables) {
				Timetable tt = gson.fromJson(connection.async().hgetall(timetableId).get().get(prop.getProperty("REDIS_TIMETABLE_PREFIX")),
						Timetable.class);
				for (StationArrivals sa : tt.getStations())
					if (sa.getStation().getName().trim().equals(station.trim()))
						requestedTimetables.add(tt);
			}

		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Error fetching timetables.");
		}

		Timetables response = new Timetables();
		response.setTimetables(requestedTimetables);

		return Response.status(Response.Status.OK).entity(response).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTimetable(String timetable) {
		if (timetable == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid timetable info.").build();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Timetable timetableObject = gson.fromJson(timetable, Timetable.class);

		RedisClient redisClient = RedisClient.create(prop.getProperty("REDIS_ADDRESS"));
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		try {
			List<String> timetableIds = connection.async().keys(prop.getProperty("REDIS_TIMETABLES_REGEX")).get();
			int id = 1;

			try {
				id = timetableIds.stream().mapToInt(s -> Integer.parseInt(s.split(prop.getProperty("REDIS_TIMETABLE_PREFIX"))[1])).max().getAsInt() + 1;

			} catch (Exception e) {
				MDPLogger.log(Level.WARNING, "Error fetching POST id.");
			}

			timetableObject.setId(id);
			String name = prop.getProperty("REDIS_TIMETABLE_PREFIX") + timetableObject.getId();

			connection.async().hset(name, timetableObject.toMap()).await(1, TimeUnit.DAYS);
		} catch (InterruptedException e1) {
			MDPLogger.log(Level.WARNING, "Timetables POST interrupted.");

		} catch (ExecutionException e1) {
			MDPLogger.log(Level.WARNING, "Timetables POST execution exception.");
		}

		connection.close();
		redisClient.shutdown();

		return Response.status(Response.Status.OK).build();
	}

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	@Path("{timetableId}")
	public Response updateTimetable(@PathParam("timetableId") String timetableId,
			@QueryParam("stationName") String stationName, String arrivalTime) {

		if (timetableId == null || stationName == null || arrivalTime == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid timetable info.").build();

		RedisClient redisClient = RedisClient.create(prop.getProperty("REDIS_ADDRESS"));
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		Gson gson = new Gson();

		try {
			Timetable tt = gson.fromJson(connection.async().hgetall(prop.getProperty("REDIS_TIMETABLE_PREFIX") + timetableId).get().get(prop.getProperty("REDIS_TIMETABLE_PREFIX")),
					Timetable.class);
			for (StationArrivals sa : tt.getStations())
				if (sa.getStation().getName().equals(stationName))
					sa.setPostedArrival(arrivalTime);

			connection.async().del(prop.getProperty("REDIS_TIMETABLE_PREFIX") + timetableId).await(1, TimeUnit.DAYS);
			connection.async().hset(prop.getProperty("REDIS_TIMETABLE_PREFIX") + timetableId, tt.toMap());

		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Timetables PUT interrupted.");
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid timetable info.").build();
		}

		return Response.status(Response.Status.OK).build();
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{timetableId}")
	public Response deleteTimetable(@PathParam("timetableId") String id) {
		if (id == null || "".equals(id)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid timetable info.").build();
		}

		RedisClient redisClient = RedisClient.create(prop.getProperty("REDIS_ADDRESS"));
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		try {
			String name = prop.getProperty("REDIS_TIMETABLE_PREFIX") + id;

			if (!connection.async().keys(prop.getProperty("REDIS_TIMETABLES_REGEX")).get().contains(name))
				return Response.status(Response.Status.NO_CONTENT).build();

			connection.async().del(name).await(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			MDPLogger.log(Level.WARNING, "Timetables DELETE interrupted.");

		} catch (ExecutionException e) {
			MDPLogger.log(Level.WARNING, "Timetables DELETE executing exception.");
		}

		connection.close();
		redisClient.shutdown();

		return Response.status(Response.Status.OK).build();
	}
}
