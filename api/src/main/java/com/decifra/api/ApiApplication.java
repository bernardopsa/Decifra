package com.decifra.api;

import com.decifra.api.service.AiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    // Este bloco vai rodar sozinho assim que a aplicação iniciar
    @Bean
    public CommandLineRunner run(AiService aiService) {
        return args -> {
            System.out.println("-----------------------------------");
            System.out.println("Iniciando Teste com a IA Local (Gemma)");
            
            String transacaoFeia = "PGTO*UBER EATS SAO PAULO";
            System.out.println("Enviando para a IA: " + transacaoFeia);
            
            String resultadoJson = aiService.decifrarTransacao(transacaoFeia);
            
            System.out.println("Resposta da IA: " + resultadoJson);
            System.out.println("-----------------------------------");
        };
    }
}