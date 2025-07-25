package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.dto.Partida.PartidaRequestDto;
import br.com.meli.futebolapi.entity.Clube;
import br.com.meli.futebolapi.entity.Estadio;
import br.com.meli.futebolapi.entity.Partida;
import br.com.meli.futebolapi.repository.ClubeRepository;
import br.com.meli.futebolapi.repository.EstadioRepository;
import br.com.meli.futebolapi.repository.PartidaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
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

    @Test
    void cadastrarPartidaDeveLancarExceptionSeDataInvalida() {
        Clube clubeCasa = new Clube(); clubeCasa.setId(1L); clubeCasa.setDataCriacao(LocalDate.of(2020,1,1)); clubeCasa.setStatus(true);
        Clube clubeFora = new Clube(); clubeFora.setId(2L); clubeFora.setDataCriacao(LocalDate.of(2020,1,1)); clubeFora.setStatus(true);
        Estadio estadio = new Estadio(); estadio.setId(3L);

        PartidaRequestDto partidaRequestDto = new PartidaRequestDto();
        partidaRequestDto.setClubeCasaId(1L); partidaRequestDto.setClubeForaId(2L); partidaRequestDto.setEstadioId(3L);
        partidaRequestDto.setGolsCasa(2); partidaRequestDto.setGolsFora(1);
        partidaRequestDto.setDataHora(LocalDateTime.of(2019,1,2,12,0));

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeFora));
        when(estadioRepository.findById(3L)).thenReturn(Optional.of(estadio));

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.cadastrarPartida(partidaRequestDto));

    }

    @Test
    void cadastrarPartidaDeveLancarExceptionSeConflito48hMandante() {
        Clube clubeCasa = new Clube(); clubeCasa.setId(1L); clubeCasa.setDataCriacao(LocalDate.of(2020,1,1)); clubeCasa.setStatus(true);
        Clube clubeFora = new Clube(); clubeFora.setId(2L); clubeFora.setDataCriacao(LocalDate.of(2020,1,1)); clubeFora.setStatus(true);
        Estadio estadio = new Estadio();

        PartidaRequestDto partidaRequestDto = new PartidaRequestDto();
        partidaRequestDto.setClubeCasaId(1L); partidaRequestDto.setClubeForaId(2L); partidaRequestDto.setEstadioId(3L);
        partidaRequestDto.setGolsCasa(2); partidaRequestDto.setGolsFora(1);
        partidaRequestDto.setDataHora(LocalDateTime.of(2022,1,3,16,0));

        Partida antiga = new Partida();
        antiga.setDataHora(LocalDateTime.of(2022,1,3,16,0));

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeFora));
        when(estadioRepository.findById(3L)).thenReturn(Optional.of(estadio));
        when(partidaRepository.findByClubeCasaOrClubeFora(clubeCasa, clubeCasa)).thenReturn(List.of(antiga));
        when(partidaRepository.findByClubeCasaOrClubeFora(clubeFora, clubeFora)).thenReturn(List.of());

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.cadastrarPartida(partidaRequestDto));
    }

    @Test
    void cadastrarPartidaDeveLancarExceptionSeEstadioOcupadoNoDia() {
        Clube mandante = new Clube(); mandante.setId(1L); mandante.setDataCriacao(LocalDate.of(2020,1,1));
        mandante.setStatus(true);
        Clube visitante = new Clube(); visitante.setId(2L); visitante.setDataCriacao(LocalDate.of(2020,1,1));
        visitante.setStatus(true);
        Estadio estadio = new Estadio(); estadio.setId(33L);

        PartidaRequestDto partidaRequestDto = new PartidaRequestDto();
        partidaRequestDto.setClubeCasaId(1L);  partidaRequestDto.setClubeForaId(2L); partidaRequestDto.setEstadioId(33L);
        partidaRequestDto.setGolsCasa(2); partidaRequestDto.setGolsFora(1);
        partidaRequestDto.setDataHora(LocalDateTime.of(2024,7,24,16,0));

        when(clubeRepository.findById(eq(1L))).thenReturn(Optional.of(mandante));
        when(clubeRepository.findById(eq(2L))).thenReturn(Optional.of(visitante));
        when(estadioRepository.findById(33L)).thenReturn(Optional.of(estadio));
        when(partidaRepository.findByClubeCasaOrClubeFora(any(), any())).thenReturn(List.of());
        when(partidaRepository.findByEstadioAndDataHoraBetween(eq(estadio), any(), any()))
                .thenReturn(List.of(new Partida()));
        assertThrows(DataIntegrityViolationException.class, () -> partidaService.cadastrarPartida(partidaRequestDto));
    }





}
