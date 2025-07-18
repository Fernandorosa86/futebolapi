package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.dto.*;
import br.com.meli.futebolapi.entity.Clube;
import br.com.meli.futebolapi.entity.Partida;
import br.com.meli.futebolapi.exception.NotFoundException;
import br.com.meli.futebolapi.repository.ClubeRepository;
import br.com.meli.futebolapi.repository.PartidaRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ClubeService {

    private final ClubeRepository clubeRepository;
    private final PartidaRepository partidaRepository;

    //private final PartidaRepository partidaRepository;
    private Clube fromRequestDto(ClubeRequestDto clubeRequestDto) {
        Clube clube = new Clube();
        clube.setNome(clubeRequestDto.getNome());
        clube.setEstado(clubeRequestDto.getEstado());
        clube.setDataCriacao(clubeRequestDto.getDataCriacao());
        clube.setStatus(clubeRequestDto.getStatus());

        return clube;
    }

    private ClubeResponseDto toClubeResponseDto(Clube clube) {
        ClubeResponseDto clubeResponseDto = new ClubeResponseDto();

        clubeResponseDto.setId(clube.getId());
        clubeResponseDto.setNome(clube.getNome());
        clubeResponseDto.setEstado(clube.getEstado());
        clubeResponseDto.setDataCriacao(clube.getDataCriacao());
        clubeResponseDto.setStatus(clube.getStatus());

        return clubeResponseDto;
    }

    public ClubeService(ClubeRepository clubeRepository, /*, PartidaRepository partidaRepository */PartidaRepository partidaRepository) {
        this.clubeRepository = clubeRepository;
        // this.partidaRepository = partidaRepository;
        this.partidaRepository = partidaRepository;
    }

    public ClubeResponseDto salvar(@NotNull ClubeRequestDto clubeRequestDto) {
        if (clubeRepository.existsByNomeAndEstado(clubeRequestDto.getNome(), clubeRequestDto.getEstado())) {
            throw new DataIntegrityViolationException("Clube já existe");
        }


        Clube clube = fromRequestDto(clubeRequestDto);
        clube = clubeRepository.save(clube);

        return toClubeResponseDto(clube);
    }

    public ClubeResponseDto editar(Long id, @NotNull ClubeRequestDto clubeRequestDto)  {
        Clube clube = clubeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado"));


        boolean doisClubesMesmoNomeEstado =
                clubeRepository.existsByNomeAndEstado(clubeRequestDto.getNome(), clubeRequestDto.getEstado()) &&
                        !(clube.getNome().equals(clubeRequestDto.getNome()) && clube.getEstado().equals(clubeRequestDto.getEstado()));
        if(doisClubesMesmoNomeEstado) {
            throw new DataIntegrityViolationException("Já existe um clube com este nome");
        }


        clube.setNome(clubeRequestDto.getNome());
        clube.setEstado(clubeRequestDto.getEstado());
        clube.setDataCriacao(clubeRequestDto.getDataCriacao());
        clube.setStatus(clubeRequestDto.getStatus());

        clube = clubeRepository.save(clube);

        return toClubeResponseDto(clube);

    }

    public void inativarClube(Long id) {
        Clube clube = clubeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado"));
        clube.setStatus(false);

        clubeRepository.save(clube);
    }

    public ClubeResponseDto buscarPorId(Long id) {
        Clube clube = clubeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado"));

        return toClubeResponseDto(clube);
    }

    public Page<ClubeResponseDto> listarClubes(
            String nome, String estado, Boolean status, int page, int size,
            String ordenarPor, String direcao) {

        Sort.Direction direction = "desc".equalsIgnoreCase(direcao) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, ordenarPor));

        if (nome != null && estado != null && status != null) {
            return clubeRepository.findByNomeContainingIgnoreCaseAndEstadoAndStatus(nome, estado, status, pageable)
                    .map(this::toClubeResponseDto);
        }

        if (nome != null && estado != null) {
            return clubeRepository.findByNomeContainsIgnoreCaseAndEstado(nome, estado, pageable)
                    .map(this::toClubeResponseDto);
        }

        if (nome != null) {
            return clubeRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(this::toClubeResponseDto);
        }
        if (estado != null) {
            return clubeRepository.findByEstado(estado, pageable)
                    .map(this::toClubeResponseDto);
        }
        if (status != null) {
            return clubeRepository.findByStatus(status, pageable)
                    .map(this::toClubeResponseDto);
        }
        return clubeRepository.findAll(pageable)
                .map(this::toClubeResponseDto);

    }

    public RetrospectoResponseDto calcularRetrospecto(Long clubeId) {
        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado."));

        List<Partida> partidas = partidaRepository.findByClubeCasaOrClubeFora(clube, clube);

        int vitorias = 0, empates = 0, derrotas = 0, golsFeitos = 0, golsContra = 0;

        for (Partida partida : partidas) {
            boolean ehCasa = partida.getClubeCasa().getId().equals(clubeId);

            int golsClube, golsAdversario;
            if (ehCasa) {
                golsClube = partida.getGolsCasa();
                golsAdversario = partida.getGolsFora();
            }else{
                golsClube = partida.getGolsFora();
                golsAdversario = partida.getGolsCasa();
            }
            golsFeitos += golsClube;
            golsContra += golsAdversario;

            if (golsClube > golsAdversario){
                vitorias++;
            }else if (golsClube == golsAdversario){
                empates++;
            }else {
                derrotas++;
            }
        }

        RetrospectoResponseDto retrospectoResponseDto = new RetrospectoResponseDto();

        retrospectoResponseDto.setVitorias(vitorias);
        retrospectoResponseDto.setEmpates(empates);
        retrospectoResponseDto.setDerrotas(derrotas);
        retrospectoResponseDto.setGolsFeitos(golsFeitos);
        retrospectoResponseDto.setGolsContra(golsContra);

        return retrospectoResponseDto;
    }

    public List<ConfrontoResponseDto> retrospectoContraAdversarios(Long clubeId) {
        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado."));

        List<Partida> partidas = partidaRepository.findByClubeCasaOrClubeFora(clube, clube);

        Map<Long, ConfrontoResponseDto> mapa = new HashMap<>();

        for (Partida partida : partidas) {
            boolean ehCasa = partida.getClubeCasa().getId().equals(clubeId);
            Clube adversario = ehCasa ? partida.getClubeFora() : partida.getClubeCasa();

            ConfrontoResponseDto confrontoResponseDto = mapa.getOrDefault(adversario.getId(), new ConfrontoResponseDto());
            confrontoResponseDto.setAdversario(adversario.getNome());

            int golsClube = ehCasa ? partida.getGolsCasa() : partida.getGolsFora();
            int golsAdversario = ehCasa ? partida.getGolsFora() : partida.getGolsCasa();

            confrontoResponseDto.setGolsFeitos(confrontoResponseDto.getGolsFeitos() + golsClube);
            confrontoResponseDto.setGolsContra(confrontoResponseDto.getGolsContra() + golsAdversario);

            if (golsClube > golsAdversario) {
                confrontoResponseDto.setVitorias(confrontoResponseDto.getVitorias() + 1);
            }else if (golsClube == golsAdversario){
                confrontoResponseDto.setEmpates(confrontoResponseDto.getEmpates() + 1);
            }else{
                confrontoResponseDto.setDerrotas(confrontoResponseDto.getDerrotas() + 1);
            }

            mapa.put(adversario.getId(), confrontoResponseDto);
        }

        return new ArrayList<>(mapa.values());
    }

    public ConfrontoDiretoResponseDto getConfrontoDireto(Long clubeAId, Long clubeBId) {
        Clube clubeA = clubeRepository.findById(clubeAId)
                .orElseThrow(() -> new NotFoundException("Clube A não encontrado."));
        Clube clubeB = clubeRepository.findById(clubeBId)
                .orElseThrow(() -> new NotFoundException("Clube B não encontrado."));

        List<Partida> partidas = partidaRepository.findByClubeCasaAndClubeForaOrClubeCasaAndClubeFora(clubeA, clubeB, clubeB, clubeA);

        RetrospectoSimplesDto retroA = new RetrospectoSimplesDto();
        retroA.setClubeId(clubeA.getId());
        retroA.setClubeNome(clubeA.getNome());

        RetrospectoSimplesDto retroB = new RetrospectoSimplesDto();
        retroB.setClubeId(clubeB.getId());
        retroB.setClubeNome(clubeB.getNome());

        List<PartidaSimplesDto> partidaSimplesDto = new ArrayList<>();

        for (Partida partida : partidas) {
            boolean ehCasa = partida.getClubeCasa().getId().equals(clubeAId);

            int golsA = ehCasa ? partida.getGolsCasa() : partida.getGolsFora();
            int golsB = ehCasa ? partida.getGolsFora() : partida.getGolsCasa();

            retroA.setGolsFeitos(retroA.getGolsFeitos() + golsA);
            retroA.setGolsContra(retroA.getGolsContra() + golsB);

            retroB.setGolsFeitos(retroB.getGolsFeitos() + golsB);
            retroB.setGolsContra(retroB.getGolsContra() + golsA);

            if (golsA > golsB) {
                retroA.setVitorias(retroA.getVitorias() + 1);
                retroB.setDerrotas(retroB.getDerrotas() + 1);
            }else if (golsA < golsB){
                retroB.setVitorias(retroB.getVitorias() + 1);
                retroA.setDerrotas(retroA.getDerrotas() + 1);
            }else {
                retroA.setEmpates(retroA.getEmpates() + 1);
                retroB.setEmpates(retroB.getEmpates() + 1);
            }

            PartidaSimplesDto pSimplesDto = new PartidaSimplesDto();

            pSimplesDto.setId(partida.getId());
            pSimplesDto.setDataHora(partida.getDataHora());
            pSimplesDto.setClubeCasa(partida.getClubeCasa().getNome());
            pSimplesDto.setClubeFora(partida.getClubeFora().getNome());
            pSimplesDto.setGolsCasa(partida.getGolsCasa());
            pSimplesDto.setGolsFora(partida.getGolsFora());
            pSimplesDto.setEstadio(partida.getEstadio().getNome());
            partidaSimplesDto.add(pSimplesDto);
        }

        ConfrontoDiretoResponseDto confrontoDiretoResponseDto = new ConfrontoDiretoResponseDto();
        confrontoDiretoResponseDto.setRetrospectoClubeA(retroA);
        confrontoDiretoResponseDto.setRetrospectoClubeB(retroB);
        confrontoDiretoResponseDto.setPartidas(partidaSimplesDto);

        return confrontoDiretoResponseDto;
    }


}
