package br.com.meli.futebolapi.controller;


import br.com.meli.futebolapi.dto.PartidaRequestDto;
import br.com.meli.futebolapi.dto.PartidaResponseDto;
import br.com.meli.futebolapi.entity.Partida;
import br.com.meli.futebolapi.service.PartidaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
