package iqadot.iqadotkit.controller.domain;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginReq {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
