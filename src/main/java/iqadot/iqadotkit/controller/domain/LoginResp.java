package iqadot.iqadotkit.controller.domain;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResp {
    private String accessToken;
}
