package br.com.fiap.cp5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Cp5Application {

	public static void main(String[] args) {
		SpringApplication.run(Cp5Application.class, args);
	}

}
