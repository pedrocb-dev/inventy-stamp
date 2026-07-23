package com.inventystamp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Gasto {
    private int id;
    private int estatisticaId;
    private String categoria;
    private BigDecimal valor;
    private LocalDateTime criadoEm;

    public Gasto() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEstatisticaId() { return estatisticaId; }
    public void setEstatisticaId(int estatisticaId) { this.estatisticaId = estatisticaId; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}