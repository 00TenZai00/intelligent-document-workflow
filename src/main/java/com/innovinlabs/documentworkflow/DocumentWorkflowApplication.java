package com.innovinlabs.documentworkflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DocumentWorkflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentWorkflowApplication.class, args);
	}

}
