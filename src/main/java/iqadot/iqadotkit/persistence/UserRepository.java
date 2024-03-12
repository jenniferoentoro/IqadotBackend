package iqadot.iqadotkit.persistence;

import iqadot.iqadotkit.persistence.entity.*;
import org.springframework.data.jpa.repository.*;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    UserEntity findByEmail(String email);
}
