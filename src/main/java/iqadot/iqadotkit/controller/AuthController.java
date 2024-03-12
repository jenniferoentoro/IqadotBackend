package iqadot.iqadotkit.controller;

import iqadot.iqadotkit.business.*;
import iqadot.iqadotkit.controller.domain.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/",allowedHeaders = "*")
public class AuthController {
    private AuthService authService;

    @PostMapping(value = {"login"})
    public ResponseEntity<LoginResp> login(@RequestBody @Valid LoginReq loginRequest) {
        LoginResp loginResponse = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }
}
