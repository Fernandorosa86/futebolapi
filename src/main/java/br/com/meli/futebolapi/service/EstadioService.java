package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.dto.EstadioRequestDto;
import br.com.meli.futebolapi.dto.EstadioResponseDto;
import br.com.meli.futebolapi.model.Estadio;
import br.com.meli.futebolapi.repository.EstadioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class EstadioService {

    private final EstadioRepository estadioRepository;

    public EstadioService(EstadioRepository estadioRepository) {
        this.estadioRepository = estadioRepository;
    }

    public EstadioResponseDto salvar(EstadioRequestDto estadioRequestDto) {
        if(estadioRepository.existsByNome(estadioRequestDto.getNome())) {
            throw new DataIntegrityViolationException("Já existe estadio cadastrado com esse nome.");
        }

        Estadio estadio = new Estadio();
        estadio.setNome(estadioRequestDto.getNome());
        estadio = estadioRepository.save(estadio);

        EstadioResponseDto estadioResponseDto = new EstadioResponseDto();
        estadioResponseDto.setId(estadio.getId());
        estadioResponseDto.setNome(estadio.getNome());

        return estadioResponseDto;
    }
}
