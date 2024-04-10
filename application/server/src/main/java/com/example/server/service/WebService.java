package com.example.server.service;

import com.example.server.model.WebPostJSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebService {

    private final RaspberryPiReportService raspberryPiReportService;

    // Object mappers
    private ObjectMapper objectMapperForSendingJSON;

    private int requestPeriod = 5;

    @Autowired
    public WebService(RaspberryPiReportService raspberryPiReportService) {

        this.raspberryPiReportService = raspberryPiReportService;

    }

    // Initialize the JSON mappers
    public boolean initializeJSONMappers() {

        // Convert Object to JSON
        objectMapperForSendingJSON = new ObjectMapper();

        // Allow list to be represented as a nested JSON
        objectMapperForSendingJSON.enable(SerializationFeature.INDENT_OUTPUT);

        return true;

    }

    // Update new actuator states received from server
    public void updateRaspberryPiValuesAndStates(WebPostJSON webPostJSON) {

        raspberryPiReportService.updateList(null, RaspberryPiReportService.ListOption.JSON_FROM_WEBPAGE, webPostJSON, null);

    }

    // Page refresh data
    public String generateJSONBody() {

        return raspberryPiReportService.updateList(null, RaspberryPiReportService.ListOption.JSON_TO_WEBPAGE, null, objectMapperForSendingJSON);

    }

    public int getRequestPeriod() {
        return requestPeriod;
    }

    public void setRequestPeriod(int requestPeriod) {
        this.requestPeriod = requestPeriod;
    }

}
