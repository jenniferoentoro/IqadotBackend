package iqadot.iqadotkit.persistence.repository;

import iqadot.iqadotkit.persistence.entity.UploadedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedArticleRepository extends JpaRepository<UploadedArticle,Long> {

    UploadedArticle findByArticleGuid(String articleGuid);

    List<UploadedArticle> findAllByArticleGuidIn(List<String> articleGuid);
}
