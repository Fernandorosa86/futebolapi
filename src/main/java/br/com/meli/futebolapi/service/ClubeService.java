package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.model.Clube;
import br.com.meli.futebolapi.dto.ClubeRequestDto;
import br.com.meli.futebolapi.dto.ClubeResponseDto;
import br.com.meli.futebolapi.repository.ClubeRepository;
import org.springframework.stereotype.Service;




@Service
public class ClubeService {

    private ClubeRepository clubeRepository;

    public ClubeService(ClubeRepository clubeRepository) {
        this.clubeRepository = clubeRepository;
    }

    public ClubeResponseDto salvar(ClubeRequestDto clubeRequestDto) throws Exception {
        if(clubeRepository.existsByNomeAndEstado(clubeRequestDto.getNome(), clubeRequestDto.getEstado())) {
            throw new Exception("Clube já existe");
        }

        Clube clube = new Clube();
        clube.setNome(clubeRequestDto.getNome());
        clube.setEstado(clubeRequestDto.getEstado());
        clube.setDataCriacao(clubeRequestDto.getDataCriacao());
        clube.setStatus(clubeRequestDto.getStatus());

        clube = clubeRepository.save(clube);

        ClubeResponseDto clubeResponseDto = new ClubeResponseDto();
        clubeResponseDto.setId(clube.getId());
        clubeResponseDto.setNome(clube.getNome());
        clubeResponseDto.setEstado(clube.getEstado());
        clubeResponseDto.setDataCriacao(clube.getDataCriacao());
        clubeResponseDto.setStatus(clube.getStatus());

        return clubeResponseDto;
    }
}
