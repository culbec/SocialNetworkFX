package ro.ubbcluj.map.socialnetworkfx.controllers.AdminInterface;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Dialogue for adding a user.
 */
public class UserAddDialogue {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    // Boolean variable which indicates if the cancel button was pressed.
    private boolean cancelled = false;

    /**
     * Handler for the pressing of the 'add' button.
     *
     * @return Map, which contains the values from the fields.
     */
    @FXML
    public Map<String, String> handleAdd() {
        // Retrieving data from the fields.
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        // Inserting the data into a map.
        Map<String, String> map = new HashMap<>();
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("email", email);
        map.put("password", password);

        // Closing the dialogue.
        Stage stage = (Stage) this.firstNameField.getScene().getWindow();
        stage.close();

        // Returning the values.
        return map;
    }

    /**
     * Checks if the cancel button was pressed.
     *
     * @return Boolean value which indicates if the cancel button was pressed or not.
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Handler for the pressing of the cancel button.
     */
    @FXML
    public void handleCancel() {
        // Updating the indicator.
        this.cancelled = true;

        // Closing the dialogue.
        Stage stage = (Stage) this.firstNameField.getScene().getWindow();
        stage.close();
    }
}
