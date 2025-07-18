package br.com.meli.futebolapi.controller;

import br.com.meli.futebolapi.dto.Clube.ClubeRankingDto;
import br.com.meli.futebolapi.dto.Clube.ClubeRequestDto;
import br.com.meli.futebolapi.dto.Clube.ClubeResponseDto;
import br.com.meli.futebolapi.dto.Confronto.ConfrontoDiretoResponseDto;
import br.com.meli.futebolapi.dto.Confronto.ConfrontoResponseDto;
import br.com.meli.futebolapi.dto.Retrospecto.RetrospectoResponseDto;
import br.com.meli.futebolapi.exception.NotFoundException;
import br.com.meli.futebolapi.service.ClubeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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

    @PutMapping("/{id}")
    public ResponseEntity<?> editarClube(@PathVariable Long id, @Valid @RequestBody ClubeRequestDto clubeRequestDto) {
        try {
            ClubeResponseDto atualizado = clubeService.editar(id, clubeRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(atualizado);
        } catch (br.com.meli.futebolapi.exception.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> inativarClube(@PathVariable Long id) {
        try {
            clubeService.inativarClube(id);
            return ResponseEntity.noContent().build();
        } catch (br.com.meli.futebolapi.exception.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            ClubeResponseDto clube = clubeService.buscarPorId(id);
            return ResponseEntity.status(HttpStatus.OK).body(clube);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarClubes(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direcao
    ) {
        Page<ClubeResponseDto> pagina = clubeService.listarClubes(
                nome, estado, status, page, size, ordenarPor, direcao
        );
        return ResponseEntity.status(HttpStatus.OK).body(pagina);
    }

    @GetMapping("/{id}/retrospecto")
    public ResponseEntity<?> retrospecto(@PathVariable Long id) {
        try {
            RetrospectoResponseDto retrospectoResponseDto = clubeService.calcularRetrospecto(id);
            return ResponseEntity.status(HttpStatus.OK).body(retrospectoResponseDto);
        } catch (br.com.meli.futebolapi.exception.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/retrospecto-contra-adversarios")
    public ResponseEntity<?> retrospectoContraAdversarios(@PathVariable Long id) {
        try {
            List<ConfrontoResponseDto> confrontos = clubeService.retrospectoContraAdversarios(id);
            return ResponseEntity.status(HttpStatus.OK).body(confrontos);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/confronto-direto")
    public ResponseEntity<?> confrontoDireto( @PathVariable Long id, @RequestParam Long adversarioId) {
        try {
            ConfrontoDiretoResponseDto resultado = clubeService.getConfrontoDireto(id, adversarioId);
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        }catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/ranking")
    public ResponseEntity<?> ranking(
            @RequestParam(defaultValue = "pontos") String criterio) {
        List<ClubeRankingDto> ranking = clubeService.getRanking(criterio);
        return ResponseEntity.status(HttpStatus.OK).body(ranking);
    }

}



