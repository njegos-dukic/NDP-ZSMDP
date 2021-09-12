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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import mdp.logger.MDPLogger;
import mdp.rest.model.Station;
import mdp.rest.model.Stations;

@Path("/stations")
public class StationController {

	public static String RESOURCES_ROOT = new StationController().getClass().getClassLoader().getResource("../resources/").getPath().toString();
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
	public Response getStations() {
		ArrayList<Station> requestedStations = new ArrayList<>();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		RedisClient redisClient = RedisClient.create(prop.getProperty("REDIS_ADDRESS"));
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		try {
			List<String> stations = connection.async().keys(prop.getProperty("REDIS_STATIONS_REGEX")).get();

			for (String stationId : stations)
				requestedStations
						.add(gson.fromJson(gson.toJson(connection.async().hgetall(stationId).get()), Station.class));
		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Error getting keys from Redis.");
		}

		Stations response = new Stations();
		response.setStations(requestedStations);

		return Response.status(Response.Status.OK).entity(response).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addStation(String station) {
		if (station == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid station info.").build();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Station stationObject = gson.fromJson(station, Station.class);

		RedisClient redisClient = RedisClient.create(prop.getProperty("REDIS_ADDRESS"));
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		try {
			List<String> stationIds = connection.async().keys(prop.getProperty("REDIS_STATIONS_REGEX")).get();
			int id = 1;

			try {
				id = stationIds.stream().mapToInt(s -> Integer.parseInt(s.split(prop.getProperty("REDIS_STATION_PREFIX"))[1])).max().getAsInt() + 1;
			} catch (Exception e) {
				MDPLogger.log(Level.INFO, "Error parsing id with POST request.");
			}

			stationObject.setId(id);
			String name = prop.getProperty("REDIS_STATION_PREFIX") + stationObject.getId();

			for (String stationId : stationIds)
				if (gson.fromJson(gson.toJson(connection.async().hgetall(stationId).get()), Station.class).getName()
						.equals(stationObject.getName()))
					return Response.status(Response.Status.CONFLICT).build();

			connection.async().hset(name, stationObject.toMap()).await(1, TimeUnit.DAYS);
		} catch (InterruptedException e1) {
			MDPLogger.log(Level.WARNING, "Stations POST interrupted.");

		} catch (ExecutionException e1) {
			MDPLogger.log(Level.WARNING, "Stations POST excecution exception.");
		}

		connection.close();
		redisClient.shutdown();

		return Response.status(Response.Status.OK).build();
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{stationId}")
	public Response deleteStation(@PathParam("stationId") String id) {
		if (id == null || "".equals(id)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid station info.").build();
		}

		RedisClient redisClient = RedisClient.create(prop.getProperty("REDIS_ADDRESS"));
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		try {
			String name = prop.getProperty("REDIS_STATION_PREFIX") + id;

			if (!connection.async().keys(prop.getProperty("REDIS_STATIONS_REGEX")).get().contains(name))
				return Response.status(Response.Status.NO_CONTENT).build();

			connection.async().del(name).await(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			MDPLogger.log(Level.WARNING, "Stations DELETE interrupted.");

		} catch (ExecutionException e) {
			MDPLogger.log(Level.WARNING, "Stations DELETE excecution exception.");
		}

		connection.close();
		redisClient.shutdown();

		return Response.status(Response.Status.OK).build();
	}
}
