package iqadot.iqadotkit.config.security;

import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.*;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder createBCryptPasswordEncoder() { return new BCryptPasswordEncoder(); }
}
