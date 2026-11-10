package com.canchalibre.canchalibre.dto;

import java.time.LocalTime;
import java.util.List;

public class FilaDisp {

    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final List<CeldaDisp> celdas;

    // ---- generado: constructor y getters ----
    public FilaDisp(LocalTime horaInicio, LocalTime horaFin, List<CeldaDisp> celdas) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.celdas = celdas;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public List<CeldaDisp> getCeldas() {
        return celdas;
    }

}
