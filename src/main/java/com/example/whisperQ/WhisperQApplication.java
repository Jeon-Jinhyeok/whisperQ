package com.example.whisperQ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WhisperQApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhisperQApplication.class, args);
	}

}
