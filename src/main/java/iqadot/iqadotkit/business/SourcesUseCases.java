package iqadot.iqadotkit.business;

import iqadot.iqadotkit.controller.domain.SourcesDTO;
import iqadot.iqadotkit.persistence.entity.Sources;

import java.util.List;

public interface SourcesUseCases {

    Sources createSource(Sources request);

    List<Sources> getAllSources();

    Sources updateSource(Sources request, Long id);

    void deleteSource(Long id);
}
