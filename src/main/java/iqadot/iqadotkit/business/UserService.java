package iqadot.iqadotkit.business;

import iqadot.iqadotkit.controller.domain.UserCreateReq;
import iqadot.iqadotkit.controller.domain.UserCreateResponseDTO;
import iqadot.iqadotkit.persistence.entity.*;

import java.util.List;

public interface UserService {
    UserCreateResponseDTO createUser(UserCreateReq request);

    List<UserCreateResponseDTO> getAllUsers();
}
