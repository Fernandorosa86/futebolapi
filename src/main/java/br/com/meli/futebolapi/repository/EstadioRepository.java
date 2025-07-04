package br.com.meli.futebolapi.repository;

import br.com.meli.futebolapi.model.Estadio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadioRepository extends JpaRepository<Estadio, Long> {
    boolean existsByNome(String nome);

    Optional<Estadio> findByNome(String nome);
}
