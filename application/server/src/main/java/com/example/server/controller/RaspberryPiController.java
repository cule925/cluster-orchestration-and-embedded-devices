package com.example.server.controller;

import com.example.server.model.*;
import com.example.server.service.RaspberryPiReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server")
public class RaspberryPiController {

    private final RaspberryPiReportService raspberryPiReportService;

    @Autowired
    public RaspberryPiController(RaspberryPiReportService raspberryPiReportService) { this.raspberryPiReportService = raspberryPiReportService; }

    @PostMapping("")
    public String handleRaspberryPiReport(@RequestBody RaspberryPiReport raspberryPiReport) {

        return raspberryPiReportService.updateList(raspberryPiReport, RaspberryPiReportService.ListOption.JSON_FROM_AND_TO_RASPBERRY_PI, null, null);

    }

}
