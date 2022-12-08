package ru.qmbo.mirexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MirExchangeApplication
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 08.12.2022
 */
@SpringBootApplication
@EnableScheduling
public class MirExchangeApplication {

	/**
	 * The entry point of application.
	 *
	 * @param args the input arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(MirExchangeApplication.class, args);
	}

}
