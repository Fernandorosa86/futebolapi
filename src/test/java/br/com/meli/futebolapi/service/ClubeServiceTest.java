package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.dto.Clube.ClubeRequestDto;
import br.com.meli.futebolapi.dto.Clube.ClubeResponseDto;
import br.com.meli.futebolapi.dto.Confronto.ConfrontoDiretoResponseDto;
import br.com.meli.futebolapi.dto.Confronto.ConfrontoResponseDto;
import br.com.meli.futebolapi.dto.Retrospecto.RetrospectoResponseDto;
import br.com.meli.futebolapi.entity.Clube;
import br.com.meli.futebolapi.entity.Estadio;
import br.com.meli.futebolapi.entity.Partida;
import br.com.meli.futebolapi.exception.NotFoundException;
import br.com.meli.futebolapi.repository.ClubeRepository;
import br.com.meli.futebolapi.repository.PartidaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
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
        assertEquals("Portuguesa", resultado.getContent().getFirst().getNome());
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
        assertEquals("Flamengo", resultado.getContent().getFirst().getNome());
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
        assertEquals("Portuguesa", resultado.getContent().getFirst().getNome());
        assertEquals("RJ", resultado.getContent().getFirst().getEstado());
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

    @Test
    void listarClubesDeveChamarMetodoPorNomeEEstado() {
        Clube  clube = new Clube();
        clube.setId(1L);
        clube.setNome("Flamengo");
        clube.setEstado("RJ");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<Clube> page = new PageImpl<>(List.of(clube));

        when(clubeRepository.findByNomeContainsIgnoreCaseAndEstado(eq("Flamengo"), eq("RJ"), any(Pageable.class)))
                .thenReturn(page);

        Page<ClubeResponseDto> resultado = clubeService.listarClubes(
                "Flamengo", "RJ", null, 0, 10, "id", "asc");

        assertEquals(1, resultado.getContent().size());
        assertEquals("Flamengo", resultado.getContent().getFirst().getNome());
        assertEquals("RJ", resultado.getContent().getFirst().getEstado());

        verify(clubeRepository).findByNomeContainsIgnoreCaseAndEstado(eq("Flamengo"), eq("RJ"), any(Pageable.class));

    }

    @Test
    void listarClubesDeveFiltrarPorEstado() {
        Clube clube = new Clube();
        clube.setId(1L);
        clube.setNome("Flamengo");
        clube.setEstado("RJ");

        Page<Clube> page = new PageImpl<>(List.of(clube));

        when(clubeRepository.findByEstado(eq("RJ"), any(Pageable.class)))
                .thenReturn(page);


        Page<ClubeResponseDto> resultado = clubeService.listarClubes(
                null, "RJ", null, 0, 10, "id", "asc");


        assertEquals(1, resultado.getContent().size());
        assertEquals("Flamengo", resultado.getContent().getFirst().getNome());
        assertEquals("RJ", resultado.getContent().getFirst().getEstado());
        verify(clubeRepository).findByEstado(eq("RJ"), any(Pageable.class));
    }

    @Test
    void listarClubesDeveFiltrarPorStatus() {
        Clube  clube = new Clube();
        clube.setId(1L);
        clube.setNome("Flamengo");
        clube.setEstado("RJ");

        Page<Clube> page = new PageImpl<>(List.of(clube));

        when(clubeRepository.findByStatus(eq(true), any(Pageable.class)))
                .thenReturn(page);

        Page<ClubeResponseDto> resultado = clubeService.listarClubes(
                null, null, true, 0, 10, "id", "asc");

        assertEquals(1, resultado.getContent().size());
        assertEquals("Flamengo", resultado.getContent().getFirst().getNome());
        assertEquals("RJ", resultado.getContent().getFirst().getEstado());

        verify(clubeRepository).findByStatus(eq(true), any(Pageable.class));
    }

    @Test
    void CalcularRetrospectoDeveLancarExceptionSeClubeNaoExiste() {
        when(clubeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clubeService.calcularRetrospecto(99L));
    }

    @Test
    void calcularRetrospectoDeveZerarSeSemPartidas() {
        Clube  clube = new Clube();
        clube.setId(1L);
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of());

        RetrospectoResponseDto responseDto = clubeService.calcularRetrospecto(1L);

        assertEquals(0, responseDto.getVitorias());
        assertEquals(0, responseDto.getEmpates());
        assertEquals(0, responseDto.getDerrotas());
        assertEquals(0, responseDto.getGolsFeitos());
        assertEquals(0, responseDto.getGolsContra());
    }

    @Test
    void calcularRetrospectoDeveContarVitorias() {
        Clube  clube = new Clube();
        clube.setId(1L);
        Partida p1 = new Partida();
        p1.setClubeCasa(clube); p1.setClubeFora(new Clube()); p1.setGolsCasa(3); p1.setGolsFora(0);
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of(p1));
        RetrospectoResponseDto responseDto = clubeService.calcularRetrospecto(1L);
        assertEquals(1, responseDto.getVitorias());
        assertEquals(3, responseDto.getGolsFeitos());
    }

    @Test
    void calcularRetrospectoDeveContarEmpates() {
        Clube  clube = new Clube();
        clube.setId(1L);
        Partida p1 = new Partida();
        p1.setClubeCasa(clube); p1.setClubeFora(new Clube()); p1.setGolsCasa(1); p1.setGolsFora(1);
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of(p1));
        RetrospectoResponseDto responseDto = clubeService.calcularRetrospecto(1L);
        assertEquals(1, responseDto.getEmpates() );
        assertEquals(1, responseDto.getGolsFeitos());
    }

    @Test
    void calcularRetrospectoDeveContarDerrotas() {
        Clube  clube = new Clube();
        clube.setId(1L);
        Partida p1 = new Partida();
        p1.setClubeCasa(clube); p1.setClubeFora(new Clube()); p1.setGolsCasa(0); p1.setGolsFora(1);
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of(p1));
        RetrospectoResponseDto responseDto = clubeService.calcularRetrospecto(1L);
        assertEquals(1, responseDto.getDerrotas() );
        assertEquals(1, responseDto.getGolsContra());
    }

    @Test
    void calcularRetrospectoSepararMandanteVisitante() {
        Clube  clube = new Clube();
        clube.setId(1L);
        Clube adversario = new Clube();
        adversario.setId(2L);

        Partida p1 = new Partida();
        p1.setClubeCasa(clube); p1.setClubeFora(adversario); p1.setGolsCasa(2); p1.setGolsFora(1);
        Partida p2 = new Partida();
        p2.setClubeCasa(adversario); p2.setClubeFora(clube); p2.setGolsCasa(0); p2.setGolsFora(3);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of(p1, p2));
        RetrospectoResponseDto responseDto = clubeService.calcularRetrospecto(1L);

        assertEquals(2+3, responseDto.getGolsFeitos());
        assertEquals(1+0, responseDto.getGolsContra());
    }

    @Test
    void calcularRetrospectoComTodosOsResultados() {
        Clube  clube = new Clube();
        clube.setId(1L);
        Clube adversario = new Clube();
        adversario.setId(2L);

        Partida vitoria = new Partida();
        vitoria.setClubeCasa(clube); vitoria.setClubeFora(adversario); vitoria.setGolsCasa(1); vitoria.setGolsFora(0);
        Partida empate = new Partida();
        empate.setClubeCasa(clube); empate.setClubeFora(adversario); empate.setGolsCasa(0); empate.setGolsFora(0);
        Partida derrota = new Partida();
        derrota.setClubeCasa(adversario); derrota.setClubeFora(clube); derrota.setGolsCasa(1); derrota.setGolsFora(0);
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of(vitoria, empate, derrota));

        RetrospectoResponseDto responseDto = clubeService.calcularRetrospecto(1L);
        assertEquals(1, responseDto.getVitorias());
        assertEquals(1, responseDto.getEmpates());
        assertEquals(1, responseDto.getDerrotas());
        assertEquals(1+0+0, responseDto.getGolsFeitos());
        assertEquals(0+0+1, responseDto.getGolsContra());
    }

    @Test
    void retrospectoContraAdversariosDeveLancarSeClubeNaoExiste() {
        when(clubeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clubeService.retrospectoContraAdversarios(99L));
    }

    @Test
    void retrospectoContraAdversariosDeveRetornarListaVaziaSeSemPartidas() {
        Clube clube = new Clube(); clube.setId(1L);
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of());

        List<ConfrontoResponseDto> resultado = clubeService.retrospectoContraAdversarios(1L);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void retrospectoContraAdversariosVitorias() {
        Clube clube = new Clube(); clube.setId(1L);
        Clube adversario = new Clube(); adversario.setId(2L); adversario.setNome("Vasco");
        Partida p = new Partida();
        p.setClubeCasa(clube); p.setClubeFora(adversario); p.setGolsCasa(4); p.setGolsFora(0);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of(p));

        List<ConfrontoResponseDto> resultado = clubeService.retrospectoContraAdversarios(1L);
        assertEquals(1, resultado.size());
        ConfrontoResponseDto c = resultado.getFirst();
        assertEquals("Vasco", c.getAdversario());
        assertEquals(1, c.getVitorias());
        assertEquals(0, c.getEmpates());
        assertEquals(0, c.getDerrotas());
        assertEquals(4, c.getGolsFeitos());
        assertEquals(0, c.getGolsContra());
    }

    @Test
    void retrospectoContraAdversariosEmpatesMultiplos() {
        Clube clube = new Clube(); clube.setId(1L);
        Clube adv1 = new Clube(); adv1.setId(2L); adv1.setNome("A");
        Clube adv2 = new Clube(); adv2.setId(3L); adv2.setNome("B");

        Partida p1 = new Partida(); p1.setClubeCasa(clube); p1.setClubeFora(adv1); p1.setGolsCasa(2); p1.setGolsFora(2);
        Partida p2 = new Partida(); p2.setClubeCasa(clube); p2.setClubeFora(adv2); p2.setGolsCasa(1); p2.setGolsFora(1);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of(p1, p2));

        List<ConfrontoResponseDto> resultado = clubeService.retrospectoContraAdversarios(1L);

        assertEquals(2, resultado.size());
        for (ConfrontoResponseDto c : resultado) {
            assertEquals(1, c.getEmpates());
            assertEquals(0, c.getVitorias());
            assertEquals(0, c.getDerrotas());
        }
    }

    @Test
    void retrospectoContraAdversariosDerrotas() {
        Clube clube = new Clube(); clube.setId(1L);
        Clube adversario = new Clube(); adversario.setId(2L); adversario.setNome("Flamengo");
        Partida p = new Partida();
        p.setClubeCasa(clube); p.setClubeFora(adversario); p.setGolsCasa(0); p.setGolsFora(6);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of(p));

        List<ConfrontoResponseDto> resultado = clubeService.retrospectoContraAdversarios(1L);
        assertEquals(1, resultado.size());
        ConfrontoResponseDto c = resultado.getFirst();
        assertEquals("Flamengo", c.getAdversario());
        assertEquals(0, c.getVitorias());
        assertEquals(0, c.getEmpates());
        assertEquals(1, c.getDerrotas());
        assertEquals(0, c.getGolsFeitos());
        assertEquals(6, c.getGolsContra());
    }

    @Test
    void retrospectoContraAdversariosMisto() {
        Clube clube = new Clube(); clube.setId(1L);
        Clube adv = new Clube(); adv.setId(2L); adv.setNome("C");
        Partida v = new Partida(); v.setClubeCasa(clube); v.setClubeFora(adv); v.setGolsCasa(2); v.setGolsFora(0);
        Partida e = new Partida(); e.setClubeCasa(clube); e.setClubeFora(adv); e.setGolsCasa(1); e.setGolsFora(1);
        Partida d = new Partida(); d.setClubeCasa(adv); d.setClubeFora(clube); d.setGolsCasa(3); d.setGolsFora(1);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube)).thenReturn(List.of(v, e, d));

        List<ConfrontoResponseDto> resultado = clubeService.retrospectoContraAdversarios(1L);
        ConfrontoResponseDto c = resultado.getFirst();
        assertEquals(1, c.getVitorias());
        assertEquals(1, c.getEmpates());
        assertEquals(1, c.getDerrotas());
        assertEquals(4, c.getGolsFeitos());
        assertEquals(4, c.getGolsContra());
    }

    @Test
    void retrospectoContraAdversariosMultiplosMandanteEVisitante() {

        Clube clube = new Clube();
        clube.setId(1L);
        clube.setNome("Flamengo");

        Clube adversario1 = new Clube();
        adversario1.setId(2L);
        adversario1.setNome("Vasco");

        Clube adversario2 = new Clube();
        adversario2.setId(3L);
        adversario2.setNome("Corinthians");


        Partida p1 = new Partida();
        p1.setClubeCasa(clube);
        p1.setClubeFora(adversario1);
        p1.setGolsCasa(3);
        p1.setGolsFora(1);


        Partida p2 = new Partida();
        p2.setClubeCasa(adversario2);
        p2.setClubeFora(clube);
        p2.setGolsCasa(2);
        p2.setGolsFora(2);


        Partida p3 = new Partida();
        p3.setClubeCasa(adversario1);
        p3.setClubeFora(clube);
        p3.setGolsCasa(1);
        p3.setGolsFora(0);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clube));
        when(partidaRepository.findByClubeCasaOrClubeFora(clube, clube))
                .thenReturn(List.of(p1, p2, p3));


        List<ConfrontoResponseDto> resultado = clubeService.retrospectoContraAdversarios(1L);


        assertEquals(2, resultado.size());


        ConfrontoResponseDto contraVasco = resultado.stream()
                .filter(r -> r.getAdversario().equals("Vasco"))
                .findFirst()
                .orElseThrow();
        assertEquals(1, contraVasco.getVitorias());
        assertEquals(0, contraVasco.getEmpates());
        assertEquals(1, contraVasco.getDerrotas());
        assertEquals(3, contraVasco.getGolsFeitos());
        assertEquals(1+1, contraVasco.getGolsContra());

        ConfrontoResponseDto contraCorinthians = resultado.stream()
                .filter(r -> r.getAdversario().equals("Corinthians"))
                .findFirst()
                .orElseThrow();
        assertEquals(0, contraCorinthians.getVitorias());
        assertEquals(1, contraCorinthians.getEmpates());
        assertEquals(0, contraCorinthians.getDerrotas());
        assertEquals(2, contraCorinthians.getGolsFeitos());
        assertEquals(2, contraCorinthians.getGolsContra());
    }

    @Test
    void confrontoDiretoDeveLancarSeClubeANaoExiste() {
        when(clubeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clubeService.getConfrontoDireto(1L, 2L));
    }

    @Test
    void confrontoDiretoDeveLancarSeClubeBNaoExiste() {
        Clube clubeA = new Clube(); clubeA.setId(1L);
        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeA));
        when(clubeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clubeService.getConfrontoDireto(1L, 2L));
    }

    @Test
    void confrontoDiretoSemPartidasDeveRetornarZerado() {
        Clube clubeA = new Clube(); clubeA.setId(1L); clubeA.setNome("Flamengo");
        Clube clubeB = new Clube(); clubeB.setId(2L); clubeB.setNome("Vasco");

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeA));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeB));
        when(partidaRepository.findByClubeCasaAndClubeForaOrClubeCasaAndClubeFora(clubeA, clubeB, clubeB, clubeA)).thenReturn(List.of());

        ConfrontoDiretoResponseDto dto = clubeService.getConfrontoDireto(1L, 2L);

        assertNotNull(dto);
        assertEquals(0, dto.getRetrospectoClubeA().getVitorias());
        assertEquals(0, dto.getRetrospectoClubeB().getVitorias());
        assertTrue(dto.getPartidas().isEmpty());
    }

    @Test
    void confrontoDiretoComVitoriaMandante() {
        Clube clubeA = new Clube(); clubeA.setId(1L); clubeA.setNome("A");
        Clube clubeB = new Clube(); clubeB.setId(2L); clubeB.setNome("B");

        Partida partida = new Partida();
        partida.setId(10L);
        partida.setClubeCasa(clubeA);
        partida.setClubeFora(clubeB);
        partida.setGolsCasa(4);
        partida.setGolsFora(1);
        partida.setDataHora(LocalDateTime.now());
        Estadio estadio = new Estadio();
        estadio.setNome("Estadio X");
        partida.setEstadio(estadio);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeA));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeB));
        when(partidaRepository.findByClubeCasaAndClubeForaOrClubeCasaAndClubeFora(clubeA, clubeB, clubeB, clubeA))
                .thenReturn(List.of(partida));

        ConfrontoDiretoResponseDto dto = clubeService.getConfrontoDireto(1L, 2L);

        assertEquals(1, dto.getRetrospectoClubeA().getVitorias());
        assertEquals(0, dto.getRetrospectoClubeB().getVitorias());
        assertEquals(4, dto.getRetrospectoClubeA().getGolsFeitos());
        assertEquals(1, dto.getRetrospectoClubeB().getGolsFeitos());
        assertEquals(1, dto.getPartidas().size());
    }

    @Test
    void confrontoDiretoComVitoriaVisitante() {
        Clube clubeA = new Clube(); clubeA.setId(1L); clubeA.setNome("A");
        Clube clubeB = new Clube(); clubeB.setId(2L); clubeB.setNome("B");

        Partida partida = new Partida();
        partida.setId(11L);
        partida.setClubeCasa(clubeB);
        partida.setClubeFora(clubeA);
        partida.setGolsCasa(0);
        partida.setGolsFora(2);

        partida.setEstadio(new Estadio());

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeA));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeB));
        when(partidaRepository.findByClubeCasaAndClubeForaOrClubeCasaAndClubeFora(clubeA, clubeB, clubeB, clubeA))
                .thenReturn(List.of(partida));

        ConfrontoDiretoResponseDto dto = clubeService.getConfrontoDireto(1L, 2L);

        assertEquals(1, dto.getRetrospectoClubeA().getVitorias());
        assertEquals(0, dto.getRetrospectoClubeB().getVitorias());
        assertEquals(2, dto.getRetrospectoClubeA().getGolsFeitos());
        assertEquals(0, dto.getRetrospectoClubeB().getGolsFeitos());
        assertEquals(1, dto.getPartidas().size());
    }

    @Test
    void confrontoDiretoComEmpate() {
        Clube clubeA = new Clube(); clubeA.setId(1L); clubeA.setNome("A");
        Clube clubeB = new Clube(); clubeB.setId(2L); clubeB.setNome("B");
        Partida partida = new Partida();
        partida.setClubeCasa(clubeA);
        partida.setClubeFora(clubeB);
        partida.setGolsCasa(1);
        partida.setGolsFora(1);
        partida.setEstadio(new Estadio());

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeA));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeB));
        when(partidaRepository.findByClubeCasaAndClubeForaOrClubeCasaAndClubeFora(clubeA, clubeB, clubeB, clubeA)).thenReturn(List.of(partida));

        ConfrontoDiretoResponseDto dto = clubeService.getConfrontoDireto(1L, 2L);

        assertEquals(1, dto.getRetrospectoClubeA().getEmpates());
        assertEquals(1, dto.getRetrospectoClubeB().getEmpates());
        assertEquals(1, dto.getPartidas().size());
    }






}
