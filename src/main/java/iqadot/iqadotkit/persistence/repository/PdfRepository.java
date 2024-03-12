package iqadot.iqadotkit.persistence.repository;

import iqadot.iqadotkit.persistence.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PdfRepository extends JpaRepository<Article, Long> {

}
