package com.example.clientserver.service;

import com.example.clientserver.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class RaspberryPiService {

    private final ESP32ReportService esp32ReportService;
    private RaspberryPiReport raspberryPiReport;

    // URL to the Raspberry Pi
    private String uri;

    private String resource = "/server";

    // Reporting period in seconds to the Raspberry Pi
    private int reportingPeriod = 5;

    // Object mappers
    private ObjectMapper objectMapperForSendingJSON;
    private ObjectMapper objectMapperForReceivingJSON;

    @Autowired
    public RaspberryPiService(ESP32ReportService esp32ReportService) {

        this.esp32ReportService = esp32ReportService;

    }

    // Construct the class representing the state of this device
    public boolean constructRaspberryPiReport(String name) {

        UUID uuid = UUID.randomUUID();
        List<ESP32Report> esp32ReportList = new LinkedList<>();

        raspberryPiReport = new RaspberryPiReport(uuid, name, esp32ReportList);

        return true;

    }

    // Construct URI to Server
    public boolean constructURI(String ipAddress, String port) {

        uri = "http://" + ipAddress + ":" + port + resource;

        System.out.println(uri);

        return true;

    }

    // Initialize the JSON mappers
    public boolean initializeJSONMappers() {

        // Convert Object to JSON
        objectMapperForSendingJSON = new ObjectMapper();

        // Allow list to be represented as a nested JSON
        objectMapperForSendingJSON.enable(SerializationFeature.INDENT_OUTPUT);

        // For received JSON to object
        objectMapperForReceivingJSON = new ObjectMapper();

        return true;

    }

    // Update new actuator states received from server
    private void updateRaspberryPiValuesAndStates(String jsonResponse) {

        esp32ReportService.updateList(null, ESP32ReportService.ListOption.JSON_FROM_SERVER, null, null, objectMapperForReceivingJSON, jsonResponse);

    }

    // Report Raspberry Pi state
    private String generateJSONBody() {

        String jsonSend = null;
        jsonSend = esp32ReportService.updateList(null, ESP32ReportService.ListOption.JSON_TO_SERVER, raspberryPiReport, objectMapperForSendingJSON, null, null);
        return jsonSend;

    }

    public void setReportingPeriod(int reportingPeriod) {

        this.reportingPeriod = reportingPeriod;

    }

    public void raspberryPiStartReporting() {

        // HTTP client
        WebClient webClient = WebClient.create(uri);

        // Periodically every 5 seconds
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

                                updateRaspberryPiValuesAndStates(jsonResponse);

                            });
                })
                .subscribe();

        // Block main until termination signal arrives
        Flux.interval(Duration.ofSeconds(10)).blockLast();

    }

}
