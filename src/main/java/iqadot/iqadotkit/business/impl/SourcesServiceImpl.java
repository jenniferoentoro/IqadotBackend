package iqadot.iqadotkit.business.impl;

import iqadot.iqadotkit.business.SourcesUseCases;
import iqadot.iqadotkit.business.exception.CustomException;
import iqadot.iqadotkit.controller.domain.SourcesDTO;
import iqadot.iqadotkit.persistence.entity.Sources;
import iqadot.iqadotkit.persistence.repository.SourcesRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class SourcesServiceImpl implements SourcesUseCases {
    private final SourcesRepository sourcesRepository;

    @Override
    public Sources createSource(Sources request) {
        return sourcesRepository.save(request);
    }

    @Override
    public List<Sources> getAllSources() {
        return sourcesRepository.findAll();
    }

    @Override
    public Sources updateSource(Sources request, Long id) {
        Optional<Sources> sources = sourcesRepository.findById(id);

        if (sources.isEmpty()) {
            throw new CustomException("Source not found");
        }

        sources.get().setName(request.getName());
        sources.get().setUrl(request.getUrl());
        sources.get().setMethod(request.getMethod());
        sources.get().setBody(request.getBody());



        return sourcesRepository.save(sources.get());


    }

    @Override
    public void deleteSource(Long id) {

        Optional<Sources> sources = sourcesRepository.findById(id);

        if (sources.isEmpty()) {
            throw new CustomException("Source not found");
        }

        sourcesRepository.deleteById(id);

    }
}
