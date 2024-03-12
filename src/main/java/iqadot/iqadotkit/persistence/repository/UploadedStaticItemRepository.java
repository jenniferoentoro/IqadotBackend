package iqadot.iqadotkit.persistence.repository;

import iqadot.iqadotkit.persistence.entity.UploadedStaticItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedStaticItemRepository extends JpaRepository<UploadedStaticItem, Long> {
    //findAllByArticleGuidIn

    List<UploadedStaticItem> findAllByStaticItemGuidIn(List<String> articleGuid);

    UploadedStaticItem findByStaticItemGuid(String staticItemGuid);
}
