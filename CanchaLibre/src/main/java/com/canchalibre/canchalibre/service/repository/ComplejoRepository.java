package com.canchalibre.canchalibre.repository;

import com.canchalibre.canchalibre.domain.Complejo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ComplejoRepository extends JpaRepository<Complejo, Long>, JpaSpecificationExecutor<Complejo> {

    @Query("select distinct c.provincia from Complejo c where c.activo = true order by c.provincia")
    List<String> provinciasActivas();
}
