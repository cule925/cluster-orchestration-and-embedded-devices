package com.example.server.controller;

import com.example.server.model.WebPostJSON;
import com.example.server.service.WebService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("")
public class WebController {

    private final WebService webService;

    @Autowired
    public WebController(WebService webService) { this.webService = webService; }

    @GetMapping("")
    public ModelAndView serveEmptyPage() {

        ModelAndView modelAndView = new ModelAndView("html/homepage");
        modelAndView.addObject("requestPeriod", webService.getRequestPeriod() * 1000);
        return modelAndView;

    }

    @PostMapping("/post")
    public ResponseEntity<String> postData(@RequestBody WebPostJSON webPostJSON) {

        webService.updateRaspberryPiValuesAndStates(webPostJSON);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping("/refresh")
    public String refreshPage() {

        return webService.generateJSONBody();

    }

}
