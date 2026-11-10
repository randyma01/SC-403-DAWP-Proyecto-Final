package com.canchalibre.canchalibre.repository;

import com.canchalibre.canchalibre.domain.EstadoReserva;
import com.canchalibre.canchalibre.domain.Reserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    Optional<Reserva> findByCodigo(String codigo);

    List<Reserva> findByUsuarioIdOrderByFechaDescHoraInicioDesc(Long usuarioId);

    List<Reserva> findByEstadoAndExpiraEnBefore(EstadoReserva estado, LocalDateTime limite);

    List<Reserva> findByEstadoAndFechaLessThanEqual(EstadoReserva estado, LocalDate fecha);

    long countByEspacioComplejoIdAndEstado(Long complejoId, EstadoReserva estado);

    long countByEspacioComplejoIdAndFechaAndEstadoIn(Long complejoId, LocalDate fecha, Collection<EstadoReserva> estados);

    long countByEspacioComplejoIdAndEstadoIn(Long complejoId, Collection<EstadoReserva> estados);
}
