package com.goeuro;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.commons.io.IOUtils;

import au.com.bytecode.opencsv.CSVWriter;

import com.goeuro.entity.GeoPosition;
import com.goeuro.entity.Location;

/**
 * @author Yauheni Zubovich
 */
public class Runner {

	private static final String ENDPOINT = "http://api.goeuro.com/api/v2/position/suggest/en/";

	public void run(String path) {
		String fullPath = String.format("%s%s", ENDPOINT, path);
		writeToFile(readLocations(fullPath));
	}

	/**
	 * Reading JSON from URL and parsing it to entities
	 */
	private List<Location> readLocations(String path) {
		List<Location> result = new ArrayList<Location>();
		InputStream inputStream = null;
		try {
			inputStream = new URL(path).openStream();
			JsonReader jsonReader = Json.createReader(inputStream);
			JsonArray locations = jsonReader.readArray();
			for (JsonObject o : locations.getValuesAs(JsonObject.class)) {
				result.add(parseLocationJson(o));
			}
			return result;
		} catch (IOException e) {
			System.out.println("Failure reading locations by URL " + path);
			return null;
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Writing results to CSV
	 */
	private void writeToFile(List<Location> locations) {
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
		File resultFile = new File("RESULT-" + format.format(new Date()) + ".csv");
		FileWriter fileWriter;
		try {
			if (!resultFile.createNewFile()) {
				System.out.println("Failure creating result csv file.");
			}
			fileWriter = new FileWriter(resultFile);
		} catch (IOException e) {
			System.out.println("Failure creating result csv file.");
			return;
		}
		CSVWriter csvWriter = new CSVWriter(fileWriter);
		csvWriter.writeAll(toStringArray(locations));
		try {
			csvWriter.close();
		} catch (IOException e) {
			System.out.println("Failure closing output stream");
			return;
		}
		System.out.println("Result file located: " + resultFile.getAbsolutePath());
	}

	/**
	 * Parsing single JsonObject to entity Location
	 */
	private Location parseLocationJson(JsonObject object) {
		Location location = new Location();
		if (object.get("_id") != null) {
			location.setId(object.getInt("_id"));
		}
		if (object.get("key") != null) {
			location.setKey(object.get("key").getValueType() != JsonValue.ValueType.NULL ? object.getString("key") : null);
		}
		if (object.get("name") != null) {
			location.setName(object.getString("name"));
		}
		if (object.get("fullName") != null) {
			location.setFullName(object.getString("fullName"));
		}
		if (object.get("iata_airport_code") != null) {
			location.setIataAirportCode(object.get("iata_airport_code").getValueType() != JsonValue.ValueType.NULL
                    ? object.getString("iata_airport_code") : null);
		}
		if (object.get("type") != null) {
			location.setType(object.getString("type"));
		}
		if (object.get("country") != null) {
			location.setCountry(object.getString("country"));
		}
		if (object.get("geo_position") != null) {
			location.setGeoPosition(parseGeopositionJson(object.getJsonObject("geo_position")));
		}
		if (object.get("location_id") != null) {
			location.setLocationId(object.get("location_id").getValueType() != JsonValue.ValueType.NULL
                    ? Long.valueOf(object.getString("location_id")) : null);
		}
		if (object.get("inEurope") != null) {
			location.setInEurope(object.getBoolean("inEurope"));
		}
		if (object.get("countryCode") != null) {
			location.setCountryCode(object.getString("countryCode"));
		}
		if (object.get("coreCountry") != null) {
			location.setCoreCountry(object.getBoolean("coreCountry"));
		}
		if (object.get("distance") != null) {
			location.setDistance(object.get("distance").getValueType() != JsonValue.ValueType.NULL ? object.getString("distance")
					: null);
		}
		return location;
	}

	/**
	 * Parsing GeoPosition
	 */
	private GeoPosition parseGeopositionJson(JsonObject object) {
		GeoPosition geoPosition = new GeoPosition();
		geoPosition.setLatitude(object.getJsonNumber("latitude").doubleValue());
		geoPosition.setLongitude(object.getJsonNumber("longitude").doubleValue());
		return geoPosition;
	}

	/**
	 * Transforming list Location to List records which be write to CSV
	 */
	private List<String[]> toStringArray(List<Location> locations) {
		List<String[]> records = new ArrayList<String[]>();
		records.add(new String[] { "_id", "name", "type", "latitude", "longitude" });
		for (Location l : locations) {
			records.add(new String[] { String.valueOf(l.getId()), l.getName(), l.getType(),
					String.valueOf(l.getGeoPosition().getLatitude()), String.valueOf(l.getGeoPosition().getLongitude()) });
		}
		return records;
	}

}
