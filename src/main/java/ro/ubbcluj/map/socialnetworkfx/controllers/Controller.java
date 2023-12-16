package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import ro.ubbcluj.map.socialnetworkfx.service.Service;

public class Controller {
    // Service dependency.
    protected Service service;

    /**
     * Initializes a controller.
     * @param service Service dependency.
     */
    public void initController(Service service) {
        // Setting the server dependency.
        this.service = service;

        // Other specific behavior, maybe overridden by the derived classes.
    }

    /**
     * Shows or masks the password on user action.
     * @param showPasswordCheckbox Checkbox that encapsulates the user action.
     * @param passwordField Field where the password resides.
     */
    public static void showPasswordAction(CheckBox showPasswordCheckbox, PasswordField passwordField) {
        if (showPasswordCheckbox.isSelected()) {
            passwordField.setPromptText(passwordField.getText());
            passwordField.clear();
        } else {
            passwordField.setText(passwordField.getPromptText());
            passwordField.setPromptText(null);
        }
    }
}
