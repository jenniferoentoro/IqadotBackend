package iqadot.iqadotkit.controller.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

    private String id;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String body;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String channel;

    private String author;

    private String tags;

    private boolean allowComments;

    private String pathFile;
}
