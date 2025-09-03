package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.LoginResponse;
import com.alexandros.dailycompanion.dto.UserDto;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.mapper.UserDtoMapper;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.UserRepository;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class FirebaseAuthService {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    public FirebaseAuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse verifyFirebaseTokenAndLogin(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedFirebaseToken = firebaseAuth.verifyIdToken(idToken);
        String email = decodedFirebaseToken.getEmail().toLowerCase();

        Optional<User> optionalUser = userRepository.findByEmail(email);

        User user;
        if(optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            try {
                user = new User();
                user.setEmail(email);
                user.setFirstName(decodedFirebaseToken.getName() != null ? decodedFirebaseToken.getName() : "User");
                user.setRole(Roles.USER);
                user.setCreatedAt(LocalDate.now());
                user.setUpdatedAt(LocalDate.now());
                userRepository.save(user);
            } catch (DataIntegrityViolationException e) {
                user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalStateException("User was just created but cant be found."));
            }
        }

        String token = jwtUtil.generateToken(user.getEmail());
        UserDto userDto = UserDtoMapper.toUserDto(user);

        return new LoginResponse(userDto, token);
    }
}
