package br.com.meli.futebolapi.repository;

import br.com.meli.futebolapi.entity.Clube;
import br.com.meli.futebolapi.entity.Estadio;
import br.com.meli.futebolapi.entity.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PartidaRepository extends JpaRepository<Partida, Long> {
    List<Partida> findByClubeCasaOrClubeFora(Clube clubeCasa, Clube clubeFora);
    List<Partida> findByEstadioAndDataHoraBetween(Estadio estadio, LocalDateTime inicio, LocalDateTime fim);
}
