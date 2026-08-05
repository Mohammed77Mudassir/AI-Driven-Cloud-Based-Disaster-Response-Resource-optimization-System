package com.disaster.service;

import com.disaster.dto.UpdateUserRequest;
import com.disaster.dto.UserActivityDTO;
import com.disaster.dto.UserDTO;
import com.disaster.dto.PasswordResetRequest;
import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    List<UserDTO> searchUsers(String query);
    List<UserDTO> filterByRole(String role);
    List<UserDTO> filterByActive(boolean active);
    UserDTO getUserById(Long id);
    UserDTO updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    void activateUser(Long id);
    void deactivateUser(Long id);
    void changePassword(Long userId, PasswordResetRequest request);
    void resetPassword(Long userId, String newPassword);
    void changeUserRole(Long id, String newRole);
    UserActivityDTO getUserActivity(Long id);
    UserActivityDTO getCurrentUserActivity(String username);
    UserDTO getCurrentUserProfile(String username);
    UserDTO updateProfile(String username, UpdateUserRequest request);
}
