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
public class CreateArticleCSVExtra {
    @NotNull(message = "CSV File is required")
    private MultipartFile file;
//    @NotNull(message = "Channel is required")
    private String channel;
    private String author;
    private String tags;
    private boolean allow_comments;
    @NotNull(message = "Publish is required")
    private boolean publish;
    @NotNull(message = "Selected Title is required")
    private String title;
    @NotNull(message = "Selected Description is required")
    private String bodyColumns;
    @NotNull(message = "Selected Alignment is required")
    private String alignment;
}
