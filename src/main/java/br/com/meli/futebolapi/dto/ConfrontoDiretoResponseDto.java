package br.com.meli.futebolapi.dto;

import java.util.List;

public class ConfrontoDiretoResponseDto {
    private RetrospectoSimplesDto retrospectoClubeA;
    private RetrospectoSimplesDto retrospectoClubeB;
    private List<PartidaSimplesDto> partidas;

    public RetrospectoSimplesDto getRetrospectoClubeA() {
        return retrospectoClubeA;
    }

    public void setRetrospectoClubeA(RetrospectoSimplesDto retrospectoClubeA) {
        this.retrospectoClubeA = retrospectoClubeA;
    }

    public RetrospectoSimplesDto getRetrospectoClubeB() {
        return retrospectoClubeB;
    }

    public void setRetrospectoClubeB(RetrospectoSimplesDto retrospectoClubeB) {
        this.retrospectoClubeB = retrospectoClubeB;
    }

    public List<PartidaSimplesDto> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<PartidaSimplesDto> partidas) {
        this.partidas = partidas;
    }
}
