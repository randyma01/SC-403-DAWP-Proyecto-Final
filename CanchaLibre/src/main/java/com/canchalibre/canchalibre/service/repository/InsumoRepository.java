package com.canchalibre.canchalibre.repository;

import com.canchalibre.canchalibre.domain.Insumo;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    List<Insumo> findByComplejoIdAndActivoTrueOrderByCategoriaAscNombreAsc(Long complejoId);

    List<Insumo> findByComplejoIdOrderByCategoriaAscNombreAsc(Long complejoId);

    /** Bloquea la fila para serializar la validacion de inventario entre usuarios simultaneos. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Insumo i where i.id = :id")
    Optional<Insumo> findByIdParaActualizar(@Param("id") Long id);
}
