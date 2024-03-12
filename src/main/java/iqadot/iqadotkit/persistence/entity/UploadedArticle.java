package iqadot.iqadotkit.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedArticle implements Serializable {
    private static final Long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String articleGuid;

    @ManyToOne
    private UserEntity adminId;

    @Column(nullable = false)
    private String pathFile;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Date dateCreated;

    private Boolean publish;

    private Boolean isDeleted;

}
