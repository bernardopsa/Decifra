package com.decifra.api.controller;

import com.decifra.api.model.Transacao;
import com.decifra.api.repository.TransacaoRepository;
import com.decifra.api.service.AiService;
import com.decifra.api.service.ExtratoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transacoes")
@CrossOrigin(origins = "http://localhost:3000") // Permite que o Next.js acesse a API
public class TransacaoController {

    private final TransacaoRepository repository;
    private final AiService aiService;
    private final ExtratoService extratoService;

    // Construtor único para injeção automática das dependências
    public TransacaoController(TransacaoRepository repository, AiService aiService, ExtratoService extratoService) {
        this.repository = repository;
        this.aiService = aiService;
        this.extratoService = extratoService;
    }

    // 1. Lista todo o histórico gravado no banco
    @GetMapping
    public ResponseEntity<List<Transacao>> listarTodas() {
        return ResponseEntity.ok(repository.findAll());
    }

    // 2. Processa uma única string manual (o card de input atual)
    @PostMapping("/decifrar")
    public ResponseEntity<Transacao> decifrarManual(@RequestBody String nomeOriginal) {
        // Chama a IA (Gemma) para processar a string
        String jsonResposta = aiService.decifrarTransacao(nomeOriginal);
        
        // Aqui você pode usar uma biblioteca como Jackson para converter o JSON 
        // ou criar um parser simples para popular o objeto Transacao
        // Exemplo simplificado de salvamento:
        Transacao t = new Transacao();
        t.setNomeOriginal(nomeOriginal);
        t.setData(LocalDate.now());
        return ResponseEntity.ok(repository.save(t));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploadExtrato(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Arquivo não enviado ou vazio."));
        }

        try {
            extratoService.processarExtratoPdf(file);
            
            return ResponseEntity.ok(Map.of("mensagem", "Extrato processado e enviado para análise da IA!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao processar o arquivo: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transacao> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/extrato/{extratoId}")
    public ResponseEntity<List<Transacao>> listarPorExtrato(@PathVariable Long extratoId) {
        return ResponseEntity.ok(repository.findByExtratoId(extratoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transacao> atualizarTransacao(@PathVariable Long id, @RequestBody Transacao transacaoAtualizada) {
        return repository.findById(id)
                .map(transacaoExistente -> {
                    // Atualiza apenas os campos permitidos para edição
                    transacaoExistente.setNomeAmigavel(transacaoAtualizada.getNomeAmigavel());
                    transacaoExistente.setCategoria(transacaoAtualizada.getCategoria());
                    transacaoExistente.setDescricao(transacaoAtualizada.getDescricao());
                    transacaoExistente.setTagComportamental(transacaoAtualizada.getTagComportamental());
                    
                    return ResponseEntity.ok(repository.save(transacaoExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}