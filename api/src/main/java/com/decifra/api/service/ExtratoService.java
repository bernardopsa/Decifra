package com.decifra.api.service;

import com.decifra.api.model.Extrato;
import com.decifra.api.model.Transacao;
import com.decifra.api.repository.ExtratoRepository;
import com.decifra.api.repository.TransacaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExtratoService {

    private final AiService aiService;
    private final TransacaoRepository transacaoRepository;
    private final ExtratoRepository extratoRepository;
    private final ObjectMapper objectMapper;

    public ExtratoService(AiService aiService, TransacaoRepository transacaoRepository, ExtratoRepository extratoRepository) {
        this.aiService = aiService;
        this.transacaoRepository = transacaoRepository;
        this.extratoRepository = extratoRepository;
        this.objectMapper = new ObjectMapper();
    }

    public void processarExtratoPdf(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // 1. Cria o registro base do Extrato no banco
            Extrato extrato = new Extrato();
            extrato.setNomeOriginal(file.getOriginalFilename());
            extrato.setDataUpload(LocalDateTime.now());
            extrato.setFixado(false);
            extrato = extratoRepository.save(extrato);

            String[] linhas = text.split("\\r?\\n");
            Pattern pattern = Pattern.compile("^(\\d{2}/\\d{2}/\\d{4})\\s+(.+)");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDate menorData = null;
            LocalDate maiorData = null;

            // 2. Loop de Processamento
            for (String linha : linhas) {
                Matcher matcher = pattern.matcher(linha.trim());

                if (matcher.find()) {
                    String dataStr = matcher.group(1);
                    String restoDaLinha = matcher.group(2).trim();

                    if (restoDaLinha.contains("SALDO DO DIA") || 
                        restoDaLinha.contains("REND PAGO") || 
                        restoDaLinha.contains("Aviso!")) {
                        continue;
                    }

                    String descricaoBruta = restoDaLinha;
                    Double valor = 0.0;

                    Pattern valorPattern = Pattern.compile("(-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})");
                    Matcher valorMatcher = valorPattern.matcher(restoDaLinha);
                    
                    String ultimoValorStr = null;
                    int indexDoValor = -1;

                    while (valorMatcher.find()) {
                        ultimoValorStr = valorMatcher.group(1);
                        indexDoValor = valorMatcher.start();
                    }

                    if (ultimoValorStr != null) {
                        descricaoBruta = restoDaLinha.substring(0, indexDoValor).trim();
                        try {
                            String valorFormatado = ultimoValorStr.replace(".", "").replace(",", ".");
                            valor = Double.parseDouble(valorFormatado);
                        } catch (NumberFormatException e) {
                            System.err.println("Erro ao formatar valor financeiro: " + ultimoValorStr);
                        }
                    }

                    LocalDate dataConvertida = LocalDate.parse(dataStr, formatter);

                    // Lógica para encontrar o período do extrato
                    if (menorData == null || dataConvertida.isBefore(menorData)) menorData = dataConvertida;
                    if (maiorData == null || dataConvertida.isAfter(maiorData)) maiorData = dataConvertida;

                    if (transacaoRepository.existsByDataAndNomeOriginalAndValor(dataConvertida, descricaoBruta, valor)) {
                        System.out.println("Transação já existente. Ignorando duplicata: " + descricaoBruta);
                        continue; 
                    }

                    String jsonResposta = aiService.decifrarTransacao(descricaoBruta);
                    
                    try {
                        JsonNode root = objectMapper.readTree(jsonResposta);
                        String nomeAmigavel = root.path("nomeAmigavel").asText("Desconhecido");
                        String categoria = root.path("categoria").asText("Indefinido");

                        Transacao transacao = new Transacao();
                        transacao.setData(dataConvertida);
                        transacao.setNomeOriginal(descricaoBruta);
                        transacao.setNomeAmigavel(nomeAmigavel);
                        transacao.setCategoria(categoria);
                        transacao.setValor(valor);
                        transacao.setExtratoId(extrato.getId()); // Vincula a transação ao PDF

                        transacaoRepository.save(transacao);

                    } catch (Exception e) {
                        System.err.println("Erro ao fazer parse do JSON da IA: " + jsonResposta);
                    }
                }
            }

            // 3. Atualiza o Extrato com as datas extremas encontradas
            if (menorData != null && maiorData != null) {
                extrato.setDataInicioTransacoes(menorData);
                extrato.setDataFimTransacoes(maiorData);
                extratoRepository.save(extrato);
            }

        } catch (Exception e) {
            throw new RuntimeException("Falha ao ler o PDF: " + e.getMessage());
        }
    }
}