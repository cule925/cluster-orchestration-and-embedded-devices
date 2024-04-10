package com.example.server;

import com.example.server.service.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServerApplication implements CommandLineRunner {

	@Autowired
	private WebService webService;

	public static void main(String[] args) {

		SpringApplication.run(ServerApplication.class, args);

	}

	@Override
	public void run(String[] args) {

		// Check if argument size is valid
		if(args.length != 1) {
			System.err.println("Wrong argument number, argument needed: <request period>");
			return;
		}

		// Extract argument
		String reportingPeriodString = args[0];

		try {

			int requestPeriod = Integer.parseInt(reportingPeriodString);

			// Set request period
			webService.setRequestPeriod(requestPeriod);

		} catch(NumberFormatException e) {
			e.printStackTrace();
			System.out.println("Setting default format");
			return;
		}

		// Initialize JSON mappers
		if(!webService.initializeJSONMappers()) {
			System.err.println("Error in initializing JSON mappers!");
			return;
		}

	}

}
