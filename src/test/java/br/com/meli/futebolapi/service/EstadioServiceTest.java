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


}
