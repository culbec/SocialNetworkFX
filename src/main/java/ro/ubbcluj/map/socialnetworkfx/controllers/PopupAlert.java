package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Popup alert for the social network.
 */
public class PopupAlert {
    public static void showInformation(Stage owner, Alert.AlertType alertType, String header, String text) {
        Alert message = new Alert(alertType);

        // Setting the owner of the alert.
        message.initOwner(owner);

        // Setting header and content.
        message.setHeaderText(header);
        message.setContentText(text);

        // Graphical options.
        message.setResizable(true);
        message.getDialogPane().setMinHeight(100);
        message.getDialogPane().setMinWidth(300);
        message.getDialogPane().setPrefHeight(300);
        message.getDialogPane().setPrefWidth(700);

        // Showing and waiting for execution.
        message.showAndWait();
    }
}
