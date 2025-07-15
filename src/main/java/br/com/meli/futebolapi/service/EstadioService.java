package br.com.meli.futebolapi.service;

import br.com.meli.futebolapi.dto.EstadioRequestDto;
import br.com.meli.futebolapi.dto.EstadioResponseDto;
import br.com.meli.futebolapi.entity.Estadio;
import br.com.meli.futebolapi.exception.NotFoundException;
import br.com.meli.futebolapi.repository.EstadioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class EstadioService {

    private final EstadioRepository estadioRepository;

    public EstadioService(EstadioRepository estadioRepository) {
        this.estadioRepository = estadioRepository;
    }

    private EstadioResponseDto toEstadioResponseDto(Estadio estadio) {
        EstadioResponseDto estadioResponseDto = new EstadioResponseDto();
        estadioResponseDto.setId(estadio.getId());
        estadioResponseDto.setNome(estadio.getNome());
        return estadioResponseDto;
    }

    public EstadioResponseDto salvar(EstadioRequestDto estadioRequestDto) {
        if(estadioRepository.existsByNome(estadioRequestDto.getNome())) {
            throw new DataIntegrityViolationException("Já existe estádio cadastrado com esse nome.");
        }

        Estadio estadio = new Estadio();
        estadio.setNome(estadioRequestDto.getNome());
        estadio = estadioRepository.save(estadio);

        return toEstadioResponseDto(estadio);
    }

    public EstadioResponseDto atualizar(Long id, EstadioRequestDto estadioRequestDto) {
        Estadio estadio = estadioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estádio não encontrado"));

        Long currentId = estadio.getId();

        estadioRepository.findByNome(estadioRequestDto.getNome())
                .filter(outro -> !outro.getId().equals(currentId))
                .ifPresent(outro -> {
                    throw new DataIntegrityViolationException("Já existe um estádio cadastrado com esse nome.");
                });



        estadio.setNome(estadioRequestDto.getNome());
        estadio = estadioRepository.save(estadio);
        return toEstadioResponseDto(estadio);

    }

    public void excluirEstadio(Long id) {
        Estadio estadio = estadioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estádio não encontrado"));

        estadioRepository.deleteById(id);
    }

    public EstadioResponseDto buscarEstadioPorId(Long id) {
        Estadio estadio = estadioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estádio não encontrado"));

        return toEstadioResponseDto(estadio);
    }

   public Page<EstadioResponseDto> listarEstadios(
           String nome, int page, int size, String ordenarPor, String direcao) {

       Sort.Direction direction = "desc".equalsIgnoreCase(direcao) ? Sort.Direction.DESC : Sort.Direction.ASC;
       Pageable pageable = PageRequest.of(page, size, Sort.by(direction, ordenarPor));

       if (nome != null && !nome.trim().isEmpty()) {
           return estadioRepository.findByNomeContainingIgnoreCase(nome, pageable)
                   .map(this::toEstadioResponseDto);
       }
       return estadioRepository.findAll(pageable)
               .map(this::toEstadioResponseDto);
   }


}
