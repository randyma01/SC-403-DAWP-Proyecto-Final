package com.canchalibre.canchalibre.service;

import com.canchalibre.canchalibre.domain.Complejo;
import com.canchalibre.canchalibre.domain.EstadoReserva;
import com.canchalibre.canchalibre.domain.Espacio;
import com.canchalibre.canchalibre.domain.Insumo;
import com.canchalibre.canchalibre.dto.Cotizacion;
import com.canchalibre.canchalibre.dto.InsumoVista;
import com.canchalibre.canchalibre.dto.LineaCotizacion;
import com.canchalibre.canchalibre.repository.InsumoRepository;
import com.canchalibre.canchalibre.repository.ReservaInsumoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tarifas, insumos disponibles y cotizacion (HU-05, HU-06, HU-07). */
@Service
public class CotizacionService {

    private final InsumoRepository insumoRepo;
    private final ReservaInsumoRepository reservaInsumoRepo;

    public CotizacionService(InsumoRepository insumoRepo, ReservaInsumoRepository reservaInsumoRepo) {
        this.insumoRepo = insumoRepo;
        this.reservaInsumoRepo = reservaInsumoRepo;
    }

    /** Insumos activos del complejo que aplican a la disciplina del espacio, con su disponibilidad para fecha/hora. */
    @Transactional(readOnly = true)
    public List<InsumoVista> insumosDisponibles(Espacio espacio, LocalDate fecha, LocalTime hora) {
        List<InsumoVista> vistas = new ArrayList<>();
        for (Insumo i : insumoRepo.findByComplejoIdAndActivoTrueOrderByCategoriaAscNombreAsc(espacio.getComplejo().getId())) {
            if (i.aplicaA(espacio.getDisciplina())) {
                vistas.add(new InsumoVista(i, disponibles(i, fecha, hora)));
            }
        }
        return vistas;
    }

    /** Unidades libres de un insumo en esa fecha/hora; null = ilimitado. */
    private Integer disponibles(Insumo insumo, LocalDate fecha, LocalTime hora) {
        if (insumo.getStock() == null) {
            return null;
        }
        Long usadas = reservaInsumoRepo.cantidadReservada(insumo.getId(), fecha, hora, EstadoReserva.ACTIVAS);
        return Math.max(0, insumo.getStock() - (usadas == null ? 0 : usadas.intValue()));
    }

    /**
     * Valida las cantidades pedidas (HU-06: no superar el inventario) y arma las lineas de cotizacion.
     *
     * @param bloquear true al crear la reserva: bloquea la fila del insumo para evitar sobreventa concurrente
     */
    @Transactional
    public List<LineaCotizacion> resolverLineas(Espacio espacio, LocalDate fecha, LocalTime hora,
                                                Map<Long, Integer> cantidades, boolean bloquear) {
        List<LineaCotizacion> lineas = new ArrayList<>();
        if (cantidades == null) {
            return lineas;
        }
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            int cantidad = entrada.getValue() == null ? 0 : entrada.getValue();
            if (cantidad <= 0) {
                continue;
            }
            Insumo insumo = (bloquear ? insumoRepo.findByIdParaActualizar(entrada.getKey()) : insumoRepo.findById(entrada.getKey()))
                    .orElseThrow(() -> new NegocioException("insumo.error.noexiste"));
            Complejo c = espacio.getComplejo();
            if (!insumo.isActivo() || !insumo.getComplejo().getId().equals(c.getId()) || !insumo.aplicaA(espacio.getDisciplina())) {
                throw new NegocioException("insumo.error.noaplica", insumo.getNombre());
            }
            Integer libres = disponibles(insumo, fecha, hora);
            if (libres != null && cantidad > libres) {
                throw new NegocioException("insumo.error.stock", insumo.getNombre(), libres);
            }
            lineas.add(new LineaCotizacion(insumo, cantidad, insumo.getPrecio(),
                    PrecioCalculator.subtotalLinea(insumo.getPrecio(), cantidad)));
        }
        return lineas;
    }

    /** Cotizacion completa: pista (diurna o nocturna) + insumos, con IVA desglosado dentro del total. */
    public Cotizacion cotizar(Espacio espacio, LocalTime hora, List<LineaCotizacion> lineas) {
        Complejo c = espacio.getComplejo();
        boolean nocturno = PrecioCalculator.esNocturno(hora, c.getHoraInicioNocturno());
        BigDecimal recargo = nocturno ? espacio.getRecargoNocturno() : BigDecimal.ZERO;
        BigDecimal pista = PrecioCalculator.subtotalPista(espacio.getTarifaDiurna(), espacio.getRecargoNocturno(), nocturno);
        BigDecimal insumos = BigDecimal.ZERO;
        for (LineaCotizacion l : lineas) {
            insumos = insumos.add(l.getSubtotal());
        }
        BigDecimal total = pista.add(insumos);
        BigDecimal iva = PrecioCalculator.ivaIncluido(total, c.getPorcentajeIva());
        return new Cotizacion(nocturno, espacio.getTarifaDiurna(), recargo, pista, lineas, insumos,
                c.getPorcentajeIva(), iva, total, espacio.getDepositoGarantia());
    }
}
