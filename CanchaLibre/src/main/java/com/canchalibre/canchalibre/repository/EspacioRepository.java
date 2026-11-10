package com.canchalibre.canchalibre.repository;

import com.canchalibre.canchalibre.domain.Espacio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspacioRepository extends JpaRepository<Espacio, Long> {

    List<Espacio> findByComplejoIdAndActivoTrueOrderByNombreAsc(Long complejoId);

    List<Espacio> findByComplejoIdOrderByNombreAsc(Long complejoId);
}
