package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.dto.Clube.ClubeRequestDto;
import br.com.meli.futebolapi.dto.Clube.ClubeResponseDto;
import br.com.meli.futebolapi.entity.Clube;
import br.com.meli.futebolapi.exception.NotFoundException;
import br.com.meli.futebolapi.repository.ClubeRepository;
import br.com.meli.futebolapi.repository.PartidaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubeServiceTest {

    @Mock
    private ClubeRepository clubeRepository;
    @Mock private PartidaRepository partidaRepository;
    @InjectMocks
    private ClubeService clubeService;

    @Test
    void salvarDeveCadastrarUmClubeSeNaoExistir() {
        ClubeRequestDto clubeRequestDto = new ClubeRequestDto();
        clubeRequestDto.setNome("Portuguesa");
        clubeRequestDto.setEstado("RJ");

        when(clubeRepository.existsByNomeAndEstado("Portuguesa", "RJ")).thenReturn(false);

        Clube clubeSalvo = new Clube();
        clubeSalvo.setId(1L);
        clubeSalvo.setNome("Portuguesa");
        clubeSalvo.setEstado("RJ");
        when(clubeRepository.save(any(Clube.class))).thenReturn(clubeSalvo);

        ClubeResponseDto resultado = clubeService.salvar(clubeRequestDto);

        assertEquals("Portuguesa", resultado.getNome());
        assertEquals("RJ", resultado.getEstado());
        assertEquals(1L, resultado.getId());

    }

    @Test
    void salvarDeveLancarExceptionSeNomeEstadoDuplicado() {
        ClubeRequestDto clubeRequestDto = new ClubeRequestDto();
        clubeRequestDto.setNome("Flamengo");
        clubeRequestDto.setEstado("RJ");
        when(clubeRepository.existsByNomeAndEstado("Flamengo", "RJ")).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> clubeService.salvar(clubeRequestDto));
    }

    @Test
    void editarDeveEditarSeValido() {
        Long id = 1L;
        Clube clubeExistente = new Clube();
        clubeExistente.setId(id);
        clubeExistente.setNome("Flamengo");
        clubeExistente.setEstado("RJ");

        ClubeRequestDto clubeRequestDto = new ClubeRequestDto();
        clubeRequestDto.setNome("Flamengo Novo");
        clubeRequestDto.setEstado("RJ");
        clubeRequestDto.setStatus(true);

        when(clubeRepository.findById(id)).thenReturn(Optional.of(clubeExistente));
        when(clubeRepository.existsByNomeAndEstado("Flamengo Novo", "RJ")).thenReturn(false);
        when(clubeRepository.save(any(Clube.class))).thenReturn(clubeExistente);

        ClubeResponseDto response = clubeService.editar(id, clubeRequestDto);

        assertEquals("Flamengo Novo", response.getNome());
    }

    @Test
    void editarDeveLancarExceptionSeDuplicado() {
        Long id = 1L;
        Clube clube = new Clube();
        clube.setId(id);
        clube.setNome("A");
        clube.setEstado("RJ");

        ClubeRequestDto clubeRequestDto = new ClubeRequestDto();
        clubeRequestDto.setNome("B");
        clubeRequestDto.setEstado("RJ");
        when(clubeRepository.findById(id)).thenReturn(Optional.of(clube));
        when(clubeRepository.existsByNomeAndEstado("B", "RJ")).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> clubeService.editar(id, clubeRequestDto));
    }

    @Test
    void inativarDeveSetarStatusFalse() {
        Clube clube = new Clube();
        clube.setId(1L);
        clube.setStatus(true);
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        clubeService.inativarClube(1L);
        verify(clubeRepository).save(argThat(c -> !c.getStatus()));
    }

    @Test
    void inativarLancaExceptionSeNaoExiste() {
        when(clubeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clubeService.inativarClube(999L));
    }

    @Test
    void buscarPorIdDeveRetornarDtoSeExiste() {
        Clube clube = new Clube();
        clube.setId(1L);
        clube.setNome("Portuguesa");
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        ClubeResponseDto response = clubeService.buscarPorId(1L);
        assertEquals("Portuguesa", response.getNome());
    }

    @Test
    void buscarPorIdDeveLancarExceptionSeNaoExiste() {
        when(clubeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clubeService.buscarPorId(999L));
    }

    @Test
    void listarClubesDeveRetornarPageComTodosOsClubesSemFiltro() {
        Clube  clube = new Clube();
        clube.setId(1L);
        clube.setNome("Portuguesa");
        clube.setEstado("RJ");

        List<Clube> clubes = List.of(clube);
        Page<Clube> page = new PageImpl<>(clubes);

        when(clubeRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ClubeResponseDto> resultado = clubeService.listarClubes(
                null, null, null, 0,
                10, "id", "asc");

        assertEquals(1, resultado.getContent().size());
        assertEquals("Portuguesa", resultado.getContent().get(0).getNome());
        verify(clubeRepository).findAll(any(Pageable.class));
    }

    @Test
    void listarClubesComFiltroNome() {
        Clube  clube = new Clube();
        clube.setId(1L);
        clube.setNome("Flamengo");

        Page<Clube> page = new PageImpl<>(List.of(clube));
        when(clubeRepository.findByNomeContainingIgnoreCase(eq("Fla"), any(Pageable.class)))
                .thenReturn(page);

        Page<ClubeResponseDto> resultado = clubeService.listarClubes(
                "Fla", null, null, 0,
                10, "id", "asc");

        assertEquals(1, resultado.getContent().size());
        assertEquals("Flamengo", resultado.getContent().get(0).getNome());
        verify(clubeRepository).findByNomeContainingIgnoreCase(eq("Fla"), any(Pageable.class));

    }

    @Test
    void listarClubesComTodosOsFiltros() {
        Clube  clube = new Clube();
        clube.setId(1L);
        clube.setNome("Portuguesa");
        clube.setEstado("RJ");
        clube.setStatus(true);

        Page<Clube> page = new PageImpl<>(List.of(clube));
        when(clubeRepository.findByNomeContainingIgnoreCaseAndEstadoAndStatus(
                eq("Portuguesa"), eq("RJ"), eq(true), any(Pageable.class)))
                .thenReturn(page);

        Page<ClubeResponseDto> resultado = clubeService.listarClubes(
                "Portuguesa", "RJ", true, 0,
                10, "id", "asc");
        assertEquals(1, resultado.getContent().size());
        assertEquals("Portuguesa", resultado.getContent().get(0).getNome());
        assertEquals("RJ", resultado.getContent().get(0).getEstado());
        verify(clubeRepository).findByNomeContainingIgnoreCaseAndEstadoAndStatus(
                eq("Portuguesa"), eq("RJ"), eq(true), any(Pageable.class));
    }

    @Test
    void listarClubesSemResultados() {
        Page<Clube> page = Page.empty();
        when(clubeRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ClubeResponseDto> resultado = clubeService.listarClubes(
                null, null, null, 0, 10, "id", "asc");

        assertTrue(resultado.isEmpty());
    }


}
