package iqadot.iqadotkit.controller.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedStaticItemResponse {
    @Column(nullable = false)
    private String staticItemGuid;

    private UserCreateResponseDTO adminId;

    private String pathFile;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private Date dateCreated;

    private Boolean publish;

    private Boolean isDeleted;
}
