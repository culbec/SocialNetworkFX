package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.service.Service;

public class SignupController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    private Service service;

    public void initController(Service service) {
        this.service = service;
    }

    public void signupAction() {
        // Retrieving the field data from the fields in the form.
        String firstName = this.firstNameField.getText();
        String lastName = this.lastNameField.getText();
        String email = this.emailField.getText();
        String password = this.passwordField.getText();

        // Trying to add the new user.
        try {
            this.service.addUser(firstName, lastName, email, password);
        } catch (ServiceException sE) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Cannot sign up!", sE.getMessage());
            Stage stage = (Stage) this.firstNameField.getScene().getWindow();
            stage.close();
            return;
        }

        PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "User added successfully!", "");
        Stage stage = (Stage) this.firstNameField.getScene().getWindow();
        stage.close();
    }
}
