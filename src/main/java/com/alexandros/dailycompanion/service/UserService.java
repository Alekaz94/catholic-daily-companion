/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.enums.AuditAction;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.mapper.FeedbackDtoMapper;
import com.alexandros.dailycompanion.mapper.UserDtoMapper;
import com.alexandros.dailycompanion.model.RefreshToken;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.FeedbackRepository;
import com.alexandros.dailycompanion.repository.JournalEntryRepository;
import com.alexandros.dailycompanion.repository.RosaryLogRepository;
import com.alexandros.dailycompanion.repository.UserRepository;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.alexandros.dailycompanion.security.PasswordUtil;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);
    private final AuditLogService auditLogService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ServiceHelper serviceHelper;
    private final RefreshTokenService refreshTokenService;
    private final JournalEntryRepository journalEntryRepository;
    private final RosaryLogRepository rosaryLogRepository;
    private final FeedbackRepository feedbackRepository;
    private final RosaryLogService rosaryLogService;

    @Autowired
    public UserService(AuditLogService auditLogService, @Lazy AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil, ServiceHelper serviceHelper, RefreshTokenService refreshTokenService, JournalEntryRepository journalEntryRepository, RosaryLogRepository rosaryLogRepository, FeedbackRepository feedbackRepository, RosaryLogService rosaryLogService) {
        this.auditLogService = auditLogService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.serviceHelper = serviceHelper;
        this.refreshTokenService = refreshTokenService;
        this.journalEntryRepository = journalEntryRepository;
        this.rosaryLogRepository = rosaryLogRepository;
        this.feedbackRepository = feedbackRepository;
        this.rosaryLogService = rosaryLogService;
    }

    public Page<UserDto> getAllUsers(String query, int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<User> users;
        if(query == null || query.trim().isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findAllByEmailContainingIgnoreCase(query, pageable);
        }
        return UserDtoMapper.toUserDto(users);
    }

    public UserDto getUser(UUID userId) throws AccessDeniedException {
        User currentUser = serviceHelper.getAuthenticatedUser();
        if(!currentUser.getRole().equals(Roles.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to this user's information");
        }

        User user = serviceHelper.getUserByIdOrThrow(userId);
        return UserDtoMapper.toUserDto(user);
    }

    public UserDto createUser(@Valid UserRequest userRequest,
                              String ipAddress) {
        String email = userRequest.email().toLowerCase();
        Optional<User> existingUser = userRepository.findByEmail(email);
        if(existingUser.isPresent()) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        User user = new User();
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(email);
        user.setPassword(PasswordUtil.hashPassword(userRequest.password()));
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setRole(Roles.USER);
        userRepository.save(user);

        auditLogService.logAction(user.getId(),
                AuditAction.CREATE_USER.name(),
                "User",
                user.getId(),
                String.format("{\"ID\": \"%s\"}", user.getId()),
                ipAddress
        );
        logger.info("Created user account '{}'", user.getId());

        return UserDtoMapper.toUserDto(user);
    }

    public UserDashboardDto getDashboardForCurrentUser() {
        User user = serviceHelper.getAuthenticatedUser();

        int journalCount = journalEntryRepository.countByUserId(user.getId());
        int rosaryCount = rosaryLogRepository.countCompletedByUserId(user.getId());
        int feedbackCount = feedbackRepository.countByUserEmail(user.getEmail());

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<FeedbackDto> recentFeedbacks = feedbackRepository
                .findAllByUserEmail(user.getEmail(), pageable)
                .map(FeedbackDtoMapper::toFeedbackDto);

        int currentStreak = rosaryLogService.getStreak(user.getId());
        int highestStreak = rosaryLogService.calculateHighestStreak(user.getId());

        return new UserDashboardDto(
                journalCount,
                rosaryCount,
                feedbackCount,
                recentFeedbacks,
                currentStreak,
                highestStreak
        );
    }

    public UserDto updateUserPassword(UUID userId,
                                      @Valid UserUpdateRequest userUpdateRequest,
                                      String ipAddress) throws AccessDeniedException {
        User currentUser = serviceHelper.getAuthenticatedUser();
        if(!currentUser.getRole().equals(Roles.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to this user's information");
        }

        User user = serviceHelper.getUserByIdOrThrow(userId);
        String storedPasswordHash = userRepository.findPasswordHashById(userId);

        if(!PasswordUtil.validateHashedPassword(userUpdateRequest.currentPassword(), storedPasswordHash)) {
            throw new IllegalArgumentException("Current password is incorrect!");
        }
        user.setPassword(PasswordUtil.hashPassword(userUpdateRequest.newPassword()));
        user.setUpdatedAt(LocalDate.now());
        userRepository.save(user);

        auditLogService.logAction(
                user.getId(),
                AuditAction.UPDATE_USER_PASSWORD.name(),
                "User",
                user.getId(),
                "{\"passwordChange\": true}",
                ipAddress
        );

        logger.info("Updated password for user '{}'", user.getId());
        return UserDtoMapper.toUserDto(user);
    }

    public UserDto updateUserName(UUID userId,
                                  UserNameUpdateRequest userNameUpdateRequest,
                                  String ipAddress) throws AccessDeniedException, BadRequestException {
        User currentUser = serviceHelper.getAuthenticatedUser();
        if(!currentUser.getRole().equals(Roles.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to this user's information");
        }

        if (userNameUpdateRequest.firstName() == null && userNameUpdateRequest.lastName() == null) {
            throw new BadRequestException("At least one field must be provided to update.");
        }

        User user = serviceHelper.getUserByIdOrThrow(userId);

        if(userNameUpdateRequest.firstName() != null) {
            user.setFirstName(userNameUpdateRequest.firstName().trim());
        }
        if(userNameUpdateRequest.lastName() != null) {
            user.setLastName(userNameUpdateRequest.lastName().trim());
        }

        user.setUpdatedAt(LocalDate.now());
        userRepository.save(user);

        auditLogService.logAction(
                user.getId(),
                AuditAction.UPDATE_USER_NAME.name(),
                "User",
                user.getId(),
                String.format("{\"nameChange\": \"true\"}"),
                ipAddress
        );

        logger.info("Updated name for user '{}'", user.getId());
        return UserDtoMapper.toUserDto(user);
    }

    @Transactional
    public void deleteUser(UUID userId, String ipAddress) throws AccessDeniedException {
        User currentUser = serviceHelper.getAuthenticatedUser();
        if(!currentUser.getRole().equals(Roles.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to this user's information");
        }

        refreshTokenService.deleteByEmail(currentUser.getEmail());
        journalEntryRepository.deleteAllByUserId(userId);
        rosaryLogRepository.deleteAllByUserId(userId);

        auditLogService.logAction(
                currentUser.getId(),
                AuditAction.DELETE_USER.name(),
                "User",
                currentUser.getId(),
                "{}",
                ipAddress
        );

        logger.warn("Deleted user '{}'", userId);
        userRepository.deleteById(userId);
    }

    public LoginResponse login(@Valid LoginRequest loginRequest) {
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("No user found!"));

        UserDto userDto = UserDtoMapper.toUserDto(user);
        String token = jwtUtil.generateToken(userDto);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        return new LoginResponse(userDto, token, refreshToken.getToken());
    }

    public User signUp(@Valid UserRequest userRequest, String ipAddress) {
        try{
            String email = userRequest.email().toLowerCase();
            if (userRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Email already registered!");
            }
            User user = new User();
            user.setFirstName(userRequest.firstName());
            user.setLastName(userRequest.lastName());
            user.setEmail(email);
            user.setPassword(PasswordUtil.hashPassword(userRequest.password()));
            user.setCreatedAt(LocalDate.now());
            user.setUpdatedAt(LocalDate.now());
            user.setRole(Roles.USER);
            userRepository.save(user);

            auditLogService.logAction(
                    user.getId(),
                    "SIGNUP_USER",
                    "User",
                    user.getId(),
                    "{\"passwordChange\": true}",
                    ipAddress
            );

            return user;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already registered!");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword() != null ? user.getPassword() : "",
                        List.of(new SimpleGrantedAuthority(user.getRole().toString()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
