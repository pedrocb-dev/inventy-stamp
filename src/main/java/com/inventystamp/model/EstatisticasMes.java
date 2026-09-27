package com.inventystamp.model;

import java.math.BigDecimal;

public class EstatisticasMes {
    private int id;
    private int ano;
    private int mes;
    private BigDecimal lucroBruto;
    private BigDecimal lucroBordado;
    private BigDecimal lucroDtf;
    private BigDecimal lucroSublimacao;
    private BigDecimal lucroSilkscreen;
    private BigDecimal lucroBrindes;

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

    public BigDecimal getLucroDtf() { return lucroDtf != null ? lucroDtf : BigDecimal.ZERO; }
    public void setLucroDtf(BigDecimal lucroDtf) { this.lucroDtf = lucroDtf; }

    public BigDecimal getLucroSublimacao() { return lucroSublimacao != null ? lucroSublimacao : BigDecimal.ZERO; }
    public void setLucroSublimacao(BigDecimal lucroSublimacao) { this.lucroSublimacao = lucroSublimacao; }

    public BigDecimal getLucroSilkscreen() { return lucroSilkscreen != null ? lucroSilkscreen : BigDecimal.ZERO; }
    public void setLucroSilkscreen(BigDecimal lucroSilkscreen) { this.lucroSilkscreen = lucroSilkscreen; }

    public BigDecimal getLucroBrindes() { return lucroBrindes != null ? lucroBrindes : BigDecimal.ZERO; }
    public void setLucroBrindes(BigDecimal lucroBrindes) { this.lucroBrindes = lucroBrindes; }
}