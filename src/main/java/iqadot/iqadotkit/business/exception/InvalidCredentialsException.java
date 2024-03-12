package iqadot.iqadotkit.business.exception;

import org.springframework.http.*;
import org.springframework.web.server.*;

public class InvalidCredentialsException extends ResponseStatusException {
    public InvalidCredentialsException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS");
    }
}