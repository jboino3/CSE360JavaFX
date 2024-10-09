package asuHelloWorldJavaFX;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class Home extends Application {

    private UserService userService = new UserService();
    private User currentUser;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        // Username, Password, and One-Time Code Fields
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Label oneTimeCodeLabel = new Label("One-Time Code (if applicable):");
        TextField oneTimeCodeField = new TextField();

        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> handleLoginOrOneTimeCode(usernameField.getText(), passwordField.getText(), oneTimeCodeField.getText(), primaryStage));

        // Layout and Scene
        VBox loginPane = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField, oneTimeCodeLabel, oneTimeCodeField, loginButton);
        Scene loginScene = new Scene(loginPane, 300, 300);

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    // Handle login or one-time code registration
    private void handleLoginOrOneTimeCode(String username, String password, String oneTimeCode, Stage stage) {
        if (!oneTimeCode.isEmpty()) {
            handleOneTimeCodeRegistration(username, password, oneTimeCode, stage);
        } else {
            handleLogin(username, password, stage);
        }
    }

    // Handle normal login
    private void handleLogin(String username, String password, Stage stage) {
        // Check if this is the first login (i.e., no users exist in the system)
        if (userService.getAllUsers().isEmpty()) {
            System.out.println("No users in the system. Creating the first user as Admin.");
            userService.addUser(username, password, "Admin");
            currentUser = userService.getUser(username);
            System.out.println("Admin account created for: " + username);
            showProfileCompletionPage(stage);  // Redirect to profile completion
        } else {
            // Normal login process
            User user = userService.getUser(username);
            if (user == null) {
                System.out.println("No user found with username: " + username);
            } else if (userService.isOneTimePasswordValid(username, password)) {
                currentUser = user;
                showNewPasswordPage(stage);  // Redirect to new password page if one-time password is used
            } else if (user.verifyPassword(password)) {
                currentUser = user;
                if (user.getRoles().size() > 1) {
                    showRoleSelectionPage(stage); // If user has multiple roles, show role selection
                } else {
                    String role = user.getRoles().get(0);
                    if (role.equals("Admin")) {
                        showAdminHomePage(stage);
                    } else {
                        showHomePage(stage, role);
                    }
                }
            } else {
                System.out.println("Invalid password for user: " + username);
            }
        }
    }

    // Handle one-time code registration with required username and password
    private void handleOneTimeCodeRegistration(String username, String password, String oneTimeCode, Stage stage) {
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password are required when registering with a one-time code.");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please provide both username and password.");
            alert.show();
            return;
        }

        if (userService.isValidInvitationCode(oneTimeCode)) {
            // Create a new user with the username, password, and assigned roles from the invite
            String roles = userService.getRoleForInviteCode(oneTimeCode);
            userService.addUser(username, password, roles);
            currentUser = userService.getUser(username);  // Set the currentUser with the new account
            System.out.println("User created with roles: " + roles);
            showProfileCompletionPage(stage);  // Redirect to profile completion
        } else {
            System.out.println("Invalid one-time code.");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid one-time code.");
            alert.show();
        }
    }

    // Profile completion page for new users
    private void showProfileCompletionPage(Stage stage) {
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();

        Label middleNameLabel = new Label("Middle Name (optional):");
        TextField middleNameField = new TextField();

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();

        Label preferredNameLabel = new Label("Preferred First Name (optional):");
        TextField preferredNameField = new TextField();

        // Display the roles the user was assigned during registration
        Label roleLabel = new Label("Assigned Role(s): " + String.join(", ", currentUser.getRoles()));

        Button submitButton = new Button("Complete Profile");
        submitButton.setOnAction(event -> {
            currentUser.setEmail(emailField.getText());
            currentUser.setFirstName(firstNameField.getText());
            currentUser.setMiddleName(middleNameField.getText().isEmpty() ? null : middleNameField.getText());
            currentUser.setLastName(lastNameField.getText());
            currentUser.setPreferredName(preferredNameField.getText().isEmpty() ? null : preferredNameField.getText());

            // Redirect based on role after profile completion
            String primaryRole = currentUser.getRoles().get(0);
            if (primaryRole.equals("Admin")) {
                showAdminHomePage(stage);
            } else {
                showHomePage(stage, primaryRole);
            }
        });

        VBox profilePane = new VBox(10, emailLabel, emailField, firstNameLabel, firstNameField, middleNameLabel, middleNameField, lastNameLabel, lastNameField, preferredNameLabel, preferredNameField, roleLabel, submitButton);
        Scene profileScene = new Scene(profilePane, 400, 400);
        stage.setScene(profileScene);
    }

    // New password page for users logging in with one-time passwords
    private void showNewPasswordPage(Stage stage) {
        Label newPasswordLabel = new Label("Enter New Password:");
        PasswordField newPasswordField = new PasswordField();
        
        Label confirmPasswordLabel = new Label("Confirm New Password:");
        PasswordField confirmPasswordField = new PasswordField();
        
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(event -> {
            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Passwords do not match.");
                alert.show();
            } else {
                userService.setPassword(currentUser.getUsername(), newPasswordField.getText());
                userService.clearOneTimePassword(currentUser.getUsername());
                System.out.println("Password updated successfully for: " + currentUser.getUsername());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Password has been reset. Please log in again.");
                alert.show();
                start(stage);  // Redirect to login
            }
        });

        VBox newPasswordPane = new VBox(10, newPasswordLabel, newPasswordField, confirmPasswordLabel, confirmPasswordField, submitButton);
        Scene newPasswordScene = new Scene(newPasswordPane, 400, 200);
        stage.setScene(newPasswordScene);
    }

    // Role selection page for users with multiple roles
    private void showRoleSelectionPage(Stage stage) {
        Label roleLabel = new Label("Select your role for this session:");

        VBox rolePane = new VBox(10, roleLabel);

        for (String role : currentUser.getRoles()) {
            Button roleButton = new Button(role);
            roleButton.setOnAction(event -> {
                if (role.equals("Admin")) {
                    showAdminHomePage(stage);
                } else {
                    showHomePage(stage, role);
                }
            });
            rolePane.getChildren().add(roleButton);
        }

        Scene roleScene = new Scene(rolePane, 300, 200);
        stage.setScene(roleScene);
    }

    private void showAdminHomePage(Stage stage) {
        Label adminLabel = new Label("Admin Controls");

        // Buttons for admin functionalities
        Button inviteUserButton = new Button("Invite User");
        inviteUserButton.setOnAction(event -> showInviteUserPage(stage));

        Button resetPasswordButton = new Button("Reset User Password");
        resetPasswordButton.setOnAction(event -> showResetPasswordPage(stage));

        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.setOnAction(event -> showDeleteUserPage(stage));

        Button listUsersButton = new Button("List User Accounts");
        listUsersButton.setOnAction(event -> showListUsersPage(stage));

        Button modifyRolesButton = new Button("Add/Remove Roles");
        modifyRolesButton.setOnAction(event -> showModifyRolesPage(stage));

        Button logoutButton = new Button("Log out");
        logoutButton.setOnAction(event -> start(stage));

        VBox adminPane = new VBox(10, adminLabel, inviteUserButton, resetPasswordButton, deleteUserButton, listUsersButton, modifyRolesButton, logoutButton);
        Scene adminScene = new Scene(adminPane, 400, 300);
        stage.setScene(adminScene);
    }

    private void showInviteUserPage(Stage stage) {
        Label inviteLabel = new Label("Invite User");

        TextField usernameField = new TextField("Enter Username");
        CheckBox adminCheckBox = new CheckBox("Admin");
        CheckBox studentCheckBox = new CheckBox("Student");
        CheckBox instructorCheckBox = new CheckBox("Instructor");

        Button generateInviteButton = new Button("Generate Invite Code");
        generateInviteButton.setOnAction(event -> {
            String roles = "";
            if (adminCheckBox.isSelected()) roles += "Admin,";
            if (studentCheckBox.isSelected()) roles += "Student,";
            if (instructorCheckBox.isSelected()) roles += "Instructor,";
            String inviteCode = userService.generateInviteCode(roles);
            System.out.println("Invite code generated: " + inviteCode);
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> showAdminHomePage(stage));

        VBox invitePane = new VBox(10, inviteLabel, usernameField, adminCheckBox, studentCheckBox, instructorCheckBox, generateInviteButton, backButton);
        Scene inviteScene = new Scene(invitePane, 400, 300);
        stage.setScene(inviteScene);
    }

    private void showResetPasswordPage(Stage stage) {
        Label resetLabel = new Label("Reset User Password");

        TextField usernameField = new TextField("Enter Username");
        TextField passwordField = new TextField("Enter Temporary Password");

        Button resetPasswordButton = new Button("Reset Password");
        resetPasswordButton.setOnAction(event -> {
            userService.resetPassword(usernameField.getText(), passwordField.getText(), LocalDateTime.now().plusDays(1));
            System.out.println("Password reset for user: " + usernameField.getText());
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> showAdminHomePage(stage));

        VBox resetPane = new VBox(10, resetLabel, usernameField, passwordField, resetPasswordButton, backButton);
        Scene resetScene = new Scene(resetPane, 400, 300);
        stage.setScene(resetScene);
    }

    private void showDeleteUserPage(Stage stage) {
        Label deleteLabel = new Label("Delete User");

        TextField usernameField = new TextField("Enter Username");
        TextField confirmationField = new TextField();
        confirmationField.setPromptText("Type 'Yes' to confirm");

        Button deleteButton = new Button("Delete User");
        deleteButton.setOnAction(event -> {
            String username = usernameField.getText();
            String confirmation = confirmationField.getText();

            if (!confirmation.equals("Yes")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You must type 'Yes' to confirm deletion.");
                alert.show();
                return; // Exit if confirmation is not correct
            }

            User user = userService.getUser(username);
            if (user == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "User not found.");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete the account for user: " + username + "?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        userService.deleteUser(username);
                        System.out.println("User deleted: " + username);
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "User deleted successfully.");
                        successAlert.show();
                    }
                });
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> showAdminHomePage(stage));

        VBox deletePane = new VBox(10, deleteLabel, usernameField, confirmationField, deleteButton, backButton);
        Scene deleteScene = new Scene(deletePane, 400, 300);
        stage.setScene(deleteScene);
    }


    private void showListUsersPage(Stage stage) {
        Label listLabel = new Label("List of Users");

        ListView<String> userListView = new ListView<>();
        for (User user : userService.getAllUsers()) {
            userListView.getItems().add(user.getUsername() + " - " + user.getFirstName() + " " + user.getLastName() + " - Roles: " + String.join(", ", user.getRoles()));
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> showAdminHomePage(stage));

        VBox listPane = new VBox(10, listLabel, userListView, backButton);
        Scene listScene = new Scene(listPane, 400, 300);
        stage.setScene(listScene);
    }

    private void showModifyRolesPage(Stage stage) {
        Label modifyRolesLabel = new Label("Modify User Roles");

        TextField usernameField = new TextField("Enter Username");
        CheckBox adminCheckBox = new CheckBox("Admin");
        CheckBox studentCheckBox = new CheckBox("Student");
        CheckBox instructorCheckBox = new CheckBox("Instructor");

        Button modifyRolesButton = new Button("Modify Roles");
        modifyRolesButton.setOnAction(event -> {
            User user = userService.getUser(usernameField.getText());
            if (user != null) {
                // Clear all current roles
                user.clearRoles();

                // Add selected roles
                if (adminCheckBox.isSelected()) user.addRole("Admin");
                if (studentCheckBox.isSelected()) user.addRole("Student");
                if (instructorCheckBox.isSelected()) user.addRole("Instructor");

                System.out.println("Roles updated for user: " + usernameField.getText() + " to: " + String.join(", ", user.getRoles()));
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Roles updated successfully.");
                successAlert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "User not found.");
                alert.show();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> showAdminHomePage(stage));

        VBox modifyRolesPane = new VBox(10, modifyRolesLabel, usernameField, adminCheckBox, studentCheckBox, instructorCheckBox, modifyRolesButton, backButton);
        Scene modifyRolesScene = new Scene(modifyRolesPane, 400, 300);
        stage.setScene(modifyRolesScene);
    }


    private void showHomePage(Stage stage, String role) {
        Label welcomeLabel = new Label("Welcome, " + currentUser.getFirstName() + " (" + role + ")");

        Button logoutButton = new Button("Log out");
        logoutButton.setOnAction(event -> start(stage));

        VBox homePane = new VBox(10, welcomeLabel, logoutButton);
        Scene homeScene = new Scene(homePane, 300, 200);
        stage.setScene(homeScene);
    }
}

