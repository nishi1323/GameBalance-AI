package com.gamebalance.service;

import com.gamebalance.dto.*;
import com.gamebalance.exception.BadRequestException;
import com.gamebalance.exception.ConflictException;
import com.gamebalance.exception.ResourceNotFoundException;
import com.gamebalance.exception.UnauthorizedException;
import com.gamebalance.model.User;
import com.gamebalance.repository.UserRepository;
import com.gamebalance.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("An account with this email already exists.");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAge(request.getAge());
        user.setCollegeName(request.getCollegeName() != null ? request.getCollegeName().trim() : "");
        user.setDailyGamingLimitMinutes(120);
        user.setPreferredGamingStart("18:00");
        user.setPreferredGamingEnd("21:00");
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token, UserDto.from(saved));
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, UserDto.from(user));
    }

    public User getUserEntity(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found."));
    }

    public UserDto getCurrentUser(String userId) {
        return UserDto.from(getUserEntity(userId));
    }

    public UserDto updateProfile(String userId, ProfileUpdateRequest request) {
        User user = getUserEntity(userId);
        user.setFullName(request.getFullName().trim());
        user.setAge(request.getAge());
        user.setCollegeName(request.getCollegeName() != null ? request.getCollegeName().trim() : "");

        if (request.getDailyGamingLimitMinutes() != null) {
            user.setDailyGamingLimitMinutes(request.getDailyGamingLimitMinutes());
        }
        if (request.getPreferredGamingStart() != null && !request.getPreferredGamingStart().isBlank()) {
            validateTimeString(request.getPreferredGamingStart());
            user.setPreferredGamingStart(request.getPreferredGamingStart());
        }
        if (request.getPreferredGamingEnd() != null && !request.getPreferredGamingEnd().isBlank()) {
            validateTimeString(request.getPreferredGamingEnd());
            user.setPreferredGamingEnd(request.getPreferredGamingEnd());
        }
        user.setUpdatedAt(Instant.now());

        return UserDto.from(userRepository.save(user));
    }

    private void validateTimeString(String value) {
        try {
            LocalTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid time format '" + value + "'. Use HH:mm.");
        }
    }
}
