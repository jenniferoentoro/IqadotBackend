package iqadot.iqadotkit.controller.domain;

import iqadot.iqadotkit.persistence.entity.AuthType;
import iqadot.iqadotkit.persistence.entity.Method;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourcesDTO {
    private String name;

    private String url;

    private String header;

    private String method;

    private String body;

    private String authType;

    private String authBody;


}
