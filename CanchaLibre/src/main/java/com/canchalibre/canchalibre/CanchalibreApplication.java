package com.canchalibre.canchalibre;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CanchalibreApplication {

	public static void main(String[] args) {
		// Costa Rica (UTC-6, sin horario de verano): las reservas, bloques y temporizadores usan esta zona
		TimeZone.setDefault(TimeZone.getTimeZone("America/Costa_Rica"));
		SpringApplication.run(CanchalibreApplication.class, args);
	}

}
