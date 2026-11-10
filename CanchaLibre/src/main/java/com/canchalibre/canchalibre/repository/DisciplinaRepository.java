package com.canchalibre.canchalibre.repository;

import com.canchalibre.canchalibre.domain.Disciplina;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Integer> {

    List<Disciplina> findAllByOrderByOrdenAsc();
}
