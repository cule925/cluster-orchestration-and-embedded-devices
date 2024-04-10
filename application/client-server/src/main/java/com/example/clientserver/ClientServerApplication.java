package com.example.clientserver;

import com.example.clientserver.service.RaspberryPiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClientServerApplication implements CommandLineRunner {

	@Autowired
	private RaspberryPiService raspberryPiService;

	public static void main(String[] args) {

		SpringApplication.run(ClientServerApplication.class, args);

    }

	@Override
	public void run(String[] args) {

		// Check if argument size is valid
		if(args.length != 4) {
			System.err.println("Wrong argument number, arguments needed: <client-server node name> <ip address of server> <port of server> <reporting period>");
			return;
		}

		// Extract arguments
		String name = args[0];
		String ipAddress = args[1];
		String port = args[2];
		String reportingPeriodString = args[3];

		try {

			int reportingPeriod = Integer.parseInt(reportingPeriodString);

			// Set reporting period
			raspberryPiService.setReportingPeriod(reportingPeriod);

		} catch(NumberFormatException e) {
			e.printStackTrace();
			System.out.println("Setting default format");
			return;
		}

		// Construct Raspberry Pi state class
		if(!raspberryPiService.constructRaspberryPiReport(name)) {
			System.err.println("Error in creating Raspberry Pi state class!");
			return;
		}

		// Construct URL
		if(!raspberryPiService.constructURI(ipAddress, port)) {
			System.err.println("Error in creating URL!");
			return;
		}

		// Initialize JSON mappers
		if(!raspberryPiService.initializeJSONMappers()) {
			System.err.println("Error in initializing JSON mappers!");
			return;
		}

		raspberryPiService.raspberryPiStartReporting();

	}

}
