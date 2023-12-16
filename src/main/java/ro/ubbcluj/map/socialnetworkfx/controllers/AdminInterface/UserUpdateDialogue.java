package ro.ubbcluj.map.socialnetworkfx.controllers.AdminInterface;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class UserUpdateDialogue {
    @FXML
    public TextField firstNameField;
    @FXML
    public TextField lastNameField;
    @FXML
    public TextField emailField;
    @FXML
    private TextField idField;
    @FXML
    private PasswordField passwordField;
    // Boolean variable which indicates if the cancel button was pressed.
    private boolean cancelled = false;

    /**
     * Setting default values for the text & prompt text of the fields.
     *
     * @param id        User id.
     * @param firstName User firstname.
     * @param lastName  User lastname.
     * @param email     User email.
     */
    public void setFields(String id, String firstName, String lastName, String email, String password) {
        // Setting the fields.
        this.idField.setText(id);
        this.firstNameField.setPromptText(firstName);
        this.lastNameField.setPromptText(lastName);
        this.emailField.setPromptText(email);
        this.passwordField.setText(password);
    }

    /**
     * Handler for when the 'update' button is pressed.
     *
     * @return Data from the fields.
     */
    public Map<String, String> handleUpdate() {
        // Retrieving data from the fields.
        String id = idField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        // Inserting the data into a map.
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("email", email);
        map.put("password", password);

        // Closing the window.
        Stage stage = (Stage) this.firstNameField.getScene().getWindow();
        stage.close();

        // Returning the values.
        return map;
    }


    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Handler for the pressing of the cancel button.
     */
    public void handleCancel() {
        // Updating the indicator.
        this.cancelled = true;

        // Closing the dialogue.
        Stage stage = (Stage) this.firstNameField.getScene().getWindow();
        stage.close();
    }
}
