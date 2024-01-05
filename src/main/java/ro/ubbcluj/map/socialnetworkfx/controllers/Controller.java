package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import ro.ubbcluj.map.socialnetworkfx.service.IService;
import ro.ubbcluj.map.socialnetworkfx.service.Service;

public class Controller {
    /**
     * Shows or masks the password on user action.
     *
     * @param showPasswordCheckbox Checkbox that encapsulates the user action.
     * @param passwordField        Field where the password resides.
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
