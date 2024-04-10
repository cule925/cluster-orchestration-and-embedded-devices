package com.example.clientserver.controller;

import com.example.clientserver.model.ESP32Report;
import com.example.clientserver.service.ESP32ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/raspberrypi")
public class ESP32Controller {

    private final ESP32ReportService esp32ReportService;

    @Autowired
    public ESP32Controller(ESP32ReportService esp32ReportService) {
        this.esp32ReportService = esp32ReportService;
    }

    @PostMapping("")
    public String handleESP32Report(@RequestBody ESP32Report esp32Report) {

        return esp32ReportService.updateList(esp32Report, ESP32ReportService.ListOption.JSON_FROM_AND_TO_ESP32, null, null, null, null);

    }

}
