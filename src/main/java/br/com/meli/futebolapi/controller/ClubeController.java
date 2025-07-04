package br.com.meli.futebolapi.controller;

import br.com.meli.futebolapi.dto.ClubeRequestDto;
import br.com.meli.futebolapi.dto.ClubeResponseDto;
import br.com.meli.futebolapi.service.ClubeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/clubes")
public class ClubeController {
    private final ClubeService clubeService;

    public ClubeController(ClubeService clubeService) {
        this.clubeService = clubeService;
    }

    @PostMapping
    public ResponseEntity<?> criarClube(@Valid @RequestBody ClubeRequestDto clubeRequestDto) {
    try {
        ClubeResponseDto novo = clubeService.salvar(clubeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
     } catch (Exception e) {
        if (e.getMessage().equals("Clube já existe"))
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
     }

    }

}
