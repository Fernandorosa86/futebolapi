package br.com.meli.futebolapi.model;

import jakarta.persistence.*;

@Entity
public class Estadio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    public String getNome(String nome) {
        return nome;
    }

    public void setName(String nome) {
        this.nome = nome;
    }
}
