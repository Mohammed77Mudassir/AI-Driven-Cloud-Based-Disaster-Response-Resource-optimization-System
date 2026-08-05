package com.disaster.service;

import com.disaster.dto.*;
import com.disaster.entity.RefreshToken;
import com.disaster.entity.Role;
import com.disaster.entity.User;
import com.disaster.entity.VerificationToken;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.RefreshTokenRepository;
import com.disaster.repository.UserRepository;
import com.disaster.repository.VerificationTokenRepository;
import com.disaster.security.JwtUtils;
import com.disaster.security.LoginAttemptService;
import com.disaster.security.RbacService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RbacService rbacService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailProvider emailProvider;
    private final AuditService auditService;
    private final LoginAttemptService loginAttemptService;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       RbacService rbacService,
                       RefreshTokenRepository refreshTokenRepository,
                       VerificationTokenRepository verificationTokenRepository,
                       EmailProvider emailProvider,
                       AuditService auditService,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.rbacService = rbacService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailProvider = emailProvider;
        this.auditService = auditService;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );
        userRepository.save(user);

        // Create email verification token (architecture-ready; demo uses mock mailer)
        VerificationToken token = new VerificationToken(
                UUID.randomUUID().toString(),
                user,
                VerificationToken.TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(token);

        String verifyLink = frontendUrl + "/verify-email?token=" + token.getToken();
        emailProvider.sendEmail(user.getEmail(), "Verify your email",
                "Click to verify: " + verifyLink);

        auditService.log("REGISTER", "User", user.getId(), user.getUsername(), "New user registered");
        log.info("New user registered: {}", user.getUsername());
    }

    @Transactional
    public JwtResponse login(LoginRequest request) {
        if (loginAttemptService.isLocked(request.getUsername())) {
            auditService.log("LOGIN_LOCKED", "User", null, request.getUsername(),
                    "Login blocked: account temporarily locked after repeated failures");
            throw new IllegalArgumentException(
                    "Account temporarily locked after multiple failed attempts. Please try again later.");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            loginAttemptService.loginFailed(request.getUsername());
            auditService.log("LOGIN_FAILED", "User", null, request.getUsername(), "Failed login attempt");
            throw ex;
        }
        loginAttemptService.loginSucceeded(request.getUsername());
        User user = findByIdentifier(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated. Contact an administrator.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtils.generateToken(user.getUsername());
        String refreshToken = createRefreshToken(user);
        auditService.log("LOGIN", "User", user.getId(), user.getUsername(), "User logged in");

        return buildJwtResponse(accessToken, refreshToken, user);
    }

    @Transactional
    public JwtResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (stored.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }
        if (stored.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token has expired. Please login again");
        }
        User user = stored.getUser();
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated. Contact an administrator.");
        }
        // Rotate the refresh token: the presented token is single-use. Issuing a
        // new one every refresh prevents replay attacks if a token is ever leaked.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        String newRefreshToken = createRefreshToken(user);
        String accessToken = jwtUtils.generateToken(user.getUsername());
        return buildJwtResponse(accessToken, newRefreshToken, user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email"));
        VerificationToken token = new VerificationToken(
                UUID.randomUUID().toString(),
                user,
                VerificationToken.TokenType.PASSWORD_RESET,
                LocalDateTime.now().plusHours(1));
        verificationTokenRepository.save(token);

        String resetLink = frontendUrl + "/reset-password?token=" + token.getToken();
        emailProvider.sendEmail(user.getEmail(), "Reset your password",
                "Click to reset your password: " + resetLink);
        auditService.log("PASSWORD_RESET_REQUEST", "User", user.getId(), user.getUsername(),
                "Password reset requested");
        log.info("Password reset link sent for user: {}", user.getUsername());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));
        if (vt.getType() != VerificationToken.TokenType.PASSWORD_RESET) {
            throw new IllegalArgumentException("Invalid token type");
        }
        if (vt.isUsed() || vt.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token is used or expired. Request a new reset link");
        }
        User user = vt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        vt.setUsed(true);
        verificationTokenRepository.save(vt);
        auditService.log("PASSWORD_RESET", "User", user.getId(), user.getUsername(), "Password was reset");
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));
        if (vt.getType() != VerificationToken.TokenType.EMAIL_VERIFICATION) {
            throw new IllegalArgumentException("Invalid token type");
        }
        if (vt.isUsed() || vt.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token is used or expired");
        }
        User user = vt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        vt.setUsed(true);
        verificationTokenRepository.save(vt);
        auditService.log("EMAIL_VERIFIED", "User", user.getId(), user.getUsername(), "Email verified");
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshToken rt = new RefreshToken(token, user, LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L));
        refreshTokenRepository.save(rt);
        return token;
    }

    /**
     * Resolves a user by login identifier. Username wins over email when both
     * match (usernames are unique), so the identifier is tried as a username
     * first and only falls back to the email address.
     */
    private Optional<User> findByIdentifier(String usernameOrEmail) {
        Optional<User> byUsername = userRepository.findByUsername(usernameOrEmail);
        return byUsername.isPresent() ? byUsername : userRepository.findByEmail(usernameOrEmail);
    }

    private JwtResponse buildJwtResponse(String accessToken, String refreshToken, User user) {
        return new JwtResponse(accessToken, refreshToken, user.getId(), user.getUsername(),
                user.getEmail(), user.isEmailVerified(), user.getRole(),
                rbacService.permissionsForRole(user.getRole()).stream().sorted().toList());
    }
}
