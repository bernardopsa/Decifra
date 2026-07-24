package com.decifra.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {

    private final String OLLAMA_URL = "http://ollama:11434/api/generate";

    public String decifrarTransacao(String nomeOriginal) {
        
    String prompt = """
                Você é um extrator de dados JSON estrito. Sua única função é ler a string bruta, extrair o nome real do estabelecimento e classificar.

                CATEGORIAS OBRIGATÓRIAS (Escolha estritamente uma destas):
                [Alimentação, Transporte, Lazer, Saúde, Educação, Transferência Direta]

                REGRAS DE EXTRAÇÃO E LIMPEZA:
                1. O nome real do estabelecimento geralmente vem LOGO APÓS termos genéricos como "PAG*", "PGTO*", "MP*" ou "COMPRA".
                2. NUNCA use a palavra "Pagamento" ou "Pagamento do" como nome amigável.
                3. IGNORE sufixos de localização ou empresariais (ex: LGO DO MACHADO, LAGOA, LTDA, SAO PAULO).
                4. Responda EXATAMENTE com um JSON válido. Nenhuma palavra a mais.
                5. A categoria 'Transferência Direta' existe para aquelas que são feitas diretas para um CPF, isto é, uma pessoa, e não para um estabelecimento.
                6. Se o nome contiver números (ex: 99FOOD) ou você não tiver certeza, NÃO devolva N/A. Tente extrair, mesmo assim, tentando achar o nome do estabelecimento.

                EXEMPLOS DE TREINAMENTO (Siga este padrão lógico):
                Input: 'PAG*METRORIO_PASSAGEM'
                Output: {"nomeAmigavel": "Metrô Rio", "categoria": "Transporte"}

                Input: 'PAG*PARME LGO DO MACHADO'
                Output: {"nomeAmigavel": "Parmê", "categoria": "Alimentação"}

                Input: 'PAG*UBER_LTDA'
                Output: {"nomeAmigavel": "Uber", "categoria": "Transporte"}
                
                Input: 'CAZA LAGOA'
                Output: {"nomeAmigavel": "Caza Lagoa", "categoria": "Alimentação"}

                Sua tarefa atual:
                Input: '%s'
                Output:
                """.formatted(nomeOriginal);


        // 2. Configurando o corpo da requisição
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gemma:2b");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("format", "json");

        // 3. O SEGREDO: Zerando a Temperatura
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.0);
        requestBody.put("options", options);

        // 4. Cabeçalhos e Execução
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                OLLAMA_URL,
                org.springframework.http.HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("response")) {
                return (String) responseBody.get("response");
            }
        } catch (Exception e) {
            System.err.println("Erro ao comunicar com a IA local: " + e.getMessage());
        }

        return "{\"nomeAmigavel\": \"Desconhecido\", \"categoria\": \"Não Classificado\"}";
    }
}