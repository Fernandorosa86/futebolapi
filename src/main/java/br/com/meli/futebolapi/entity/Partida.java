package br.com.meli.futebolapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Partida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "clube_casa_id")
    private Clube clubeCasa;

    @ManyToOne @JoinColumn(name = "clube_fora_id")
    private Clube clubeFora;

    @ManyToOne @JoinColumn(name = "estadio_id")
    private Estadio estadio;
    private Integer golsCasa;
    private Integer golsFora;
    private LocalDateTime dataHora;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Clube getClubeCasa() {
        return clubeCasa;
    }

    public void setClubeCasa(Clube clubeCasa) {
        this.clubeCasa = clubeCasa;
    }

    public Clube getClubeFora() {
        return clubeFora;
    }

    public void setClubeFora(Clube clubeFora) {
        this.clubeFora = clubeFora;
    }

    public Estadio getEstadio() {
        return estadio;
    }

    public void setEstadio(Estadio estadio) {
        this.estadio = estadio;
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
