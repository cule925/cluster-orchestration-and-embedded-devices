package com.example.client;

import com.example.client.model.ESP32Receive;
import com.example.client.model.ESP32Report;
import com.example.client.model.MeasuringComponent;
import com.example.client.model.MeasuringComponentNew;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
public class ClientApplication {

	// Structure containing ESP32 state
	private static ESP32Report esp32Report = null;

	// URL to the Raspberry Pi
	private static String uri;
	private static String resource = "/raspberrypi";

	// Reporting period in seconds to the Raspberry Pi
	private static int defaultReportingPeriod = 5;
	private static int reportingPeriod;

	// Maximum measuring components
	private static final int MAX_MEASURING_COMPONENTS = 4;

	// Random value generator for sensors
	private static Random randomValueGenerator;

	// For sending JSON
	private static ObjectMapper objectMapperForSendingJSON = null;

	private static ObjectMapper objectMapperForReceivingJSON = null;

	// Construct the class representing the state of this device
	private static boolean constructESP32Report(String name) {

		UUID uuid = UUID.randomUUID();
		List<MeasuringComponent> measuringComponentList = new LinkedList<>();

		esp32Report = new ESP32Report(uuid, name, measuringComponentList);

		return true;

	}

	// Construct URI to Raspberry Pi
	private static boolean constructURI(String ipAddress, String port) {

		uri = "http://" + ipAddress + ":" + port + resource;
		return true;

	}

	// Initialize the JSON mappers
	public static boolean initializeJSONMappers() {

		// Convert Object to JSON
		objectMapperForSendingJSON = new ObjectMapper();

		// Allow list to be represented as a nested JSON
		objectMapperForSendingJSON.enable(SerializationFeature.INDENT_OUTPUT);

		// For received JSON to object
		objectMapperForReceivingJSON = new ObjectMapper();

		return true;

	}

	private static boolean readAvailableActuatorsAndSensors(String filePath) {

		File file = new File(filePath);

		// Check if file exists
		if(!file.exists()) {
			System.err.println("File doesn't exist!");
			return false;
		}

		// Read each JSON line and parse it into a class
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

			String line;
			ObjectMapper objectMapper = new ObjectMapper();

			// Read each line
			int lineCount = 0;
			while((line = br.readLine()) != null && lineCount < MAX_MEASURING_COMPONENTS) {

				MeasuringComponent measuringComponent = objectMapper.readValue(line, MeasuringComponent.class);
				esp32Report.getMeasuringComponentList().add(measuringComponent);
				lineCount++;

			}

        } catch (JsonMappingException e) {
			e.printStackTrace();
			System.err.println("Error in JSON to object mapping!");
			return false;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			System.err.println("Error in JSON to object conversion!");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error in reading from file!");
			return false;
		}

        return true;

	}

	private static void generateNewSensorValue(MeasuringComponent measuringComponent) {

		float valueMin = measuringComponent.getValueMin();
		float valueMax = measuringComponent.getValueMax();

		if(measuringComponent.getValueType() == MeasuringComponent.ValueType.VAL_BOOLEAN) {

			// Generate true or false
			boolean generatedBooleanValue = randomValueGenerator.nextBoolean();

			if(generatedBooleanValue) measuringComponent.setValue(1.0F);
			else measuringComponent.setValue(0.0F);

		} else if(measuringComponent.getValueType() == MeasuringComponent.ValueType.VAL_INT) {

			// Generate in bounds
			int generatedIntValue = randomValueGenerator.nextInt((int)(valueMax - valueMin) + 1) + (int)valueMin;

			measuringComponent.setValue((float)generatedIntValue);

		} else if(measuringComponent.getValueType() == MeasuringComponent.ValueType.VAL_FLOAT) {

			// Generate in bounds
			float generatedFloatValue = randomValueGenerator.nextFloat() * (valueMax - valueMin) + valueMin;

			measuringComponent.setValue(generatedFloatValue);

		}

	}

	// Update actuator states and sensor values
	private static void updateESP32ValuesAndStates(ESP32Receive esp32Receive) {

		for(MeasuringComponent measuringComponent : esp32Report.getMeasuringComponentList()) {

			// Generate new sensor values
			if(measuringComponent.getComponentType() == MeasuringComponent.ComponentType.COMPONENT_SENSOR) {

				generateNewSensorValue(measuringComponent);

			}

			// Set new actuator values
			else if(measuringComponent.getComponentType() == MeasuringComponent.ComponentType.COMPONENT_ACTUATOR) {

				int id = measuringComponent.getId();

				for(MeasuringComponentNew measuringComponentNew : esp32Receive.getMeasuringComponentNewList()) {

					int idNew = measuringComponentNew.getId();

					if(id == idNew) {

						float valueNew = measuringComponentNew.getValue();
						measuringComponent.setValue(valueNew);

					}

				}

			}

			System.out.println("Id: " + measuringComponent.getId() + ", name: " + measuringComponent.getName() + ", value: " + measuringComponent.getValue());

		}

	}

	// Generate JSON body for reporting
	public static String generateJSONBody() {

		String jsonSend = null;
		try {
			jsonSend = objectMapperForSendingJSON.writeValueAsString(esp32Report);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			System.err.println("Error in object to JSON conversion!");
			return null;
		}
		return jsonSend;

	}

	private static void esp32StartReporting() {

		// HTTP client
		WebClient webClient = WebClient.create(uri);

		// Periodically every X seconds
		Flux.interval(Duration.ofSeconds(reportingPeriod))
				.flatMap(var -> {

					// Send POST request with JSON data
					return webClient.post()
							.uri(uri)
							.contentType(MediaType.APPLICATION_JSON)
							.body(BodyInserters.fromValue(generateJSONBody()))
							.retrieve()
							.bodyToMono(String.class)
							.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)) // Retry 3 times with 5 seconds delay
									.filter(throwable -> {
										if (throwable instanceof WebClientResponseException) {
											WebClientResponseException responseException = (WebClientResponseException) throwable;
											System.err.println("Error in response. Retrying...");
											return responseException.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE;
										} else if (throwable instanceof WebClientRequestException) {
											System.err.println("Error in request. Retrying...");
											return true; // Retry for connection refused errors
										}
										return false;
									}))
							.doOnSuccess(jsonResponse -> {

								ESP32Receive esp32Receive = null;
								try {
									esp32Receive = objectMapperForReceivingJSON.readValue(jsonResponse, ESP32Receive.class);
									updateESP32ValuesAndStates(esp32Receive);
								} catch (JsonProcessingException e) {
									e.printStackTrace();
									System.err.println("Error in JSON to object conversion!");
								}

							});
				})
				.subscribe();

		// Block main until termination signal arrives
		Flux.interval(Duration.ofSeconds(10)).blockLast();

	}

	public static void run(String[] args) {

		// Check if argument size is valid
		if(args.length != 5) {
			System.err.println("Wrong argument number, arguments needed: <client node name> <ip address of client-server> <port of client-server> <filepath to sensor list> <reporting period>");
			return;
		}

		// Extract arguments
		String name = args[0];
		String ipAddress = args[1];
		String port = args[2];
		String filePath = args[3];
		String reportingPeriodString = args[4];

		try {
			reportingPeriod = Integer.parseInt(reportingPeriodString);
		} catch(NumberFormatException e) {
			e.printStackTrace();
			System.out.println("Setting default format");
			return;
		}

		// Construct ESP32 state class
		if(!constructESP32Report(name)) {
			System.err.println("Error in creating ESP32 state class!");
			return;
		}

		// Instantiate a random value generator
		randomValueGenerator = new Random();

		// Construct URI
		if(!constructURI(ipAddress, port)) {
			System.err.println("Error in creating URL!");
			return;
		}

		// Initialize JSON mappers
		if(!initializeJSONMappers()) {
			System.err.println("Error in initializing JSON mappers!");
			return;
		}

		// Read available sensors and actuators
		if(!readAvailableActuatorsAndSensors(filePath)) {
			System.err.println("Error in reading available sensors and actuators!");
			return;
		}

		esp32StartReporting();

	}

	public static void main(String[] args) {

		// SpringApplication.run(ClientApplication.class, args);
		run(args);

	}

}
