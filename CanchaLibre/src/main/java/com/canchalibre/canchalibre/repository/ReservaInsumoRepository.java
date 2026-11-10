package com.canchalibre.canchalibre.repository;

import com.canchalibre.canchalibre.domain.EstadoReserva;
import com.canchalibre.canchalibre.domain.ReservaInsumo;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaInsumoRepository extends JpaRepository<ReservaInsumo, Long> {

    /** Unidades de un insumo ya comprometidas en el mismo dia y hora (null si no hay ninguna). */
    @Query("select sum(ri.cantidad) from ReservaInsumo ri where ri.insumo.id = :insumoId "
            + "and ri.reserva.fecha = :fecha and ri.reserva.horaInicio = :hora and ri.reserva.estado in :estados")
    Long cantidadReservada(@Param("insumoId") Long insumoId,
                           @Param("fecha") LocalDate fecha,
                           @Param("hora") LocalTime hora,
                           @Param("estados") Collection<EstadoReserva> estados);
}
