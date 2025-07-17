package br.com.meli.futebolapi.repository;

import br.com.meli.futebolapi.entity.Clube;
import br.com.meli.futebolapi.entity.Estadio;
import br.com.meli.futebolapi.entity.Partida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    //usar List para validações e checagem de regra de negócio.

    List<Partida> findByClubeCasaOrClubeFora(Clube clubeCasa, Clube clubeFora);
    List<Partida> findByEstadioAndDataHoraBetween(Estadio estadio, LocalDateTime inicio, LocalDateTime fim);

    //usar Pageable para listagens e retornos paginados

    Page<Partida> findByEstadio(Estadio estadio, Pageable pageable);
    Page<Partida> findByClubeCasaOrClubeForaAndEstadio(Clube clubeCasa, Clube clubeFora, Estadio estadio, Pageable pageable);
    Page<Partida> findByClubeCasaOrClubeFora(Clube clubeCasa, Clube clubeFora, Pageable pageable);

}
