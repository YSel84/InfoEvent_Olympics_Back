package com.ieolympicstickets.backend.controller;

import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.security.JwtService;
import com.ieolympicstickets.backend.service.UserService;
import com.ieolympicstickets.backend.model.Role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
// CHANGEMENT : suppression de l’import Swagger @RequestBody pour éviter la collision
import org.springframework.web.bind.annotation.RequestBody;  // CHANGEMENT : importer Spring @RequestBody
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Authentication", description = "API pour l'authentification et l'enregistrement des utilisateurs")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class); // for debug
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

    // Login
    @Operation(
            summary = "Connexion",
            description = "Authentifier un utilisateur et renvoyer un token JWT",
            // CHANGEMENT : utilisation pleinement qualifiée de l’annotation Swagger RequestBody
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = AuthRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Connexion réussie",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Identifiants invalides")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest req  // CHANGEMENT : Spring va binder req.email et req.password
    ) {
        log.debug("Tentative de login pour email={} / password={}", req.email(), req.password());
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        List<String> roles = auth.getAuthorities().stream()
                .map(granted -> granted.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        String token = jwtService.generateToken(req.email(), roles);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // Register
    @Operation(
            summary = "Inscription",
            description = "Créer un nouveau compte utilisateur",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = RegisterRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inscription réussie",
                            content = @Content(schema = @Schema(implementation = RegisterResponse.class)))
            }
    )
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest dto  // CHANGEMENT : Spring @RequestBody pour le DTO
    ) {
        User u = new User();
        u.setEmail(dto.email());
        u.setFirstName(dto.firstName());
        u.setLastName(dto.lastName());
        u.setDateOfBirth(dto.dateOfBirth());
        User created = userService.register(u, dto.password());

        return ResponseEntity.ok(new RegisterResponse(created.getId(), created.getEmail()));
    }

    // Profile
    @Operation(
            summary = "Profil utilisateur",
            description = "Récupérer les informations du compte authentifié",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profil récupéré",
                            content = @Content(schema = @Schema(implementation = UserProfile.class)))
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserProfile> me(Authentication auth) {
        User u = userService.findUserByEmail(auth.getName());
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

    // DTOs
    public static record AuthRequest(
            @Schema(description = "Email de l'utilisateur", example = "user@example.com")
            String email,
            @Schema(description = "Mot de passe de l'utilisateur", example = "P@ssw0rd")
            String password
    ) {}

    public static record AuthResponse(
            @Schema(description = "JWT Bearer Token retourné après authentification")
            String token
    ) {}

    public static record RegisterRequest(
            @Schema(description = "Email de l'utilisateur", example = "user@example.com")
            String email,
            @Schema(description = "Prénom de l'utilisateur", example = "Alice")
            String firstName,
            @Schema(description = "Nom de famille de l'utilisateur", example = "Dupont")
            String lastName,
            @Schema(description = "Date de naissance de l'utilisateur", example = "1990-01-01")
            java.time.LocalDate dateOfBirth,
            @Schema(description = "Mot de passe souhaité", example = "MySecret123")
            String password
    ) {}

    public static record RegisterResponse(
            @Schema(description = "ID du nouvel utilisateur")
            Long id,
            @Schema(description = "Email du nouvel utilisateur")
            String email
    ) {}

    public static record UserProfile(
            @Schema(description = "ID de l'utilisateur")
            Long id,
            @Schema(description = "Email de l'utilisateur")
            String email,
            @Schema(description = "Prénom de l'utilisateur")
            String firstName,
            @Schema(description = "Nom de famille de l'utilisateur")
            String lastName,
            @Schema(description = "Date de naissance de l'utilisateur")
            java.time.LocalDate dateOfBirth,
            @Schema(description = "Rôle de l'utilisateur (USER, EMPLOYEE, ADMIN)")
            String role
    ) {}
}