package br.com.meli.futebolapi.controller;


import br.com.meli.futebolapi.dto.PartidaRequestDto;
import br.com.meli.futebolapi.dto.PartidaResponseDto;
import br.com.meli.futebolapi.entity.Partida;
import br.com.meli.futebolapi.exception.NotFoundException;
import br.com.meli.futebolapi.service.PartidaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/partidas")
public class PartidaController {

    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrarPartida(@Valid @RequestBody PartidaRequestDto partidaRequestDto) {

        try {
            PartidaResponseDto partidaResponseDto = partidaService.cadastrarPartida(partidaRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(partidaResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarPartida(
            @PathVariable Long id,
            @Valid @RequestBody PartidaRequestDto partidaRequestDto) {
        try {
            PartidaResponseDto updated = partidaService.editarPartida(id, partidaRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(updated);
        } catch (EntityNotFoundException | NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
