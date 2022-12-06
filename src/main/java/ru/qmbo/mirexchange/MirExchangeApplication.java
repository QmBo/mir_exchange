package ru.qmbo.mirexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MirExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MirExchangeApplication.class, args);
	}

}
