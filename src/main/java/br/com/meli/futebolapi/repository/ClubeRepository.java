package br.com.meli.futebolapi.repository;

import br.com.meli.futebolapi.model.Clube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ClubeRepository extends JpaRepository<Clube, Long>, JpaSpecificationExecutor<Clube> {
    boolean existsByNomeAndEstado(String nome, String estado);


    Optional<Clube> findByNomeAndEstado(String nome, String estado);
}
