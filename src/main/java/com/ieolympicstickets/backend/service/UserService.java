package com.ieolympicstickets.backend.service;

import com.ieolympicstickets.backend.model.Role;
import com.ieolympicstickets.backend.model.User ;
import com.ieolympicstickets.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository  userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo= userRepo;
        this.passwordEncoder=passwordEncoder;
    }

    /** New user sign in - hashed password */
    public User register(User u, String rawPassword) {
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(u.getRole() == null ? Role.USER : u.getRole());
        return userRepo.save(u);
    }

    /**  fetch user or throw exception*/
    public User findUserByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException(email));
    }

    /** fetch user by id or throw exception - Admin*/
    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }

    /** Spring Security auth method*/
    @Override
    public UserDetails loadUserByUsername (String email) throws  UsernameNotFoundException {
        //workaround because isn't happy with builder()
        com.ieolympicstickets.backend.model.User u = findUserByEmail(email);
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles(u.getRole().name())
                .build();
    }
}
