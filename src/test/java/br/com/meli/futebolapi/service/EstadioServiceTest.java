package br.com.meli.futebolapi.service;


import br.com.meli.futebolapi.dto.Estadio.EstadioRequestDto;
import br.com.meli.futebolapi.dto.Estadio.EstadioResponseDto;
import br.com.meli.futebolapi.entity.Estadio;
import br.com.meli.futebolapi.exception.NotFoundException;
import br.com.meli.futebolapi.repository.EstadioRepository;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstadioServiceTest {

    @Mock
    private EstadioRepository estadioRepository;

    @InjectMocks
    private EstadioService estadioService;

    @Test
    void salvarDeveSalvarEstadioComNomeValido() {

        EstadioRequestDto estadioRequestDto = new EstadioRequestDto();
        estadioRequestDto.setNome("Maracanã");

        when(estadioRepository.existsByNome("Maracanã")).thenReturn(false);

        Estadio estadio = new Estadio();
        estadio.setId(1L);
        estadio.setNome("Maracanã");
        when(estadioRepository.save(any())).thenReturn(estadio);

        EstadioResponseDto responseDto = estadioService.salvar(estadioRequestDto);
        assertEquals("Maracanã", responseDto.getNome());
        assertEquals(1L, responseDto.getId());

    }

    @Test
    void salvarDeveLancarExceptionQuandoNomeJaExiste() {
        EstadioRequestDto estadioRequestDto = new EstadioRequestDto();
        estadioRequestDto.setNome("Maracanã");

        when(estadioRepository.existsByNome("Maracanã")).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> estadioService.salvar(estadioRequestDto));

        verify(estadioRepository, never()).save(any());

    }

    @Test
    void atualizarDeveEditarNomeSePermitido() {
        Estadio estadio = new Estadio();
        estadio.setId(10L);
        estadio.setNome("NomeVelho");

        EstadioRequestDto estadioRequestDto = new EstadioRequestDto();
        estadioRequestDto.setNome("NomeNovo");

        when(estadioRepository.findById(10L)).thenReturn(Optional.of(estadio));
        when(estadioRepository.findByNome("NomeNovo")).thenReturn(Optional.empty());
        when(estadioRepository.save(any())).thenReturn(estadio);

        EstadioResponseDto estadioResponseDto = estadioService.atualizar(10L, estadioRequestDto);

        assertEquals("NomeNovo", estadioResponseDto.getNome());
    }

    @Test
    void atualizarDeveLancarExceptionSeIdNaoExiste() {
        when(estadioRepository.findById(999L)).thenReturn(Optional.empty());

        EstadioRequestDto estadioRequestDto = new EstadioRequestDto();
        estadioRequestDto.setNome("Qualquer");

        assertThrows(NotFoundException.class, () -> estadioService.atualizar(999L, estadioRequestDto));
    }

    @Test
    void atualizarDeveLancarExceptionSeNomeDuplicado() {
        Estadio estadio = new Estadio();
        estadio.setId(2L);
        estadio.setNome("Pacaembu");

        Estadio outroEstadio = new Estadio();
        outroEstadio.setId(3L);
        outroEstadio.setNome("Morumbis");

        EstadioRequestDto estadioRequestDto = new EstadioRequestDto();
        estadioRequestDto.setNome("Morumbis");

        when(estadioRepository.findById(2L)).thenReturn(Optional.of(estadio));
        when(estadioRepository.findByNome("Morumbis")).thenReturn(Optional.of(outroEstadio));

        assertThrows(DataIntegrityViolationException.class, () -> estadioService.atualizar(2L, estadioRequestDto));
    }

    @Test
    void excluirEstadioDeveRemoverSeExistir() {
        Estadio estadio = new Estadio();
        estadio.setId(1L);
        when(estadioRepository.findById(1L)).thenReturn(Optional.of(estadio));

        estadioService.excluirEstadio(1L);
        verify(estadioRepository).deleteById(1L);
    }

    @Test
    void excluirEstadioDeveLancarExceptionSeNaoExiste() {
        when(estadioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> estadioService.excluirEstadio(1L));
        verify(estadioRepository, never()).deleteById(any());
    }

    @Test
    void listarEstadiosDeveRetornarComFiltroNome() {
        Estadio estadio = new Estadio();
        estadio.setId(7L);
        estadio.setNome("Maracanã");
        Page<Estadio> page = new PageImpl<>(List.of(estadio));

        when(estadioRepository.findByNomeContainingIgnoreCase(eq("Maracanã"), any(Pageable.class)))
                .thenReturn(page);

        Page<EstadioResponseDto> resultado = estadioService.listarEstadios("Maracanã", 0, 10, "nome", "asc");
        assertEquals(1, resultado.getContent().size());
        assertEquals("Maracanã", resultado.getContent().getFirst().getNome());
    }

    @Test
    void listarEstadiosDeveRetornarTodosQuandoNomeNuloOuVazio() {
        Estadio estadio = new Estadio();
        estadio.setId(3L);
        estadio.setNome("Beira Rio");
        Page<Estadio> page = new PageImpl<>(List.of(estadio));
        when(estadioRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<EstadioResponseDto> resultado = estadioService.listarEstadios(null, 0, 10, "nome", "asc");
        assertEquals(1, resultado.getContent().size());
        assertEquals("Beira Rio", resultado.getContent().getFirst().getNome());

        resultado = estadioService.listarEstadios("  ", 0, 10, "nome", "asc");
        assertEquals(1, resultado.getContent().size());
        assertEquals("Beira Rio", resultado.getContent().getFirst().getNome());
    }



}
