package project;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class UserService {
    private Map<String, User> users = new HashMap<>();
    private Map<String, String> invitationCodes = new HashMap<>();
    private Map<String, String> oneTimePasswords = new HashMap<>();
    private Map<String, LocalDateTime> passwordExpirations = new HashMap<>();

    // Add a new user to the system
    public void addUser(String username, String password, String roles) {
        User user = new User(username, password, roles);
        users.put(username, user);
    }

    // Fetch a user by username
    public User getUser(String username) {
        return users.get(username);
    }

    // Get all users in the system
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    // Generate a one-time invitation code for new users with specific roles
    public String generateInviteCode(String roles) {
        String inviteCode = UUID.randomUUID().toString();
        invitationCodes.put(inviteCode, roles);
        return inviteCode;
    }

    // Check if the invitation code is valid
    public boolean isValidInvitationCode(String code) {
        return invitationCodes.containsKey(code);
    }

    // Fetch role for an invitation code
    public String getRoleForInviteCode(String inviteCode) {
        return invitationCodes.get(inviteCode);
    }

    // Reset user password with expiration and clear the old password immediately
    public void resetPassword(String username, String oneTimePassword, LocalDateTime expiration) {
        User user = getUser(username);
        if (user != null) {
            oneTimePasswords.put(username, oneTimePassword);
            passwordExpirations.put(username, expiration);
            user.setPassword(oneTimePassword); // Replace the old password with the one-time password
            user.setOneTimePassword(true);
        }
    }

    // Check if a one-time password is valid
    public boolean isOneTimePasswordValid(String username, String password) {
        if (oneTimePasswords.containsKey(username)) {
            LocalDateTime expiration = passwordExpirations.get(username);
            if (expiration.isAfter(LocalDateTime.now()) && oneTimePasswords.get(username).equals(password)) {
                return true;
            }
        }
        return false;
    }

    // Clear the one-time password once it's used
    public void clearOneTimePassword(String username) {
        oneTimePasswords.remove(username);
        passwordExpirations.remove(username);
        User user = getUser(username);
        if (user != null) {
            user.setOneTimePassword(false);
        }
    }

    // Set a new password for the user
    public void setPassword(String username, String newPassword) {
        User user = getUser(username);
        if (user != null) {
            user.setPassword(newPassword);  // Set the new password
        }
    }

    // Delete user from the system
    public void deleteUser(String username) {
        users.remove(username);
    }

    // Add a role to a user
    public void addRole(String username, String role) {
        User user = getUser(username);
        if (user != null) {
            user.addRole(role);
        }
    }

    // Remove a role from a user
    public void removeRole(String username, String role) {
        User user = getUser(username);
        if (user != null) {
            user.removeRole(role);
        }
    }
}

