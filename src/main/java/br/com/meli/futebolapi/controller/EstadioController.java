package br.com.meli.futebolapi.controller;

import br.com.meli.futebolapi.dto.EstadioRequestDto;
import br.com.meli.futebolapi.dto.EstadioResponseDto;
import br.com.meli.futebolapi.service.EstadioService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estadios")
public class EstadioController {

    private final EstadioService estadioService;

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

    @PutMapping("/{id}")
    public ResponseEntity<?> editarEstadio(@PathVariable Long id, @Valid @RequestBody EstadioRequestDto estadioRequestDto) {
        try {
            EstadioResponseDto atualizado = estadioService.atualizar(id, estadioRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(atualizado);
        } catch (br.com.meli.futebolapi.exception.NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch  (org.springframework.dao.DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirEstadio(@PathVariable Long id) {
        try {
            estadioService.excluirEstadio(id);
            return ResponseEntity.noContent().build();
        } catch (br.com.meli.futebolapi.exception.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch  (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Não é possével excluir o estádio: Está sendo utilizado em uma partida.");

        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarEstadio(@PathVariable Long id) {
        try {
            EstadioResponseDto estadio = estadioService.buscarEstadioPorId(id);
            return ResponseEntity.status(HttpStatus.OK).body(estadio);
        } catch (br.com.meli.futebolapi.exception.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarEstadios(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direcao
    ){
        Page<EstadioResponseDto> pagina = estadioService.listarEstadios(nome, page, size, ordenarPor, direcao);
        return ResponseEntity.status(HttpStatus.OK).body(pagina);
    }
}
