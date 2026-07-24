package com.decifra.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate data;
    private String nomeOriginal;
    private String nomeAmigavel;
    private String categoria;
    private Double valor;
    
    private String descricao;
    private String tagComportamental;

    // Vincula a transação ao PDF que a originou
    private Long extratoId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getNomeOriginal() { return nomeOriginal; }
    public void setNomeOriginal(String nomeOriginal) { this.nomeOriginal = nomeOriginal; }

    public String getNomeAmigavel() { return nomeAmigavel; }
    public void setNomeAmigavel(String nomeAmigavel) { this.nomeAmigavel = nomeAmigavel; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getTagComportamental() { return tagComportamental; }
    public void setTagComportamental(String tagComportamental) { this.tagComportamental = tagComportamental; }

    public Long getExtratoId() { return extratoId; }
    public void setExtratoId(Long extratoId) { this.extratoId = extratoId; }
}