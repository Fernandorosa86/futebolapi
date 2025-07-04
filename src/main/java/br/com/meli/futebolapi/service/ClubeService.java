package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.model.Clube;
import br.com.meli.futebolapi.dto.ClubeRequestDto;
import br.com.meli.futebolapi.dto.ClubeResponseDto;
import br.com.meli.futebolapi.repository.ClubeRepository;
// import br.com.meli.futebolapi.repository.PartidaRepository;
import br.com.meli.futebolapi.exception.NotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;




@Service
public class ClubeService {

    private final ClubeRepository clubeRepository;

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

    public ClubeService(ClubeRepository clubeRepository /*, PartidaRepository partidaRepository */) {
        this.clubeRepository = clubeRepository;
        // this.partidaRepository = partidaRepository;
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
                clubeRepository.existsByNomeAndEstado(clubeRequestDto.getNome(), clubeRequestDto.getEstado())
                        && !(clube.getNome().equals(clubeRequestDto.getNome())) && clube.getEstado().equals(clubeRequestDto.getEstado());
        if(doisClubesMesmoNomeEstado) {
            throw new DataIntegrityViolationException("Já existe um clube com este nome");
        }


        // if (partidaRepository.existsByClubeAndDataAfter(clube, dto.getDataCriacao())) {
        //     throw new DataIntegrityViolationException("Não pode definir data de criação posterior a uma partida já realizada por esse clube");
        // }

        fromRequestDto(clubeRequestDto);

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
}
