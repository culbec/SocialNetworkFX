package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.service.Service;

public class FriendshipAddDialogue {
    @FXML
    private ComboBox<User> firstUserComboBox;
    @FXML
    private ComboBox<User> secondUserComboBox;

    // Service dependency for populating the combo boxes.
    private Service service;

    // Boolean variable which indicates if the cancel button was pressed.
    private boolean cancelled = false;

    public void setService(Service service) {
        this.service = service;

        // Populating the combo boxes.
        this.firstUserComboBox.getItems().addAll(this.service.getUsers());
        this.secondUserComboBox.getItems().addAll(this.service.getUsers());
    }

    /**
     * Handler for the pressing of the 'add' button.
     *
     * @return Tuple, which contains two users.
     */
    @FXML
    public Tuple<User, User> handleAdd() {
        // Inserting the data into a map.
        Tuple<User, User> userTuple = new Tuple<>(this.firstUserComboBox.getValue(), this.secondUserComboBox.getValue());

        // Closing the dialogue.
        Stage stage = (Stage) this.firstUserComboBox.getScene().getWindow();
        stage.close();

        // Returning the values.
        return userTuple;
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
        Stage stage = (Stage) this.firstUserComboBox.getScene().getWindow();
        stage.close();
    }
}
