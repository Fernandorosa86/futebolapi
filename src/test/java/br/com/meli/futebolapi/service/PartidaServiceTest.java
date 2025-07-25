package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.dto.Partida.PartidaRequestDto;
import br.com.meli.futebolapi.repository.ClubeRepository;
import br.com.meli.futebolapi.repository.EstadioRepository;
import br.com.meli.futebolapi.repository.PartidaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PartidaServiceTest {
    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private ClubeRepository clubeRepository;

    @Mock
    private EstadioRepository estadioRepository;

    @InjectMocks
    private PartidaService partidaService;


    @Test
    void cadastrarPartidaDeveLancarExceptionParaClubesIguais() {
        PartidaRequestDto partidaRequestDto = new PartidaRequestDto();
        partidaRequestDto.setClubeCasaId(1L);
        partidaRequestDto.setClubeForaId(1L);

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.cadastrarPartida(partidaRequestDto));
    }

    @Test
    void cadastrarPartidaDeveLancarExceptionSeClubeMandanteNaoExiste() {
        PartidaRequestDto partidaRequestDto = new PartidaRequestDto();
        partidaRequestDto.setClubeCasaId(1L);
        partidaRequestDto.setClubeForaId(2L);
        partidaRequestDto.setEstadioId(3L);

        when(clubeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.cadastrarPartida(partidaRequestDto));
    }





}
