package com.decifra.api.repository;

import com.decifra.api.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    
    boolean existsByDataAndNomeOriginalAndValor(LocalDate data, String nomeOriginal, Double valor);

    List<Transacao> findByExtratoId(Long extratoId);

    @Transactional
    void deleteByExtratoId(Long extratoId);
}