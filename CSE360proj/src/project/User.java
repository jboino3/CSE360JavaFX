package project;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {
    private String username;
    private byte[] passwordHash;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private String preferredName;
    private boolean isOneTimePassword;  // New flag for one-time password use
    private List<String> roles;

    // Constructor for new users
    public User(String username, String password, String role) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.roles = new ArrayList<>(Arrays.asList(role));
        this.isOneTimePassword = false;  // Default to false
    }

    // Password verification
    public boolean verifyPassword(String password) {
        return Arrays.equals(this.passwordHash, hashPassword(password));
    }

    public static byte[] hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Set a new password
    public void setPassword(String newPassword) {
        this.passwordHash = hashPassword(newPassword);
    }

    // Set one-time password flag
    public void setOneTimePassword(boolean isOneTimePassword) {
        this.isOneTimePassword = isOneTimePassword;
    }

    // Check if one-time password flag is set
    public boolean isOneTimePassword() {
        return isOneTimePassword;
    }

    // Getters and setters for other fields...
    
    public String getFirstName() {
        return preferredName != null && !preferredName.isEmpty() ? preferredName : firstName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void addRole(String role) {
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }

    public void removeRole(String role) {
        roles.remove(role);
    }
    
    public void clearRoles() {
        roles.clear();
    }
}


