package br.com.meli.futebolapi.repository;

import br.com.meli.futebolapi.dto.ClubeResponseDto;
import br.com.meli.futebolapi.entity.Clube;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ClubeRepository extends JpaRepository<Clube, Long>, JpaSpecificationExecutor<Clube> {
    boolean existsByNomeAndEstado(String nome, String estado);




    Page<Clube> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Clube> findByEstado(String estado, Pageable pageable);
    Page<Clube> findByStatus(Boolean status, Pageable pageable);
    Page<Clube> findByNomeContainsIgnoreCaseAndEstado(String nome, String estado, Pageable pageable);
    Page<Clube> findByNomeContainingIgnoreCaseAndEstadoAndStatus(String nome, String estado, Boolean status, Pageable pageable);


}
