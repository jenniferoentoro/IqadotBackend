package iqadot.iqadotkit.business.impl;

import iqadot.iqadotkit.controller.domain.UserCreateReq;
import iqadot.iqadotkit.controller.domain.UserCreateResponseDTO;
import iqadot.iqadotkit.persistence.UserRepository;
import iqadot.iqadotkit.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {UserServiceImpl.class})
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createUser_Successful() {
        UserCreateReq request = new UserCreateReq();
        request.setUsername("testuser");
        request.setPassword("password");

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testuser");
        userEntity.setPassword("encodedpassword");

        UserCreateResponseDTO responseDTO = new UserCreateResponseDTO();
        responseDTO.setUsername("testuser");

        when(modelMapper.map(request, UserEntity.class)).thenReturn(userEntity);
        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(modelMapper.map(userEntity, UserCreateResponseDTO.class)).thenReturn(responseDTO);

        UserCreateResponseDTO createdUser = userService.createUser(request);

        assertEquals("testuser", createdUser.getUsername());
    }

    @Test
    void getAllUsers_Successful() {
        List<UserEntity> userEntities = new ArrayList<>();
        UserEntity user1 = new UserEntity();
        user1.setUsername("user1");
        userEntities.add(user1);

        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");
        userEntities.add(user2);

        List<UserCreateResponseDTO> responseDTOs = new ArrayList<>();
        UserCreateResponseDTO response1 = new UserCreateResponseDTO();
        response1.setUsername("user1");
        responseDTOs.add(response1);

        UserCreateResponseDTO response2 = new UserCreateResponseDTO();
        response2.setUsername("user2");
        responseDTOs.add(response2);

        when(userRepository.findAll()).thenReturn(userEntities);
        when(modelMapper.map(user1, UserCreateResponseDTO.class)).thenReturn(response1);
        when(modelMapper.map(user2, UserCreateResponseDTO.class)).thenReturn(response2);

        List<UserCreateResponseDTO> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUsername());
        assertEquals("user2", users.get(1).getUsername());
    }
}
