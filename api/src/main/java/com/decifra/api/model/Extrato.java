package com.decifra.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Extrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomeOriginal;
    private String nomePersonalizado;
    private LocalDateTime dataUpload;
    private Boolean fixado;
    private LocalDate dataInicioTransacoes;
    private LocalDate dataFimTransacoes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeOriginal() { return nomeOriginal; }
    public void setNomeOriginal(String nomeOriginal) { this.nomeOriginal = nomeOriginal; }

    public String getNomePersonalizado() { return nomePersonalizado; }
    public void setNomePersonalizado(String nomePersonalizado) { this.nomePersonalizado = nomePersonalizado; }

    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }

    public Boolean getFixado() { return fixado; }
    public void setFixado(Boolean fixado) { this.fixado = fixado; }

    public LocalDate getDataInicioTransacoes() { return dataInicioTransacoes; }
    public void setDataInicioTransacoes(LocalDate dataInicioTransacoes) { this.dataInicioTransacoes = dataInicioTransacoes; }

    public LocalDate getDataFimTransacoes() { return dataFimTransacoes; }
    public void setDataFimTransacoes(LocalDate dataFimTransacoes) { this.dataFimTransacoes = dataFimTransacoes; }
}