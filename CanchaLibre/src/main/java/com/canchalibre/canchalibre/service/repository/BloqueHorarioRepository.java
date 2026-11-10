package com.canchalibre.canchalibre.repository;

import com.canchalibre.canchalibre.domain.BloqueHorario;
import com.canchalibre.canchalibre.domain.EstadoBloque;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloqueHorarioRepository extends JpaRepository<BloqueHorario, Long> {

    List<BloqueHorario> findByEspacioIdInAndFecha(Collection<Long> espacioIds, LocalDate fecha);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BloqueHorario b where b.espacio.id = :espacioId and b.fecha = :fecha and b.horaInicio = :hora")
    Optional<BloqueHorario> findParaActualizar(@Param("espacioId") Long espacioId,
                                               @Param("fecha") LocalDate fecha,
                                               @Param("hora") LocalTime hora);

    List<BloqueHorario> findByEstadoAndExpiraEnBefore(EstadoBloque estado, LocalDateTime limite);

    /** Bloqueos operativos (mantenimiento) futuros de un complejo, para el panel administrativo. */
    @Query("select b from BloqueHorario b where b.espacio.complejo.id = :complejoId and b.estado = :estado "
            + "and b.fecha >= :desde order by b.fecha, b.espacio.nombre, b.horaInicio")
    List<BloqueHorario> findDelComplejo(@Param("complejoId") Long complejoId,
                                        @Param("estado") EstadoBloque estado,
                                        @Param("desde") LocalDate desde);
}
