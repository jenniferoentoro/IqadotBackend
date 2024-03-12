package iqadot.iqadotkit.business.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Setter
@Getter
public class CustomException extends ResponseStatusException {

    private final String errorMessage;

    public CustomException(String errorMessage) {
        super(HttpStatus.BAD_REQUEST, errorMessage);
        this.errorMessage = errorMessage;
    }
}
