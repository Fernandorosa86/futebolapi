package br.com.meli.futebolapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class PartidaRequestDto {

    @NotNull private Long clubeCasaId;
    @NotNull private Long clubeForaId;
    @NotNull private Long estadioId;
    @NotNull @Min(0) private Integer golsCasa;
    @NotNull @Min(0) private Integer golsFora;
    @NotNull private LocalDateTime dataHora;

    public Long getClubeCasaId() {
        return clubeCasaId;
    }

    public void setClubeCasaId(Long clubeCasaId) {
        this.clubeCasaId = clubeCasaId;
    }

    public Long getClubeForaId() {
        return clubeForaId;
    }

    public void setClubeForaId(Long clubeForaId) {
        this.clubeForaId = clubeForaId;
    }

    public Long getEstadioId() {
        return estadioId;
    }

    public void setEstadioId(Long estadioId) {
        this.estadioId = estadioId;
    }

    public Integer getGolsCasa() {
        return golsCasa;
    }

    public void setGolsCasa(Integer golsCasa) {
        this.golsCasa = golsCasa;
    }

    public Integer getGolsFora() {
        return golsFora;
    }

    public void setGolsFora(Integer golsFora) {
        this.golsFora = golsFora;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
