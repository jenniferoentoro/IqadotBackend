package iqadot.iqadotkit.controller.domain;

import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateResponseDTO {
    private String firstName;

    private String lastName;

    private String email;

    private String username;
}
