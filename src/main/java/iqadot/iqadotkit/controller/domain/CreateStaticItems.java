package iqadot.iqadotkit.controller.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateStaticItems {
    private String subject;

    private String body;

    private String answer;

    @NotNull(message = "Channel is required")
    private String channel;
    private String author;
    private String tags;
    private String remarks;
    @NotNull(message = "Publish is required")
    private boolean publish;

}
