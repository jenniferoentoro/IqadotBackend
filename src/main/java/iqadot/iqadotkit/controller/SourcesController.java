package iqadot.iqadotkit.controller;

import iqadot.iqadotkit.business.SourcesUseCases;
import iqadot.iqadotkit.controller.domain.SourcesDTO;
import iqadot.iqadotkit.persistence.entity.AuthType;
import iqadot.iqadotkit.persistence.entity.Method;
import iqadot.iqadotkit.persistence.entity.Sources;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/",allowedHeaders = "*")
public class SourcesController {
    private final SourcesUseCases sourcesUseCases;
    private final ModelMapper modelMapper;

    @GetMapping("/get-all-sources")
    public ResponseEntity<List<Sources>> getAllSources() {
        return ResponseEntity.ok(sourcesUseCases.getAllSources());
    }

    @PostMapping
    public ResponseEntity<Sources> createSource(@RequestBody SourcesDTO request) {

        Method method = Method.GET;
        if (!Objects.equals(request.getMethod(), "GET")) {
            method = Method.POST;

        }

        AuthType authType = AuthType.NONE;

        if (Objects.equals(request.getAuthType(), "BASIC")) {
            authType = AuthType.BASIC;
        } else if (Objects.equals(request.getAuthType(), "BEARER")) {
            authType = AuthType.BEARER;
        } else if (Objects.equals(request.getAuthType(), "API_KEY")) {
            authType = AuthType.API_KEY;
        }
        Sources sources = Sources.builder().name(request.getName()).url(request.getUrl()).method(method).authBody(request.getAuthBody()).body(request.getBody()).authType(authType).header(request.getHeader()).build();
        return ResponseEntity.ok(sourcesUseCases.createSource(sources));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSource(@PathVariable Long id) {
        sourcesUseCases.deleteSource(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sources> updateSource(@RequestBody SourcesDTO request, @PathVariable Long id) {

        Method method = Method.GET;
        if (!Objects.equals(request.getMethod(), "GET")) {
            method = Method.POST;

        }

        AuthType authType = AuthType.NONE;

        if (Objects.equals(request.getAuthType(), "BASIC")) {
            authType = AuthType.BASIC;
        } else if (Objects.equals(request.getAuthType(), "BEARER")) {
            authType = AuthType.BEARER;
        } else if (Objects.equals(request.getAuthType(), "API_KEY")) {
            authType = AuthType.API_KEY;
        }
        Sources sources = Sources.builder().name(request.getName()).url(request.getUrl()).method(method).authBody(request.getAuthBody()).body(request.getBody()).authType(authType).header(request.getHeader()).build();
        return ResponseEntity.ok(sourcesUseCases.updateSource(sources, id));
    }
}
