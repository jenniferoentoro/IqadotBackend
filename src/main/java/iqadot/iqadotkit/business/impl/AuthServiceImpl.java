package iqadot.iqadotkit.business.impl;

import iqadot.iqadotkit.business.*;
import iqadot.iqadotkit.business.exception.*;
import iqadot.iqadotkit.config.security.token.*;
import iqadot.iqadotkit.config.security.token.impl.*;
import iqadot.iqadotkit.controller.domain.*;
import iqadot.iqadotkit.persistence.*;
import iqadot.iqadotkit.persistence.entity.*;
import lombok.*;
import org.modelmapper.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AccessTokenEncoder accessTokenEncoder;
    private final PasswordEncoder passwordEncoder;
    @Override
    public LoginResp login(LoginReq loginRequest) {
        UserEntity user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            throw new InvalidCredentialsException();
        }

        if (!matchesPassword(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = generateAccessToken(user);
        return LoginResp.builder().accessToken(accessToken).build();
    }

    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }



    private String generateAccessToken(UserEntity user){
        long userId= user.getUserId();
        Long  userIdOrNull = (userId != 0)?(long) userId:null;
        return accessTokenEncoder.encode(new AccessTokenImpl(user.getEmail(),userIdOrNull));
    }
}
