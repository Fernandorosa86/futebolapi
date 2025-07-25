package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.dto.Partida.PartidaRequestDto;
import br.com.meli.futebolapi.dto.Partida.PartidaResponseDto;
import br.com.meli.futebolapi.entity.Clube;
import br.com.meli.futebolapi.entity.Estadio;
import br.com.meli.futebolapi.entity.Partida;
import br.com.meli.futebolapi.exception.NotFoundException;
import br.com.meli.futebolapi.repository.ClubeRepository;
import br.com.meli.futebolapi.repository.EstadioRepository;
import br.com.meli.futebolapi.repository.PartidaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @Test
    void cadastrarPartidaSucesso() {
        Clube mandante = new Clube(); mandante.setId(1L); mandante.setDataCriacao(LocalDate.of(2010,1,1)); mandante.setStatus(true);
        Clube visitante = new Clube(); visitante.setId(2L); visitante.setDataCriacao(LocalDate.of(2010,1,1)); visitante.setStatus(true);
        Estadio estadio = new Estadio(); estadio.setId(11L); estadio.setNome("Maracanã");

        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(1L); req.setClubeForaId(2L); req.setEstadioId(11L);
        req.setGolsCasa(3); req.setGolsFora(1);
        req.setDataHora(LocalDateTime.of(2024, 7, 24, 16, 0));

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(mandante));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(visitante));
        when(estadioRepository.findById(11L)).thenReturn(Optional.of(estadio));
        when(partidaRepository.findByClubeCasaOrClubeFora(mandante, mandante)).thenReturn(List.of());
        when(partidaRepository.findByClubeCasaOrClubeFora(visitante, visitante)).thenReturn(List.of());
        when(partidaRepository.findByEstadioAndDataHoraBetween(eq(estadio), any(), any())).thenReturn(List.of());
        Partida partidaSalva = new Partida(); partidaSalva.setId(100L);
        partidaSalva.setClubeCasa(mandante); partidaSalva.setClubeFora(visitante);
        partidaSalva.setEstadio(estadio); partidaSalva.setGolsCasa(3); partidaSalva.setGolsFora(1);
        partidaSalva.setDataHora(req.getDataHora());
        when(partidaRepository.save(any())).thenReturn(partidaSalva);

        PartidaResponseDto dto = partidaService.cadastrarPartida(req);
        assertEquals(3, dto.getGolsCasa());
        assertEquals(1, dto.getGolsFora());
        assertEquals("Maracanã", dto.getEstadio());
    }

    @Test
    void removerPartidaSucesso() {
        Partida partida = new Partida();
        partida.setId(22L);

        when(partidaRepository.findById(22L)).thenReturn(Optional.of(partida));
        partidaService.removerPartida(22L);
        verify(partidaRepository).delete(partida);
    }

    @Test
    void removerPartidaLancaExceptionSeNaoExiste() {
        when(partidaRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> partidaService.removerPartida(404L));
    }

    @Test
    void buscarPartidaPorIdDeveRetornarDtoSeExiste() {
        Partida partida = new Partida();
        partida.setId(99L);
        Clube c1 = new Clube(); c1.setId(1L); c1.setNome("A");
        Clube c2 = new Clube(); c2.setId(2L); c2.setNome("B");
        Estadio estadio = new Estadio(); estadio.setNome("Maracanã");

        partida.setClubeCasa(c1); partida.setClubeFora(c2); partida.setEstadio(estadio);
        partida.setGolsCasa(3); partida.setGolsFora(1);

        when(partidaRepository.findById(99L)).thenReturn(Optional.of(partida));

        PartidaResponseDto responseDto = partidaService.buscarPartidaPorId(99L);
        assertEquals("A", responseDto.getClubeCasa());
        assertEquals("B", responseDto.getClubeFora());
        assertEquals("Maracanã", responseDto.getEstadio());
    }

    @Test
    void editarPartidaDeveLancarNotFoundSePartidaNaoExiste() {
        when(partidaRepository.findById(99L)).thenReturn(Optional.empty());
        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(1L); req.setClubeForaId(2L); req.setEstadioId(3L);
        assertThrows(NotFoundException.class, () -> partidaService.editarPartida(99L, req));
    }

    @Test
    void editarPartidaDeveLancarExceptionParaClubesIguais() {
        Partida partida = new Partida(); partida.setId(1L);
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(2L); req.setClubeForaId(2L); req.setEstadioId(3L);

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.editarPartida(1L, req));
    }

    @Test
    void editarPartidaDeveLancarExceptionClubeCasaInexistente() {
        Partida partida = new Partida(); partida.setId(1L);
        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(2L); req.setClubeForaId(3L); req.setEstadioId(4L);

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(clubeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.editarPartida(1L, req));
    }

    @Test
    void editarPartidaDeveLancarExceptionEstadioInexistente() {
        Partida partida = new Partida(); partida.setId(1L);
        Clube c1 = new Clube(); c1.setId(2L); c1.setStatus(true); c1.setDataCriacao(LocalDate.of(2020,1,1));
        Clube c2 = new Clube(); c2.setId(3L); c2.setStatus(true); c2.setDataCriacao(LocalDate.of(2020,1,1));

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(c1));
        when(clubeRepository.findById(3L)).thenReturn(Optional.of(c2));
        when(estadioRepository.findById(4L)).thenReturn(Optional.empty());

        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(2L); req.setClubeForaId(3L); req.setEstadioId(4L);

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.editarPartida(1L, req));
    }

    @Test
    void editarPartidaDeveLancarExcecaoSeClubeInativo() {
        Partida partida = new Partida(); partida.setId(1L);
        Clube mandante = new Clube(); mandante.setId(2L); mandante.setStatus(false); mandante.setDataCriacao(LocalDate.of(2020,1,1));
        Clube visitante = new Clube(); visitante.setId(3L); visitante.setStatus(true); visitante.setDataCriacao(LocalDate.of(2020,1,1));
        Estadio estadio = new Estadio(); estadio.setId(4L);

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(mandante));
        when(clubeRepository.findById(3L)).thenReturn(Optional.of(visitante));
        when(estadioRepository.findById(4L)).thenReturn(Optional.of(estadio));

        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(2L); req.setClubeForaId(3L); req.setEstadioId(4L);
        req.setDataHora(LocalDateTime.of(2024, 7, 24, 16, 0));

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.editarPartida(1L, req));
    }

    @Test
    void editarPartidaDeveLancarExcecaoPorDataInvalida() {
        Partida partida = new Partida(); partida.setId(1L);
        Clube mandante = new Clube(); mandante.setId(2L); mandante.setStatus(true); mandante.setDataCriacao(LocalDate.of(2024,1,1));
        Clube visitante = new Clube(); visitante.setId(3L); visitante.setStatus(true); visitante.setDataCriacao(LocalDate.of(2024,1,1));
        Estadio estadio = new Estadio(); estadio.setId(4L);

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(mandante));
        when(clubeRepository.findById(3L)).thenReturn(Optional.of(visitante));
        when(estadioRepository.findById(4L)).thenReturn(Optional.of(estadio));

        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(2L); req.setClubeForaId(3L); req.setEstadioId(4L);
        req.setDataHora(LocalDateTime.of(2020, 7, 24, 16, 0)); // muito antes da fundação dos clubes

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.editarPartida(1L, req));
    }




    @Test
    void editarPartidaDeveLancarExceptionSeConflito48Horas() {

        Clube clubeCasa = new Clube();
        clubeCasa.setId(1L);
        clubeCasa.setDataCriacao(LocalDate.of(2020,1,1));
        clubeCasa.setStatus(true);

        Clube clubeFora = new Clube();
        clubeFora.setId(2L);
        clubeFora.setDataCriacao(LocalDate.of(2020,1,1));
        clubeFora.setStatus(true);

        Estadio estadio = new Estadio();
        estadio.setId(33L);

        Partida partidaExistente = new Partida();
        partidaExistente.setId(50L);
        partidaExistente.setClubeCasa(clubeCasa);
        partidaExistente.setClubeFora(clubeFora);
        partidaExistente.setEstadio(estadio);
        partidaExistente.setDataHora(LocalDateTime.of(2024, 7, 24, 16, 0));

        Partida antiga = new Partida();
        antiga.setId(99L);
        antiga.setDataHora(LocalDateTime.of(2024,7,23,18,0));

        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(1L);
        req.setClubeForaId(2L);
        req.setEstadioId(33L);
        req.setDataHora(LocalDateTime.of(2024, 7, 24, 16, 0));
        req.setGolsCasa(2);
        req.setGolsFora(1);


        when(partidaRepository.findById(50L)).thenReturn(Optional.of(partidaExistente));
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeFora));
        when(estadioRepository.findById(33L)).thenReturn(Optional.of(estadio));
        when(partidaRepository.findByClubeCasaOrClubeFora(clubeCasa, clubeCasa)).thenReturn(List.of(antiga));



        assertThrows(DataIntegrityViolationException.class, () -> partidaService.editarPartida(50L, req));
    }

    @Test
    void editarPartidaDeveLancarExcecaoSeEstadioOcupadoNoMesmoDia() {
        Clube clubeCasa = new Clube();
        clubeCasa.setId(1L);
        clubeCasa.setDataCriacao(LocalDate.of(2020, 1, 1));
        clubeCasa.setStatus(true);
        Clube clubeFora = new Clube();
        clubeFora.setId(2L);
        clubeFora.setDataCriacao(LocalDate.of(2020, 1, 1));
        clubeFora.setStatus(true);
        Estadio estadio = new Estadio();
        estadio.setId(33L);
        estadio.setNome("Maracanã");

        Partida partidaExistente = new Partida();
        partidaExistente.setId(99L);
        partidaExistente.setClubeCasa(clubeCasa);
        partidaExistente.setClubeFora(clubeFora);
        partidaExistente.setEstadio(estadio);
        partidaExistente.setDataHora(LocalDateTime.of(2024, 8, 10, 16, 0));

        Partida partidaOcupada = new Partida();
        partidaOcupada.setId(123L);
        partidaOcupada.setDataHora(LocalDateTime.of(2024, 8, 10, 20, 0));

        PartidaRequestDto partidaRequestDto = new PartidaRequestDto();
        partidaRequestDto.setClubeCasaId(1L);
        partidaRequestDto.setClubeForaId(2L);
        partidaRequestDto.setEstadioId(33L);
        partidaRequestDto.setDataHora(LocalDateTime.of(2024, 8, 10, 17, 0));
        partidaRequestDto.setGolsCasa(2);
        partidaRequestDto.setGolsFora(1);

        when(partidaRepository.findById(99L)).thenReturn(Optional.of(partidaExistente));
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeFora));
        when(estadioRepository.findById(33L)).thenReturn(Optional.of(estadio));
        when(partidaRepository.findByClubeCasaOrClubeFora(clubeCasa, clubeCasa)).thenReturn(List.of());
        when(partidaRepository.findByClubeCasaOrClubeFora(clubeFora, clubeFora)).thenReturn(List.of());

        when(partidaRepository.findByEstadioAndDataHoraBetween(eq(estadio), any(), any()))
                .thenReturn(List.of(partidaOcupada));

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.editarPartida(99L, partidaRequestDto));

    }

    @Test
    void editarPartidaDeveLancarExcecaoSeVisitanteTemConflitoDe48Horas() {
        Clube clubeCasa = new Clube();
        clubeCasa.setId(1L);
        clubeCasa.setDataCriacao(LocalDate.of(2020,1,1));
        clubeCasa.setStatus(true);

        Clube clubeFora = new Clube();
        clubeFora.setId(2L);
        clubeFora.setDataCriacao(LocalDate.of(2020,1,1));
        clubeFora.setStatus(true);

        Estadio estadio = new Estadio(); estadio.setId(33L);

        Partida partidaExistente = new Partida();
        partidaExistente.setId(99L);
        partidaExistente.setClubeCasa(clubeCasa);
        partidaExistente.setClubeFora(clubeFora);
        partidaExistente.setEstadio(estadio);
        partidaExistente.setDataHora(LocalDateTime.of(2024, 8, 10, 16, 0));

        Partida partidaConflito = new Partida();
        partidaConflito.setId(100L);
        partidaConflito.setDataHora(LocalDateTime.of(2024, 8, 10, 10, 0));

        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(1L);
        req.setClubeForaId(2L);
        req.setEstadioId(33L);
        req.setDataHora(LocalDateTime.of(2024, 8, 10, 16, 0));
        req.setGolsCasa(2);
        req.setGolsFora(1);

        when(partidaRepository.findById(99L)).thenReturn(Optional.of(partidaExistente));
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeFora));
        when(estadioRepository.findById(33L)).thenReturn(Optional.of(estadio));


        lenient().when(partidaRepository.findByClubeCasaOrClubeFora(any(Clube.class), any(Clube.class)))
                .thenAnswer(invocation -> {
                    Clube c1 = invocation.getArgument(0);
                    Clube c2 = invocation.getArgument(1);
                    if (c1 != null && c2 != null && c1.getId() == 2L && c2.getId() == 2L) {
                        return List.of(partidaConflito);
                    }
                    return List.of();
                });

        when(partidaRepository.findByEstadioAndDataHoraBetween(eq(estadio), any(), any())).thenReturn(List.of());

        assertThrows(DataIntegrityViolationException.class, () -> partidaService.editarPartida(99L, req));
    }

    @Test
    void editarPartidaDeveAtualizarEDevolverResponseDoBanco() {

        Clube clubeCasa = new Clube();
        clubeCasa.setId(1L);
        clubeCasa.setStatus(true);
        clubeCasa.setDataCriacao(LocalDate.of(2020, 1, 1));
        Clube clubeFora = new Clube();
        clubeFora.setId(2L);
        clubeFora.setStatus(true);
        clubeFora.setDataCriacao(LocalDate.of(2020, 1, 1));
        Estadio estadio = new Estadio();
        estadio.setId(33L);
        estadio.setNome("Maracanã");

        Partida partida = new Partida();
        partida.setId(99L);
        partida.setClubeCasa(clubeCasa);
        partida.setClubeFora(clubeFora);
        partida.setEstadio(estadio);
        partida.setDataHora(LocalDateTime.of(2024, 8, 10, 16, 0));
        partida.setGolsCasa(1);
        partida.setGolsFora(1);

        PartidaRequestDto req = new PartidaRequestDto();
        req.setClubeCasaId(1L);
        req.setClubeForaId(2L);
        req.setEstadioId(33L);

        req.setDataHora(LocalDateTime.of(2024, 8, 12, 17, 0));
        req.setGolsCasa(0);
        req.setGolsFora(0);


        when(partidaRepository.findById(99L)).thenReturn(Optional.of(partida));
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeFora));
        when(estadioRepository.findById(33L)).thenReturn(Optional.of(estadio));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(partidaRepository.findByClubeCasaOrClubeFora(any(), any())).thenReturn(List.of());
        when(partidaRepository.findByEstadioAndDataHoraBetween(eq(estadio), any(), any())).thenReturn(List.of());


        PartidaResponseDto dto = partidaService.editarPartida(99L, req);


        assertEquals("Maracanã", dto.getEstadio());
        assertEquals(0, dto.getGolsCasa());
        assertEquals(0, dto.getGolsFora());
        assertEquals(LocalDateTime.of(2024, 8, 12, 17, 0), dto.getDataHora());
        verify(partidaRepository).save(any(Partida.class));
    }

}
