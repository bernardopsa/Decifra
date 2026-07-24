package com.decifra.api.controller;

import com.decifra.api.model.Extrato;
import com.decifra.api.repository.ExtratoRepository;
import com.decifra.api.repository.TransacaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extratos")
@CrossOrigin(origins = "http://localhost:3000")
public class ExtratoController {

    private final ExtratoRepository extratoRepository;
    private final TransacaoRepository transacaoRepository;

    public ExtratoController(ExtratoRepository extratoRepository, TransacaoRepository transacaoRepository) {
        this.extratoRepository = extratoRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Extrato>> listarTodos() {
        return ResponseEntity.ok(extratoRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Extrato> buscarPorId(@PathVariable Long id) {
        return extratoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Extrato> atualizarExtrato(@PathVariable Long id, @RequestBody Extrato extratoAtualizado) {
        return extratoRepository.findById(id)
                .map(extrato -> {
                    extrato.setNomePersonalizado(extratoAtualizado.getNomePersonalizado());
                    extrato.setFixado(extratoAtualizado.getFixado());
                    return ResponseEntity.ok(extratoRepository.save(extrato));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarExtratoETransacoes(@PathVariable Long id) {
        if (extratoRepository.existsById(id)) {
            // Apaga todas as transações vinculadas primeiro (Integridade Relacional)
            transacaoRepository.deleteByExtratoId(id);
            // Depois apaga o registro do PDF
            extratoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}