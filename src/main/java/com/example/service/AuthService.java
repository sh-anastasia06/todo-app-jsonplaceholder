package com.example.service;

import com.example.client.ApiClient;
import com.example.client.JsonPlaceholderClient;
import com.example.model.ApiException;
import com.example.model.User;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final ApiClient apiClient;
    private Long currentUserId = null;
    private User currentUser = null;

    public AuthService() {
        this.apiClient = new JsonPlaceholderClient();
    }

    public AuthService(ApiClient apiClient) {
        this.apiClient = apiClient != null ? apiClient : new JsonPlaceholderClient();
    }

    // auth by ID
    public boolean login(Long userId) throws ApiException {
        if (userId == null || userId <= 0)
            throw new IllegalArgumentException("Invalid user ID");

        try {
            // check if user exists
            Optional<User> userOptional = apiClient.getUserById(userId);

            if (userOptional.isPresent()) {
                this.currentUserId = userId;
                this.currentUser = userOptional.get();
                return true;
            }

            return false;
        } catch (ApiException e) {
            throw e; // throw it upper
        } catch (Exception e) {
            throw new ApiException("Filed to login: " + e.getMessage(), e);
        }
    }

    // auth by username
    public boolean loginByUsername(String username) throws ApiException {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("Username cannot be empty");

        try {
            List<User> allUsers = apiClient.getAllUsers();

            for (User u : allUsers) {
                if (username.equalsIgnoreCase(u.getUsername())) {
                    this.currentUserId = u.getId();
                    this.currentUser = u;
                    return true;
                }
            }

            return false;
        } catch (ApiException e) {
            throw e; // throw it upper
        } catch (Exception e) {
            throw new ApiException("Failed to login by username: " + e.getMessage(), e);
        }
    }

    public void logout() {
        this.currentUserId = null;
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    // get all users for drop-down list
    public List<User> getAllUSers() throws ApiException {
        return apiClient.getAllUsers();
    }

    public boolean testApiConnection() throws ApiException {
        return apiClient.testConnection();
    }

    public String getCurrentUserDisplayName() {
        return currentUser != null ?
                currentUser.getDisplayName() :
                "Not logged in";
    }

    // reset current session for testing
    public void reset() {
        logout();
    }
}
