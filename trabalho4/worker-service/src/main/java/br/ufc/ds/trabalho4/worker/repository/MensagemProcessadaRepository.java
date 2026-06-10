package br.ufc.ds.trabalho4.worker.repository;

import br.ufc.ds.trabalho4.worker.domain.MensagemProcessada;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemProcessadaRepository extends JpaRepository<MensagemProcessada, Long> {
}