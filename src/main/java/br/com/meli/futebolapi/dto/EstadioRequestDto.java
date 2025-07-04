package br.com.meli.futebolapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EstadioRequestDto {
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, message = "O nome deve ter pelo menos 3 letras")
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
