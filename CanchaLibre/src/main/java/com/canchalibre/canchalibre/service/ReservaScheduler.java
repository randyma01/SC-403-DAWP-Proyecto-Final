package com.canchalibre.canchalibre.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Libera automaticamente las reservas no pagadas a tiempo (HU-10). */
@Component
public class ReservaScheduler {

    private final ReservaService reservaService;

    public ReservaScheduler(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Scheduled(fixedDelayString = "${app.reserva.intervalo-expiracion-ms:30000}", initialDelayString = "${app.reserva.intervalo-expiracion-ms:30000}")
    public void liberarVencidas() {
        reservaService.procesarVencidas();
    }
}
