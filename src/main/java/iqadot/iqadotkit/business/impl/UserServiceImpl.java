package iqadot.iqadotkit.business.impl;

import iqadot.iqadotkit.business.*;
import iqadot.iqadotkit.controller.domain.UserCreateReq;
import iqadot.iqadotkit.controller.domain.UserCreateResponseDTO;
import iqadot.iqadotkit.persistence.*;
import iqadot.iqadotkit.persistence.entity.*;
import lombok.*;
import org.modelmapper.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserCreateResponseDTO createUser(UserCreateReq request){
        UserEntity userEntity = modelMapper.map(request, UserEntity.class);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        UserEntity savedUserEntity = userRepository.save(userEntity);
        return modelMapper.map(savedUserEntity, UserCreateResponseDTO.class);
    }

    @Override
    public List<UserCreateResponseDTO> getAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();

        List<UserCreateResponseDTO> userCreateResponseDTOS = new ArrayList<>();

        userEntities.forEach(
                userEntity -> {
                    UserCreateResponseDTO userCreateResponseDTO = modelMapper.map(userEntity, UserCreateResponseDTO.class);
                    userCreateResponseDTOS.add(userCreateResponseDTO);
                }
        );

        return userCreateResponseDTOS;
    }


}
