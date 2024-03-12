package iqadot.iqadotkit.controller.domain;

import iqadot.iqadotkit.persistence.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedArticleReponse {
    @Column(nullable = false)
    private String articleGuid;

    private UserCreateResponseDTO adminId;

    @Column(nullable = false)
    private String pathFile;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Date dateCreated;

    private Boolean publish;

    private Boolean isDeleted;
}
