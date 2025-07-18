package br.com.meli.futebolapi.dto;

import java.time.LocalDateTime;

public class PartidaSimplesDto {
    private Long id;
    private LocalDateTime dataHora;
    private String clubeCasa;
    private String clubeFora;
    private Integer golsCasa;
    private Integer golsFora;
    private String estadio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getClubeCasa() {
        return clubeCasa;
    }

    public void setClubeCasa(String clubeCasa) {
        this.clubeCasa = clubeCasa;
    }

    public String getClubeFora() {
        return clubeFora;
    }

    public void setClubeFora(String clubeFora) {
        this.clubeFora = clubeFora;
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

    public String getEstadio() {
        return estadio;
    }

    public void setEstadio(String estadio) {
        this.estadio = estadio;
    }
}
