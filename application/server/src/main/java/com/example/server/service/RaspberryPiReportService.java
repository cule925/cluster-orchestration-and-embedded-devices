package com.example.server.service;

import com.example.server.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

@Service
public class RaspberryPiReportService {

    public enum ListOption {
        JSON_FROM_AND_TO_RASPBERRY_PI,
        JSON_TO_WEBPAGE,
        JSON_FROM_WEBPAGE,
        LATENCY_CHECK
    }

    // List of connected Raspberry Pi devices
    private final List<RaspberryPiReportWrapper> connectedRaspberryPiList = new CopyOnWriteArrayList<>();

    // Allowed latency in seconds (until new report)
    private final long allowedDelay = 5;

    // Object mapper for sending JSON to ESP32
    private final ObjectMapper objectMapperForSendingJSON;

    public RaspberryPiReportService() {

        this.objectMapperForSendingJSON = new ObjectMapper();
        objectMapperForSendingJSON.enable(SerializationFeature.INDENT_OUTPUT);

    }

    // Latency check every 3 seconds
    @Scheduled(fixedRate = 3000)
    public void loopingLatencyCheck() {

        updateList(null, ListOption.LATENCY_CHECK, null, null);

    }

    // Method that actually checks the latency
    private void latencyCheck() {

        // If latency is higher than the threshold, remove the ESP32 device from the list
        LocalDateTime lastActivityThreshold = LocalDateTime.now().minusSeconds(allowedDelay);
        connectedRaspberryPiList.removeIf(raspberryPiReportWrapper -> raspberryPiReportWrapper.getLastActivity().isBefore(lastActivityThreshold));

        System.out.println("Raspberry Pi devices connected: " + connectedRaspberryPiList.size());

    }

    // Update list, returns JSON for sending to ESP32 or server
    public synchronized String updateList(RaspberryPiReport raspberryPiReport, ListOption listOption, WebPostJSON webPostJSON, ObjectMapper objectMapperForSendingJSON) {

        if(listOption == ListOption.LATENCY_CHECK) {

            latencyCheck();

        } else if(listOption == ListOption.JSON_FROM_AND_TO_RASPBERRY_PI) {

            // Get time
            LocalDateTime lastActivity = LocalDateTime.now();

            // Temporary wrapper
            RaspberryPiReportWrapper raspberryPiReportWrapper = new RaspberryPiReportWrapper(raspberryPiReport, lastActivity);

            // Locate the device in the list (if it exists)
            int index = connectedRaspberryPiList.indexOf(raspberryPiReportWrapper);

            // If wrapper doesn't exist
            if (index == -1) {

                connectedRaspberryPiList.add(raspberryPiReportWrapper);

            } else {

                // Update the values and states
                raspberryPiReportWrapper = connectedRaspberryPiList.get(index);
                raspberryPiReportWrapper.setLastActivity(lastActivity);

                // List of the new ESP32's
                List<ESP32Report> esp32ReportList = raspberryPiReport.getEsp32ReportList();
                raspberryPiReportWrapper.updateESP32List(esp32ReportList);

            }

            // Return JSON value
            RaspberryPiReceive raspberryPiReceive = raspberryPiReportWrapper.getRaspberryPiReceive();

            String jsonReport = null;
            try {
                jsonReport = this.objectMapperForSendingJSON.writeValueAsString(raspberryPiReceive);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.err.println("Error in object to JSON conversion!");
            }

            return jsonReport;

        } else if(listOption == ListOption.JSON_FROM_WEBPAGE) {

            // Iterate over the list of connected Raspberry Pi devices
            for(RaspberryPiReportWrapper raspberryPiReportWrapper : connectedRaspberryPiList) {

                // Search for target Raspberry Pi in list
                if(raspberryPiReportWrapper.getRaspberryPiReceive().getUuid().equals(webPostJSON.getRaspberryPiUUID())) {

                    RaspberryPiReceive raspberryPiReceiveTarget = raspberryPiReportWrapper.getRaspberryPiReceive();

                    // Iterate over a list of connected ESP32 devices
                    for(ESP32Receive esp32Receive : raspberryPiReceiveTarget.getEsp32ReceiveList()) {

                        // Search for target ESP32 in list
                        if(esp32Receive.getUuid().equals(webPostJSON.getEsp32UUID())) {

                            // Iterate over a list of actuators, find the target and update its value
                            for(MeasuringComponentNew measuringComponentNew : esp32Receive.getMeasuringComponentNewList()) {

                                if(measuringComponentNew.getId() == webPostJSON.getId()) {

                                    measuringComponentNew.setValue(webPostJSON.getValue());

                                    break;

                                }

                            }

                            break;

                        }

                    }

                    break;

                }

            }

            return null;

        } else if(listOption == ListOption.JSON_TO_WEBPAGE) {

            String jsonSend = null;
            try {
                // Just get the reports
                List<RaspberryPiReport> raspberryPiReportList = connectedRaspberryPiList.stream().map(raspberryPiReportWrapper -> raspberryPiReportWrapper.getRaspberryPiReport()).toList();
                jsonSend = objectMapperForSendingJSON.writeValueAsString(raspberryPiReportList);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.err.println("Error in object to JSON conversion!");
            }

            return jsonSend;

        }

        return null;

    }

}
