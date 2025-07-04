package br.com.meli.futebolapi.controller;

import br.com.meli.futebolapi.dto.EstadioRequestDto;
import br.com.meli.futebolapi.dto.EstadioResponseDto;
import br.com.meli.futebolapi.service.EstadioService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/estadios")
public class EstadioController {

    private EstadioService estadioService;

    public EstadioController(EstadioService estadioService) {
        this.estadioService = estadioService;
    }

    @PostMapping
    public ResponseEntity<?> criarEstadio(@Valid @RequestBody EstadioRequestDto estadioRequestDto) {
        try {
            EstadioResponseDto estadioResponseDto = estadioService.salvar(estadioRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(estadioResponseDto);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
