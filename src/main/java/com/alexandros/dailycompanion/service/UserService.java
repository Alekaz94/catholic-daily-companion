package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.mapper.UserDtoMapper;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.UserRepository;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.alexandros.dailycompanion.security.PasswordUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ServiceHelper serviceHelper;

    @Autowired
    public UserService(@Lazy AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil, ServiceHelper serviceHelper) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.serviceHelper = serviceHelper;
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
        User currentUser = getAuthenticatedUser();
        if(!currentUser.getRole().equals(Roles.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to this user's information");
        }

        User user = serviceHelper.getUserByIdOrThrow(userId);
        return UserDtoMapper.toUserDto(user);
    }

    public UserDto createUser(@Valid UserRequest userRequest) {
        Optional<User> existingUser = userRepository.findByEmail(userRequest.email());
        if(existingUser.isPresent()) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        User user = new User();
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPassword(PasswordUtil.hashPassword(userRequest.password()));
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setRole(Roles.USER);
        userRepository.save(user);
        return UserDtoMapper.toUserDto(user);
    }

    public UserDto updateUserPassword(UUID userId, @Valid UserUpdateRequest userUpdateRequest) throws AccessDeniedException {
        User currentUser = getAuthenticatedUser();
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
        return UserDtoMapper.toUserDto(user);
    }

    public void deleteUser(UUID userId) {
        User user = serviceHelper.getUserByIdOrThrow(userId);
        userRepository.deleteById(user.getId());
    }

    public LoginResponse login(@Valid LoginRequest loginRequest) {
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails.getUsername());
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("No user found!"));
        UserDto userDto = UserDtoMapper.toUserDto(user);

        return new LoginResponse(userDto, token);
    }

    public UserDto signUp(@Valid UserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.email()).isPresent()) {
            throw new IllegalArgumentException("Email already registered!");
        }
        User user = new User();
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPassword(PasswordUtil.hashPassword(userRequest.password()));
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setRole(Roles.USER);

        userRepository.save(user);
        return UserDtoMapper.toUserDto(user);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Authenticated user not found!"));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        List.of(new SimpleGrantedAuthority(user.getRole().toString()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
