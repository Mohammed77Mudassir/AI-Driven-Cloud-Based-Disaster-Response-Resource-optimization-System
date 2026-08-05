package com.disaster.service;

import com.disaster.dto.AuditLogDTO;
import com.disaster.dto.PasswordResetRequest;
import com.disaster.dto.UpdateUserRequest;
import com.disaster.dto.UserActivityDTO;
import com.disaster.dto.UserDTO;
import com.disaster.entity.Role;
import com.disaster.entity.User;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.AuditLogRepository;
import com.disaster.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllByOrderByUsernameAsc()
                .stream().map(UserDTO::fromEntity).toList();
    }

    @Override
    public List<UserDTO> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
                .stream().map(UserDTO::fromEntity).toList();
    }

    @Override
    public List<UserDTO> filterByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role.toUpperCase()))
                .stream().map(UserDTO::fromEntity).toList();
    }

    @Override
    public List<UserDTO> filterByActive(boolean active) {
        return userRepository.findByActive(active)
                .stream().map(UserDTO::fromEntity).toList();
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getProfilePhoto() != null) user.setProfilePhoto(request.getProfilePhoto());
        if (request.getEmergencyContact() != null) user.setEmergencyContact(request.getEmergencyContact());
        return UserDTO.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordResetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        validatePassword(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (newRole == null || newRole.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        try {
            user.setRole(Role.valueOf(newRole.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }
        userRepository.save(user);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one uppercase letter, one lowercase letter and one digit");
        }
    }

    @Override
    public UserActivityDTO getUserActivity(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return buildActivity(user);
    }

    @Override
    public UserActivityDTO getCurrentUserActivity(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return buildActivity(user);
    }

    private UserActivityDTO buildActivity(User user) {
        List<AuditLogDTO> activity = auditLogRepository
                .findByPerformedByOrderByTimestampDesc(user.getUsername()).stream()
                .limit(20)
                .map(AuditLogDTO::fromEntity)
                .toList();
        return UserActivityDTO.fromEntity(user, activity);
    }

    @Override
    public UserDTO getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(String username, UpdateUserRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getProfilePhoto() != null) user.setProfilePhoto(request.getProfilePhoto());
        if (request.getEmergencyContact() != null) user.setEmergencyContact(request.getEmergencyContact());
        return UserDTO.fromEntity(userRepository.save(user));
    }
}
