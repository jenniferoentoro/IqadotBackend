package iqadot.iqadotkit.controller;

import iqadot.iqadotkit.business.*;
import iqadot.iqadotkit.controller.domain.*;
import iqadot.iqadotkit.persistence.entity.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173/",allowedHeaders = "*")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserCreateResponseDTO> createUser(@RequestBody @Valid UserCreateReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    public ResponseEntity<List<UserCreateResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
