package iqadot.iqadotkit.config.security.auth;

import jakarta.servlet.http.*;
import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.security.core.*;
import org.springframework.security.web.*;
import org.springframework.stereotype.*;

import java.io.*;

/**
 * Overrides default Spring Boot behaviour of returning 403
 * on authentication check errors, instead of 401.
 */
@Component
@Primary
@Slf4j
public class Http401UnauthorizedEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}