package com.example.walletservice.service;

import com.example.walletservice.dto.request.LoginRequest;
import com.example.walletservice.dto.request.RegisterRequest;
import com.example.walletservice.dto.response.AuthResponse;
import com.example.walletservice.entity.Role;
import com.example.walletservice.entity.User;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.exception.EmailAlreadyExistsException;
import com.example.walletservice.repository.UserRepository;
import com.example.walletservice.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final WalletRepository walletRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager,WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.walletRepository=walletRepository;
    }
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);


       User saveduser = userRepository.save(user);

        Wallet wallet =new Wallet();
        wallet.setUser(saveduser);

        walletRepository.save(wallet);


        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken, new AuthResponse.UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name()));
    }


//    LOGIN
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken, new AuthResponse.UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole() != null ? user.getRole().name() : "USER"));
    }

    @Transactional
    public AuthResponse registerAdmin(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);

        User saveduser = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(saveduser);
        walletRepository.save(wallet);

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken, new AuthResponse.UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name()));
    }
}