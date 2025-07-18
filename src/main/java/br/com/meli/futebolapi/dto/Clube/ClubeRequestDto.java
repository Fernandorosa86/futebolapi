package br.com.meli.futebolapi.dto.Clube;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class ClubeRequestDto {

    @NotBlank(message = "O campo Nome é obrigatório")
    @Size(min=2, message = "O Campo Nome deve ter pelo menos 2 caracteres")
    private String nome;

    @NotBlank(message = "O campo Estado é obrigatório")
    @Pattern(regexp = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO",
            message = "Estado deve ser uma sigla válida" )
    private String estado;

    @NotNull(message = "A data de criação é obrigatória")
    @PastOrPresent(message = "A data de criação não pode ser no futuro")
    private LocalDate dataCriacao;

    @NotNull(message = "O status é obrigatório")
    private Boolean status;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

}

