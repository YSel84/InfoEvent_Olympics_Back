package com.ieolympicstickets.backend.controller;

import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.security.JwtService;
import com.ieolympicstickets.backend.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;


    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    //Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        //Identification
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        //Roles
        List<String> roles = auth.getAuthorities().stream()
                .map(granted -> granted.getAuthority())
                .collect(Collectors.toList());

        //Token emission via JwtService
        String token = jwtService.generateToken(req.email(), roles);
        return  ResponseEntity.ok(new AuthResponse(token));
    }

    //Register
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register (
            @RequestBody RegisterRequest dto) {
        // create user entity
        User u = new User();
        u.setEmail(dto.email);
        u.setFirstName(dto.firstName);
        u.setLastName(dto.lastName);
        u.setDateOfBirth(dto.dateOfBirth());
        //password hash
        User created = userService.register(u, dto.password());

        return ResponseEntity.ok(new RegisterResponse(created.getId(), created.getEmail()));
    }

    // Profile
    @GetMapping("/me")
    public ResponseEntity<UserProfile> me(Authentication auth) {
        //getname = email
        User u = userService.findUserByEmail(auth.getName());
        //DTO without exposing password hash
        UserProfile profile = new UserProfile(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getDateOfBirth(),
                u.getRole().name()
        );
        return ResponseEntity.ok(profile);
    }

    //DTO
    public static record AuthRequest(String email, String password) {}

    public static record AuthResponse(String token) {}

    public static record RegisterRequest(
            String email,
            String firstName,
            String lastName,
            java.time.LocalDate dateOfBirth,
            String password
    ) {}

    public static record RegisterResponse (Long id, String email) {}

    public static record UserProfile(
            Long id,
            String email,
            String firstName,
            String lastName,
            java.time.LocalDate dateOfBirth,
            String role
    ) {}
}
