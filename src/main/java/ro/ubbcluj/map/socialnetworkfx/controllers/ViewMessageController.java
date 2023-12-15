package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import ro.ubbcluj.map.socialnetworkfx.entity.Message;
import ro.ubbcluj.map.socialnetworkfx.entity.ReplyMessage;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.MessageEvent;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserEvent;
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.util.Collections;
import java.util.List;

public class ViewMessageController implements Observer<SocialNetworkEvent> {
    @FXML
    private ComboBox<User> receiverCombo;
    @FXML
    private ComboBox<User> senderCombo;
    @FXML
    private ListView<Message> messageListView;
    @FXML
    private TextArea messageTextArea;
    // Service dependency.
    private Service service;
    // Last user selected in the combo box.
    private User lastReceiverSelected = null;

    public void setService(Service service) {
        this.service = service;
    }

    public void initController(Service service) {
        this.setService(service);

        // Initializing the controller.
        this.receiverCombo.getItems().addAll(this.service.getUsers());
        this.senderCombo.getItems().addAll(this.service.getUsers());

        // Setting the selection model of the message list so that a single message can be selected.
        this.messageListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void initMessageModel() {
        // Retrieving the messages.
        if (this.senderCombo.getValue() != null && this.receiverCombo.getValue() != null) {
            this.messageListView.getItems().clear();
            List<Message> messageList = this.service.getMessagesBetweenUsers(this.senderCombo.getValue(), this.receiverCombo.getValue());

            // Adding all the messages.
            this.messageListView.getItems().addAll(messageList);
        }
    }

    public void receiverComboAction() {
        // Setting the sender combo to contain all users except the receiver.
        // Personal choice to not send messages to itself.

        // If the user in the receiver combo somehow gets removed -> receiver combo is now null.
        // Resetting to the default state.
        if (receiverCombo.getValue() == null) {
            this.senderCombo.setDisable(true);
            lastReceiverSelected = this.receiverCombo.getValue();
            this.messageListView.getItems().clear();
            return;
        }

        // Removing the current selected receiver from the sender and adding the last selected receiver.
        if (lastReceiverSelected == null) {
            lastReceiverSelected = this.receiverCombo.getValue();
            this.senderCombo.getItems().remove(lastReceiverSelected);
            this.senderCombo.setDisable(false);
            return;
        }

        this.senderCombo.getItems().remove(this.receiverCombo.getValue());
        this.senderCombo.getItems().add(this.lastReceiverSelected);
        this.lastReceiverSelected = this.receiverCombo.getValue();

        // Showing messages if there is a receiver and a sender selected.
        if (this.receiverCombo.getValue() != null && this.senderCombo.getValue() != null) {
            this.initMessageModel();
        }
    }

    public void senderComboAction() {
        // If the sender is a null value -> there are no messages.
        if (this.senderCombo.getValue() == null) {
            this.messageListView.getItems().clear();
            return;
        }
        // Updating the messages.
        if (this.receiverCombo.getValue() != null && this.senderCombo.getValue() != null) {
            this.initMessageModel();
        }
    }

    public void replyAction() {
        // Verifying if a text was written in the text area.
        if (this.messageTextArea.getText().isEmpty()) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Empty message", "Cannot send a empty message!");
            return;
        }

        // Verifying if a message was selected.
        if (this.messageListView.getSelectionModel().getSelectedItem() == null) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "No message selected", "No message to reply to was selected!");
            return;
        }

        Message message = this.messageListView.getSelectionModel().getSelectedItem();
        String messageText = this.messageTextArea.getText();
        // If a message was written and a message was selected, we can proceed and send the message.
        ReplyMessage replyMessage = new ReplyMessage(message.getTo().get(0), Collections.singletonList(message.getFrom()), messageText, message.getId());
        this.service.sendMessage(replyMessage);

        PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "Reply sent!", "");
    }

    @Override
    public void update(SocialNetworkEvent event) {
        if (event.getClass().equals(UserEvent.class)) {
            UserEvent userEvent = (UserEvent) event;

            switch (userEvent.getEventType()) {
                case ADD_USER -> this.addUserCombo(userEvent.getNewUser());
                case UPDATE_USER -> this.removeUserCombo(userEvent.getOldUser());
                case REMOVE_USER -> this.updateUserCombo(userEvent.getOldUser(), userEvent.getNewUser());
            }

        } else if (event.getClass().equals(MessageEvent.class)) {
            MessageEvent messageEvent = (MessageEvent) event;

            // Adding the message in the message list only if the correspondents are selected.
            if (this.receiverCombo.getValue().getId().equals(messageEvent.getMessage().getFrom()) && messageEvent.getMessage().getTo().contains(this.senderCombo.getValue().getId())) {
                this.messageListView.getItems().add(messageEvent.getMessage());
            } else if (messageEvent.getMessage().getTo().contains(this.receiverCombo.getValue().getId()) && this.senderCombo.getValue().getId().equals(messageEvent.getMessage().getFrom())) {
                this.messageListView.getItems().add(messageEvent.getMessage());
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
        // Updating the receiver combo.
        this.receiverComboAction();
        // Updating the sender combo.
        this.senderComboAction();
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
        // Updating the receiver combo.
        this.receiverComboAction();
        // Updating the sender combo.
        this.senderComboAction();
    }
}
