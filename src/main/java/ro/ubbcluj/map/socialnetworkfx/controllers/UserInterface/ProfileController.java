package ro.ubbcluj.map.socialnetworkfx.controllers.UserInterface;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkfx.SocialNetworkApplication;
import ro.ubbcluj.map.socialnetworkfx.controllers.Controller;
import ro.ubbcluj.map.socialnetworkfx.controllers.PopupAlert;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.service.IService;
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.service.ServiceUser;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProfileController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private AnchorPane profileLayout;
    // User that resides in the profile.
    private User user;
    // Service dependency.
    private ServiceUser serviceUser;
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Initializes the fields with user information.
     */
    private void initializeFields() {
        // Initializing the fields when the layout is reloaded.
        if (profileLayout.isVisible()) {
            this.firstNameField.setText(this.user.getFirstName());
            this.lastNameField.setText(this.user.getLastName());
            this.emailField.setText(this.user.getEmail());
        }
    }

    public void initController(IService serviceUser) {
        // Setting the service dependency.
        this.serviceUser = (ServiceUser) serviceUser;
        // Initializing the fields when the layout is reloaded.
        this.initializeFields();
    }

    public void updateButtonAction() {
        // Retrieving the fields from the layout.
        UUID userId = this.user.getId();
        String firstName = this.firstNameField.getText();
        String lastName = this.lastNameField.getText();
        String email = this.emailField.getText();

        // Trying to update the user.
        try {
            User retrieved = this.serviceUser.getUser(userId);
            User newUser = new User(userId, firstName, lastName, email, retrieved.getPassword());
            this.serviceUser.updateUser(newUser);
            PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "User updated successfully!", "");
        } catch (ServiceException sE) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", sE.getMessage());
        }
    }

    public void deleteButtonAction() throws IOException {
        FXMLLoader confirmDialogueLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/confirm-dialogue.fxml"));
        Scene confirmDialogueLayout = new Scene(confirmDialogueLoader.load());
        ConfirmDialogController confirmDialogController = confirmDialogueLoader.getController();

        Stage confirmDialogStage = new Stage();
        confirmDialogStage.setScene(confirmDialogueLayout);

        AtomicBoolean result = new AtomicBoolean(false);

        confirmDialogController.yesButton.setOnAction(event -> {
            result.set(true);
            confirmDialogStage.close();
        });
        confirmDialogController.noButton.setOnAction(event -> {
            result.set(false);
            confirmDialogStage.close();
        });

        confirmDialogStage.showAndWait();

        if (result.get()) {
            try {
                this.serviceUser.removeUser(this.user.getId());
                PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "User deleted successfully!", "");
                this.profileLayout.getScene().getWindow().hide();
            } catch (ServiceException sE) {
                PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", sE.getMessage());
            }
        }

    }
}
