package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import ro.ubbcluj.map.socialnetworkfx.entity.Message;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserEvent;
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.util.List;
import java.util.UUID;

public class SendMessageController implements Observer<SocialNetworkEvent> {
    @FXML
    public ComboBox<User> senderCombo;
    @FXML
    public ComboBox<User> receiverCombo;
    @FXML
    public ListView<User> userListView;
    @FXML
    public TextArea messageTextArea;

    // Service dependency.
    private Service service;

    // Last selected sender.
    private User lastSenderSelected;

    public void setService(Service service) {
        this.service = service;
    }

    public void initController(Service service) {
        this.setService(service);

        // Initializing the controller.
        this.receiverCombo.getItems().addAll(service.getUsers());
        this.senderCombo.getItems().addAll(service.getUsers());
    }

    public void senderComboAction() {
        // Setting the receiver combo to contain all users except the sender.
        // Personal choice to not send messages to itself.

        // If the user in the sender combo somehow gets removed -> sender combo is now null.
        // Resetting to the default state.
        if (senderCombo.getValue() == null) {
            this.receiverCombo.setPromptText("Select a receiver");
            this.receiverCombo.setDisable(true);
            lastSenderSelected = this.senderCombo.getValue();
            this.userListView.getItems().clear();
            return;
        }

        // Removing the current selected receiver from the sender and adding the last selected receiver.
        if (lastSenderSelected == null) {
            lastSenderSelected = this.senderCombo.getValue();
            this.receiverCombo.getItems().remove(lastSenderSelected);
            this.receiverCombo.setDisable(false);
            return;
        }

        this.receiverCombo.getItems().remove(this.senderCombo.getValue());
        this.receiverCombo.getItems().add(this.lastSenderSelected);
        this.lastSenderSelected = this.senderCombo.getValue();
    }

    public void receiverComboAction() {
        // When a receiver is selected, the receiver shall be removed from the combo box to not add it twice.
        // Also, the selected receiver shall be included in the user list.

        // Verifying that the selected receiver is not null.
        if (this.receiverCombo.getValue() != null) {
            if (this.userListView.getItems().contains(this.receiverCombo.getValue())) {
                this.userListView.getItems().remove(this.receiverCombo.getValue());
            } else {
                // If the receiver is selected again as it is already in the list -> remove the user from the user list.
                this.userListView.getItems().add(this.receiverCombo.getValue());
            }
            // Clearing the selection of the receiver combo.
            this.receiverCombo.setPromptText("Select the receiver");
        }
    }

    public void sendAction() {
        // Sending the message only if there are selected receivers and a message was written.
        if (this.userListView.getItems().isEmpty()) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "No receivers selected!", "No receivers were selected! Can't send the message.");
            return;
        }

        if (this.messageTextArea.getText().isEmpty()) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "No message written!", "Can't send a empty message!");
            return;
        }

        List<UUID> userIds = this.userListView.getItems().stream().map(User::getId).toList();
        Message message = new Message(this.senderCombo.getValue().getId(), userIds, this.messageTextArea.getText());
        this.service.sendMessage(message);

        PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "Message sent!", "");
    }

    public void resetAction() {
        // Clearing the combo boxes and the user list.
        this.senderCombo.getItems().clear();
        this.receiverCombo.getItems().clear();
        this.userListView.getItems().clear();

        // Reinitializing the combo boxes;
        this.senderCombo.getItems().addAll(this.service.getUsers());

        this.receiverCombo.setDisable(true);
        this.receiverCombo.getItems().addAll(this.service.getUsers());

        this.senderCombo.setPromptText("Select the sender");
        this.receiverCombo.setPromptText("Select the receiver");
    }

    @Override
    public void update(SocialNetworkEvent event) {
        if (event.getClass().equals(UserEvent.class)) {
            UserEvent userEvent = (UserEvent) event;

            switch (userEvent.getEventType()) {
                case ADD_USER -> this.addUserCombo(userEvent.getNewUser());
                case REMOVE_USER -> this.removeUserCombo(userEvent.getOldUser());
                case UPDATE_USER -> this.updateUserCombo(userEvent.getOldUser(), userEvent.getNewUser());
            }
        }
    }

    /**
     * Adds a new user to the combo boxes.
     *
     * @param newUser New user to be added.
     */
    private void addUserCombo(User newUser) {
        this.receiverCombo.getItems().add(newUser);
        this.senderCombo.getItems().add(newUser);
    }

    /**
     * Removes a user from the combo boxes.
     *
     * @param oldUser User to be removed.
     */
    private void removeUserCombo(User oldUser) {
        this.receiverCombo.getItems().remove(oldUser);
        this.senderCombo.getItems().remove(oldUser);
        // Updating the sender combo.
        this.senderComboAction();
        // Updating the receiver combo.
        this.receiverComboAction();
    }

    /**
     * Updates a user from the combo boxes.
     *
     * @param oldUser Old user to be updated.
     * @param newUser New user to replace the old user.
     */
    private void updateUserCombo(User oldUser, User newUser) {
        this.receiverCombo.getItems().set(this.receiverCombo.getItems().indexOf(oldUser), newUser);
        this.senderCombo.getItems().set(this.senderCombo.getItems().indexOf(oldUser), newUser);
        // Updating the sender combo.
        this.senderComboAction();
        // Updating the receiver combo.
        this.receiverComboAction();
    }
}
