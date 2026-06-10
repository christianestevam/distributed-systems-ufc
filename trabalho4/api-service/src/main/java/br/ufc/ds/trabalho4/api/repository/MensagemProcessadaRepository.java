package br.ufc.ds.trabalho4.api.repository;

import br.ufc.ds.trabalho4.api.domain.MensagemProcessada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemProcessadaRepository extends JpaRepository<MensagemProcessada, Long> {
    Page<MensagemProcessada> findByTickerOrderByProcessedAtDesc(String ticker, Pageable pageable);

    Page<MensagemProcessada> findByTickerContainingIgnoreCaseOrderByProcessedAtDesc(String ticker, Pageable pageable);

    Page<MensagemProcessada> findAllByOrderByProcessedAtDesc(Pageable pageable);

    long countByStatus(String status);
}
