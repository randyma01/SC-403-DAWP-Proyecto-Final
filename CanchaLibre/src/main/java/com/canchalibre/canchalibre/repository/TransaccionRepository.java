package com.canchalibre.canchalibre.repository;

import com.canchalibre.canchalibre.domain.Transaccion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByReservaIdOrderByCreadoEnAsc(Long reservaId);
}
