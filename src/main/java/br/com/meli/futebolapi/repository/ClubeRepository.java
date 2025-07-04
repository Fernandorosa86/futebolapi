package br.com.meli.futebolapi.repository;

import br.com.meli.futebolapi.model.Clube;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubeRepository extends JpaRepository<Clube, Long> {
    boolean existsByNomeAndEstado(String nome, String estado);


    Optional<Clube> findByNomeAndEstado(String nome, String estado);
}
