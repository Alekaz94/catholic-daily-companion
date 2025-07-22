package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.*;
import com.alexandros.dailycompanion.Enum.Roles;
import com.alexandros.dailycompanion.Mapper.UserDtoMapper;
import com.alexandros.dailycompanion.Model.User;
import com.alexandros.dailycompanion.Repository.UserRepository;
import com.alexandros.dailycompanion.Security.JwtUtil;
import com.alexandros.dailycompanion.Security.PasswordUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    @Autowired
    public UserService(@Lazy AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserDtoMapper.toUserDto(users);
    }

    public UserDto getUser(UUID userId) throws AccessDeniedException {
        User currentUser = getAuthenticatedUser();
        if(!currentUser.getRole().equals(Roles.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to this user's information");
        }

        User user = getUserByIdOrThrow(userId);
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

        User user = getUserByIdOrThrow(userId);
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
        User user = getUserByIdOrThrow(userId);
        userRepository.deleteById(user.getId());
    }

    public ResponseEntity<?> login(@Valid LoginRequest loginRequest) {
        try{
            Authentication auth = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("No user found!"));
            UserDto userDto = UserDtoMapper.toUserDto(user);

            return ResponseEntity.ok(new LoginResponse(userDto, token));
        } catch(AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials!");
        }
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

    private User getUserByIdOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() ->
                new UsernameNotFoundException("Could not find user!"));
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
