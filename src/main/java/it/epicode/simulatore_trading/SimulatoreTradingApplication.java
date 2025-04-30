package it.epicode.simulatore_trading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // Questa notation serve per abilitare la schedulazione (aggiornamento automatico)
public class SimulatoreTradingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimulatoreTradingApplication.class, args);
	}
}
