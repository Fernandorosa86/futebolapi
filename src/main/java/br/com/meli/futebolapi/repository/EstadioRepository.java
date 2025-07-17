package br.com.meli.futebolapi.repository;

import br.com.meli.futebolapi.entity.Estadio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadioRepository extends JpaRepository<Estadio, Long> {
    boolean existsByNome(String nome);

    Optional<Estadio> findByNome(String nome);

    Page<Estadio> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

}
