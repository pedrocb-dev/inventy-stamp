package com.inventystamp.model;

import java.math.BigDecimal;

public class EstatisticasMes {
    private int id;
    private int ano;
    private int mes;
    private BigDecimal lucroBruto;
    private BigDecimal lucroBordado;
    private BigDecimal lucroOutros;

    public EstatisticasMes() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public BigDecimal getLucroBruto() { return lucroBruto != null ? lucroBruto : BigDecimal.ZERO; }
    public void setLucroBruto(BigDecimal lucroBruto) { this.lucroBruto = lucroBruto; }

    public BigDecimal getLucroBordado() { return lucroBordado != null ? lucroBordado : BigDecimal.ZERO; }
    public void setLucroBordado(BigDecimal lucroBordado) { this.lucroBordado = lucroBordado; }

    public BigDecimal getLucroOutros() { return lucroOutros != null ? lucroOutros : BigDecimal.ZERO; }
    public void setLucroOutros(BigDecimal lucroOutros) { this.lucroOutros = lucroOutros; }
}