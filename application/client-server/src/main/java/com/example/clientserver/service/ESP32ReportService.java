package com.example.clientserver.service;

import com.example.clientserver.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ESP32ReportService {

    // What to do with the list
    public enum ListOption {
        JSON_FROM_AND_TO_ESP32,
        JSON_TO_SERVER,
        JSON_FROM_SERVER,
        LATENCY_CHECK
    }

    // List of connected ESP32 devices
    private final List<ESP32ReportWrapper> connectedESP32List = new CopyOnWriteArrayList<>();

    // Allowed latency in seconds (until new report)
    private final long allowedDelay = 5;

    // Object mapper for sending JSON to ESP32
    private final ObjectMapper objectMapperForSendingJSON;

    // Maximum allowed connected EAP32 devices
    private final int MAX_CONNECTED_ESP32_DEVICES = 4;

    public ESP32ReportService() {

        this.objectMapperForSendingJSON = new ObjectMapper();
        objectMapperForSendingJSON.enable(SerializationFeature.INDENT_OUTPUT);

    }

    // Latency check every 3 seconds
    @Scheduled(fixedRate = 3000)
    public void loopingLatencyCheck() {

        updateList(null, ListOption.LATENCY_CHECK, null, null, null, null);

    }

    // Method that actually checks the latency
    private void latencyCheck() {

        // If latency is higher than the threshold, remove the ESP32 device from the list
        LocalDateTime lastActivityThreshold = LocalDateTime.now().minusSeconds(allowedDelay);
        connectedESP32List.removeIf(esp32ReportWrapper -> esp32ReportWrapper.getLastActivity().isBefore(lastActivityThreshold));

        System.out.println("ESP32 devices connected: " + connectedESP32List.size());

    }

    // Update list, returns JSON for sending to ESP32 or server
    public synchronized String updateList(ESP32Report esp32Report, ListOption listOption, RaspberryPiReport raspberryPiReport, ObjectMapper objectMapperForSendingJSON, ObjectMapper objectMapperForReceivingJSON, String jsonReceive) {

        if(listOption == ListOption.LATENCY_CHECK) {

            latencyCheck();

        } else if(listOption == ListOption.JSON_FROM_AND_TO_ESP32) {

            // Get time
            LocalDateTime lastActivity = LocalDateTime.now();

            // Temporary wrapper
            ESP32ReportWrapper esp32ReportWrapper = new ESP32ReportWrapper(esp32Report, lastActivity);

            // Locate the device in the list (if it exists)
            int index = connectedESP32List.indexOf(esp32ReportWrapper);

            // If wrapper doesn't exist
            if (index == -1) {

                // Check if amount of connected devices is already at its maximum
                // if(connectedESP32List.size() == MAX_CONNECTED_ESP32_DEVICES) return null;
                connectedESP32List.add(esp32ReportWrapper);

            } else {

                // Update the values and states
                esp32ReportWrapper = connectedESP32List.get(index);
                esp32ReportWrapper.setEsp32Report(esp32Report);
                esp32ReportWrapper.setLastActivity(lastActivity);

            }

            // Return JSON value
            ESP32Receive esp32Receive = esp32ReportWrapper.getEsp32Receive();

            String jsonReport = null;
            try {
                jsonReport = this.objectMapperForSendingJSON.writeValueAsString(esp32Receive);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.err.println("Error in object to JSON conversion!");
            }

            return jsonReport;

        } else if(listOption == ListOption.JSON_TO_SERVER) {

            for(ESP32ReportWrapper esp32ReportWrapper : connectedESP32List) {
                raspberryPiReport.getEsp32ReportList().add(esp32ReportWrapper.getEsp32Report());
            }

            String jsonReport = null;
            try {
                jsonReport = objectMapperForSendingJSON.writeValueAsString(raspberryPiReport);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.err.println("Error in object to JSON conversion!");
            }

            raspberryPiReport.getEsp32ReportList().clear();
            return jsonReport;

        } else if(listOption == ListOption.JSON_FROM_SERVER) {

            RaspberryPiReceive raspberryPiReceive = null;
            try {
                raspberryPiReceive = objectMapperForReceivingJSON.readValue(jsonReceive, RaspberryPiReceive.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.err.println("Error in JSON to object conversion!");
            }

            List<ESP32Receive> esp32ReceiveList = raspberryPiReceive.getEsp32ReceiveList();

            if(esp32ReceiveList != null) {

                for (ESP32Receive esp32Receive : esp32ReceiveList) {

                    for (ESP32ReportWrapper esp32ReportWrapper : connectedESP32List) {

                        if (esp32Receive.equals(esp32ReportWrapper.getEsp32Receive())) {

                            esp32ReportWrapper.setEsp32Receive(esp32Receive);

                        }

                    }

                }

            }

            return null;

        }

        return null;

    }

}
