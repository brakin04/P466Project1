package edu.iu.p466.great_speeches;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GreatSpeechApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreatSpeechApplication.class, args);
	}

}

/*
docker build -t great_speeches .
docker run -p 8080:8080 great_speeches
*/
