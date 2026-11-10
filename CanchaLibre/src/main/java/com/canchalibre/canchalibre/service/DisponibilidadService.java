package com.canchalibre.canchalibre.service;

import com.canchalibre.canchalibre.domain.BloqueHorario;
import com.canchalibre.canchalibre.domain.Complejo;
import com.canchalibre.canchalibre.domain.Espacio;
import com.canchalibre.canchalibre.dto.CeldaDisp;
import com.canchalibre.canchalibre.dto.DiaCalendario;
import com.canchalibre.canchalibre.dto.FilaDisp;
import com.canchalibre.canchalibre.repository.BloqueHorarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calendario y cuadricula de bloques horarios (HU-04). */
@Service
@Transactional(readOnly = true)
public class DisponibilidadService {

    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String OCUPADO = "OCUPADO";
    public static final String APARTADO = "APARTADO";
    public static final String MANTENIMIENTO = "MANTENIMIENTO";
    public static final String PASADO = "PASADO";

    private final BloqueHorarioRepository bloqueRepo;

    public DisponibilidadService(BloqueHorarioRepository bloqueRepo) {
        this.bloqueRepo = bloqueRepo;
    }

    /** Una fila por hora de apertura del complejo y una columna por espacio. */
    public List<FilaDisp> cuadricula(Complejo complejo, List<Espacio> espacios, LocalDate fecha) {
        LocalDateTime ahora = LocalDateTime.now();
        Map<String, BloqueHorario> bloques = new HashMap<>();
        if (!espacios.isEmpty()) {
            List<Long> ids = espacios.stream().map(Espacio::getId).toList();
            for (BloqueHorario b : bloqueRepo.findByEspacioIdInAndFecha(ids, fecha)) {
                bloques.put(b.getEspacio().getId() + "|" + b.getHoraInicio(), b);
            }
        }
        int desde = complejo.getHoraApertura().toSecondOfDay() / 3600;
        int hasta = complejo.getHoraCierre().toSecondOfDay() / 3600;
        List<FilaDisp> filas = new ArrayList<>();
        for (int h = desde; h < hasta; h++) {
            LocalTime inicio = LocalTime.of(h, 0);
            List<CeldaDisp> celdas = new ArrayList<>();
            for (Espacio e : espacios) {
                celdas.add(celda(e, fecha, inicio, bloques.get(e.getId() + "|" + inicio), ahora));
            }
            filas.add(new FilaDisp(inicio, inicio.plusHours(1), celdas));
        }
        return filas;
    }

    private CeldaDisp celda(Espacio e, LocalDate fecha, LocalTime inicio, BloqueHorario b, LocalDateTime ahora) {
        if (fecha.atTime(inicio).isBefore(ahora) || fecha.atTime(inicio).equals(ahora)) {
            return new CeldaDisp(e.getId(), PASADO, false);
        }
        if (b == null || b.estaLibre(ahora)) {
            return new CeldaDisp(e.getId(), DISPONIBLE, true);
        }
        return switch (b.getEstado()) {
            case APARTADO_TEMPORAL -> new CeldaDisp(e.getId(), APARTADO, false);
            case MANTENIMIENTO -> new CeldaDisp(e.getId(), MANTENIMIENTO, false);
            default -> new CeldaDisp(e.getId(), OCUPADO, false);
        };
    }

    /** true si el bloque esta libre y no es pasado (verificacion previa a elegir insumos). */
    public boolean estaDisponible(Espacio espacio, LocalDate fecha, LocalTime hora) {
        LocalDateTime ahora = LocalDateTime.now();
        if (!fecha.atTime(hora).isAfter(ahora)) {
            return false;
        }
        return bloqueRepo.findByEspacioIdInAndFecha(List.of(espacio.getId()), fecha).stream()
                .filter(b -> b.getHoraInicio().equals(hora))
                .allMatch(b -> b.estaLibre(ahora));
    }

    /** Mini calendario mensual (semana de lunes a domingo). Los dias anteriores a hoy quedan deshabilitados. */
    public List<DiaCalendario> calendario(YearMonth mes, LocalDate seleccionada) {
        LocalDate hoy = LocalDate.now();
        List<DiaCalendario> dias = new ArrayList<>();
        int relleno = mes.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < relleno; i++) {
            dias.add(new DiaCalendario(null, 0, true, false, false));
        }
        for (int d = 1; d <= mes.lengthOfMonth(); d++) {
            LocalDate f = mes.atDay(d);
            dias.add(new DiaCalendario(f, d, f.isBefore(hoy), f.equals(seleccionada), f.equals(hoy)));
        }
        return dias;
    }
}
