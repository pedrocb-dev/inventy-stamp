package com.inventystamp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Demanda {
    private int id;
    private String descricao;
    private Perfil solicitante;
    private Perfil destinatario;
    private boolean concluida;
    private LocalDateTime criadoEm;
    private LocalDate prazoData;
    private String prazoHora;
    private int numeroDia;

    public Demanda() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Perfil getSolicitante() { return solicitante; }
    public void setSolicitante(Perfil solicitante) { this.solicitante = solicitante; }

    public Perfil getDestinatario() { return destinatario; }
    public void setDestinatario(Perfil destinatario) { this.destinatario = destinatario; }

    public boolean isConcluida() { return concluida; }
    public void setConcluida(boolean concluida) { this.concluida = concluida; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDate getPrazoData() { return prazoData; }
    public void setPrazoData(LocalDate prazoData) { this.prazoData = prazoData; }

    public String getPrazoHora() { return prazoHora; }
    public void setPrazoHora(String prazoHora) { this.prazoHora = prazoHora; }

    public int getNumeroDia() { return numeroDia; }
    public void setNumeroDia(int numeroDia) { this.numeroDia = numeroDia; }
}