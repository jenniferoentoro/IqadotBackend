package iqadot.iqadotkit.config.security.token.exception;

import org.springframework.http.*;
import org.springframework.web.server.*;

public class InvalidAccessTokenException extends ResponseStatusException {
    public InvalidAccessTokenException(String errorCause) {
        super(HttpStatus.UNAUTHORIZED, errorCause);
    }
}
