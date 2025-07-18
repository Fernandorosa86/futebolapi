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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final ClubeRepository clubeRepository;
    private final EstadioRepository estadioRepository;

    public PartidaService(PartidaRepository partidaRepository, ClubeRepository clubeRepository, EstadioRepository estadioRepository) {
        this.partidaRepository = partidaRepository;
        this.clubeRepository = clubeRepository;
        this.estadioRepository = estadioRepository;
    }

    private PartidaResponseDto toPartidaResponseDto(Partida partida) {
        PartidaResponseDto partidaResponseDto = new PartidaResponseDto();
        partidaResponseDto.setId(partida.getId());
        partidaResponseDto.setClubeCasa(partida.getClubeCasa().getNome());
        partidaResponseDto.setClubeFora(partida.getClubeFora().getNome());
        partidaResponseDto.setEstadio(partida.getEstadio().getNome());
        partidaResponseDto.setGolsCasa(partida.getGolsCasa());
        partidaResponseDto.setGolsFora(partida.getGolsFora());
        partidaResponseDto.setDataHora(partida.getDataHora());
        return partidaResponseDto;
    }


    public PartidaResponseDto cadastrarPartida(PartidaRequestDto partidaRequestDto) {
        if (partidaRequestDto.getClubeCasaId().equals(partidaRequestDto.getClubeForaId())) {
            throw new DataIntegrityViolationException("Clubes não podem ser iguais!");
        }

        Clube clubeCasa = clubeRepository.findById(partidaRequestDto.getClubeCasaId())
                .orElseThrow(() -> new DataIntegrityViolationException("Clube casa não existe!"));
        Clube clubeFora = clubeRepository.findById(partidaRequestDto.getClubeForaId())
                .orElseThrow(() -> new DataIntegrityViolationException("Clube fora não existe!"));
        Estadio estadio = estadioRepository.findById(partidaRequestDto.getEstadioId())
                .orElseThrow(() -> new DataIntegrityViolationException("Estádio não existe!"));
        if (!clubeCasa.getStatus() || !clubeFora.getStatus()) {
            throw new DataIntegrityViolationException("Clube inativo!");
        }

        if (partidaRequestDto.getDataHora().isBefore(clubeCasa.getDataCriacao().atStartOfDay()) ||
                partidaRequestDto.getDataHora().isBefore(clubeFora.getDataCriacao().atStartOfDay())) {
            throw new DataIntegrityViolationException("Data da partida não pode ser anterior a data de criação de um dos clubes.");

        }

        LocalDateTime inicio = partidaRequestDto.getDataHora().minusHours(48);
        LocalDateTime fim = partidaRequestDto.getDataHora().plusHours(48);

        List<Partida> partidasCasa = partidaRepository.findByClubeCasaOrClubeFora(clubeCasa, clubeCasa);
        List<Partida> partidasFora = partidaRepository.findByClubeCasaOrClubeFora(clubeFora, clubeFora);

        for (Partida partida : partidasCasa) {
            LocalDateTime dataJogo = partida.getDataHora();
            if (!dataJogo.isBefore(inicio) && !dataJogo.isAfter(fim)) {
                throw new DataIntegrityViolationException("O clube mandante já tem partida marcada a menos de 48 horas desta.");
            }
        }

        for (Partida partida : partidasFora) {
            LocalDateTime dataJogo = partida.getDataHora();
            if (!dataJogo.isBefore(inicio) && !dataJogo.isAfter(fim)) {
                throw new DataIntegrityViolationException("O clube visitante já tem partida marcada a menos de 48 horas desta.");
            }

        }
        LocalDateTime inicioDoDia = partidaRequestDto.getDataHora().toLocalDate().atStartOfDay();
        LocalDateTime fimDoDia = inicioDoDia.plusDays(1);

        boolean estadioOcupado = !partidaRepository
                .findByEstadioAndDataHoraBetween(estadio, inicioDoDia, fimDoDia)
                .isEmpty();
        if (estadioOcupado) {
            throw new DataIntegrityViolationException("Estadio já possui partida registrada para o mesmo dia!");
        }

        Partida partida = new Partida();
        partida.setClubeCasa(clubeCasa);
        partida.setClubeFora(clubeFora);
        partida.setEstadio(estadio);
        partida.setGolsCasa(partidaRequestDto.getGolsCasa());
        partida.setGolsFora(partidaRequestDto.getGolsFora());
        partida.setDataHora(partidaRequestDto.getDataHora());

        partida = partidaRepository.save(partida);

        return toPartidaResponseDto(partida);

    }

    public PartidaResponseDto editarPartida(Long id, PartidaRequestDto partidaRequestDto) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada!"));

        if (partidaRequestDto.getClubeCasaId().equals(partidaRequestDto.getClubeForaId())) {
            throw new DataIntegrityViolationException("Clubes não podem ser iguais.");
        }

        Clube clubeCasa = clubeRepository.findById(partidaRequestDto.getClubeCasaId())
                .orElseThrow(() -> new DataIntegrityViolationException("Clube da casa não existe."));

        Clube clubeFora = clubeRepository.findById(partidaRequestDto.getClubeForaId())
                .orElseThrow(() -> new DataIntegrityViolationException("Clube visitante não existe"));

        Estadio estadio = estadioRepository.findById(partidaRequestDto.getEstadioId())
                .orElseThrow(() -> new DataIntegrityViolationException("Estádio não existe."));

        if (!clubeCasa.getStatus() || !clubeFora.getStatus()) {
            throw new DataIntegrityViolationException("Clube inativo não pode participar.");
        }

        if (partidaRequestDto.getDataHora().isBefore(clubeCasa.getDataCriacao().atStartOfDay()) ||
                partidaRequestDto.getDataHora().isBefore(clubeFora.getDataCriacao().atStartOfDay())) {
            throw new DataIntegrityViolationException("Data/hora da partida não pode ser anterior à data de criação dos clubes.");
        }

        LocalDateTime inicio = partidaRequestDto.getDataHora().minusHours(48);
        LocalDateTime fim = partidaRequestDto.getDataHora().plusHours(48);

        List<Partida> partidasCasa = partidaRepository.findByClubeCasaOrClubeFora(clubeCasa, clubeCasa);
        for (Partida p : partidasCasa) {
            if (!p.getId().equals(id)) {
                LocalDateTime dataJogo = p.getDataHora();
                if (!dataJogo.isBefore(inicio) && !dataJogo.isAfter(fim)) {
                    throw new DataIntegrityViolationException("Clube casa já possui partida marcada a menos de 48 horas desta.");
                }
            }
        }

        List<Partida> partidasFora = partidaRepository.findByClubeCasaOrClubeFora(clubeFora, clubeFora);
        for (Partida p : partidasFora) {
            if (!p.getId().equals(id)) {
                LocalDateTime dataJogo = partida.getDataHora();
                if (!dataJogo.isBefore(inicio) && !dataJogo.isAfter(fim)) {
                    throw new DataIntegrityViolationException("Clube visitante já possui partida marcada a menos de 48 horas desta.");
                }
            }
        }

        LocalDateTime inicioDia = partidaRequestDto.getDataHora().toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1);

        List<Partida> partidasNoEstadio = partidaRepository.findByEstadioAndDataHoraBetween(estadio, inicioDia, fimDia);

        for (Partida p : partidasNoEstadio) {
            if (!p.getId().equals(id)) {
                throw new DataIntegrityViolationException("Estádio já possui partida cadastrada para esse dia.");
            }
        }

        partida.setClubeCasa(clubeCasa);
        partida.setClubeFora(clubeFora);
        partida.setEstadio(estadio);
        partida.setGolsCasa(partidaRequestDto.getGolsCasa());
        partida.setGolsFora(partidaRequestDto.getGolsCasa());
        partida.setDataHora(partidaRequestDto.getDataHora());

        partida = partidaRepository.save(partida);

        return toPartidaResponseDto(partida);

        //Tentar refatorar esse código, muita coisa repetida.
    }

    public void removerPartida(Long id) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada."));
        partidaRepository.delete(partida);
    }

    public PartidaResponseDto buscarPartidaPorId(Long id) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada."));
        return toPartidaResponseDto(partida);
    }

    public Page<PartidaResponseDto> listarPartidas(
            Long clubeId, Long estadioId, Boolean goleadas, int page, int size, String ordenarPor, String direcao
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(direcao) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, ordenarPor));

        Page<Partida> partidasPage;

        if (clubeId != null && estadioId != null) {
            Clube clube = clubeRepository.findById(clubeId)
                    .orElseThrow(() -> new NotFoundException("Clube não encontrado."));
            Estadio estadio = estadioRepository.findById(estadioId)
                    .orElseThrow(() -> new NotFoundException("Estádio não encontrado"));

            partidasPage = partidaRepository.findByClubeCasaOrClubeForaAndEstadio(clube, clube, estadio, pageable);
        } else if (clubeId != null) {
            Clube clube = clubeRepository.findById(clubeId)
                    .orElseThrow(() -> new NotFoundException("Clube não encontrado."));
            partidasPage = partidaRepository.findByClubeCasaOrClubeFora(clube, clube, pageable);

        } else if (estadioId != null) {
            Estadio estadio = estadioRepository.findById(estadioId)
                    .orElseThrow(() -> new NotFoundException("Estadio não encontrado."));
            partidasPage = partidaRepository.findByEstadio(estadio, pageable);
        } else {
            partidasPage = partidaRepository.findAll(pageable);
        }

        if(goleadas == null || !goleadas) {
            return partidasPage
                    .map(this::toPartidaResponseDto);
        }

        List<Partida> listaGoleadas = partidasPage.getContent().stream()
                .filter(partida -> Math.abs(partida.getGolsCasa() - partida.getGolsFora()) >=3)
                .toList();

        int start = Math.min(page * size, listaGoleadas.size());
        int end = Math.min(start + size, listaGoleadas.size());

        List<PartidaResponseDto> pageGoleadas = listaGoleadas.subList(start, end).stream()
                .map(this::toPartidaResponseDto)
                .toList();

        return new PageImpl<>(pageGoleadas, pageable, listaGoleadas.size());

    }

}
