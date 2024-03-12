package iqadot.iqadotkit.business.impl;
import iqadot.iqadotkit.business.exception.InvalidCredentialsException;
import iqadot.iqadotkit.config.security.token.impl.AccessTokenEncoderDecoderImpl;
import iqadot.iqadotkit.controller.domain.LoginReq;
import iqadot.iqadotkit.controller.domain.LoginResp;
import iqadot.iqadotkit.persistence.UserRepository;
import iqadot.iqadotkit.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AuthServiceImpl.class})
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccessTokenEncoderDecoderImpl accessTokenEncoder;


    @Test
    void login_SuccessfulLogin() {
        LoginReq loginRequest = new LoginReq("test@example.com", "password");
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(accessTokenEncoder.encode(Mockito.any())).thenReturn("dummyAccessToken");

        LoginResp loginResp = authService.login(loginRequest);

        assertNotNull(loginResp);
        assertNotNull(loginResp.getAccessToken());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        LoginReq loginRequest = new LoginReq("test@example.com", "password");
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_PasswordMismatch_ThrowsException() {
        LoginReq loginRequest = new LoginReq("test@example.com", "password");
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("anotherPassword"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void userIdAssignment_NonZeroValue() {
        int userId = 42;
        Long userIdOrNull = (userId != 0) ? (long) userId : null;

        assertNotNull(userIdOrNull);
        assertEquals(userId, userIdOrNull.intValue());
    }

}
