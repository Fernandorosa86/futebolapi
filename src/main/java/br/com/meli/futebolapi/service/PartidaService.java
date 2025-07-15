package br.com.meli.futebolapi.service;


import br.com.meli.futebolapi.dto.PartidaRequestDto;
import br.com.meli.futebolapi.dto.PartidaResponseDto;
import br.com.meli.futebolapi.entity.Clube;
import br.com.meli.futebolapi.entity.Estadio;
import br.com.meli.futebolapi.entity.Partida;
import br.com.meli.futebolapi.repository.ClubeRepository;
import br.com.meli.futebolapi.repository.EstadioRepository;
import br.com.meli.futebolapi.repository.PartidaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final ClubeRepository clubeRepository;
    private final EstadioRepository estadioRepository;

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

    public PartidaService(PartidaRepository partidaRepository, ClubeRepository clubeRepository, EstadioRepository estadioRepository) {
        this.partidaRepository = partidaRepository;
        this.clubeRepository = clubeRepository;
        this.estadioRepository = estadioRepository;
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
            if(!dataJogo.isBefore(inicio) && !dataJogo.isAfter(fim)) {
                throw new DataIntegrityViolationException("O clube mandante já tem partida marcada a menos de 48 horas desta.");
            }
        }

        for (Partida partida : partidasFora) {
            LocalDateTime dataJogo = partida.getDataHora();
            if(!dataJogo.isBefore(inicio) && !dataJogo.isAfter(fim)) {
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


}
