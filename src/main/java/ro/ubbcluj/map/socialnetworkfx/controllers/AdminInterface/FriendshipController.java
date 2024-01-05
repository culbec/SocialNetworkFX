package ro.ubbcluj.map.socialnetworkfx.controllers.AdminInterface;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkfx.SocialNetworkApplication;
import ro.ubbcluj.map.socialnetworkfx.controllers.PopupAlert;
import ro.ubbcluj.map.socialnetworkfx.entity.FriendRequest;
import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.FriendshipEvent;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserEvent;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.utility.PopupEnum;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FriendshipController implements Observer<SocialNetworkEvent> {
    // Contains all the users of the network.
    @FXML
    private ComboBox<User> userComboBox;
    // Contains the friends of the selected user in the combo box.
    @FXML
    private ListView<User> friendsListView;
    // Contains all the friend requests still pending sent to the user in the combo box.
    @FXML
    private ListView<User> friendRequestsListView;
    // Service dependency.
    private Service service;

    /**
     * Initializes the controller.
     *
     * @param service Service dependency.
     */
    public void initController(Service service) {
        // Setting the service dependency.
        this.service = service;

        // Populating the combo box with users.
        this.userComboBox.getItems().addAll(this.service.getUsers());

        // Setting the selection model of the friends to multiple and of the friend requests to single.
        this.friendsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.friendRequestsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Updates the contents of the lists based on the user selected in the combo box.
     */
    public void userComboAction() {
        // Retrieving the selected user from the combo box.
        User selected = this.userComboBox.getValue();

        // Running action when selected value is not null.
        if (selected != null) {
            // Updating the list of friends based on the selected user.
            this.friendsListView.getItems().clear();
            this.friendsListView.getItems().addAll(this.service.getFriendsOf(selected.getId()));

            // Updating the friend requests for each user.
            this.friendRequestsListView.getItems().clear();
            List<FriendRequest> friendRequests = this.service.getFriendRequestsOfUser(selected.getId());

            // If there are friend requests, we map the friend requests with friend request senders.
            if (!friendRequests.isEmpty()) {
                List<User> senders = friendRequests.stream()
                        .filter(friendRequest -> friendRequest.getIdTo().equals(this.userComboBox.getValue().getId()) && friendRequest.getStatus().equals("pending"))
                        .map(friendRequest -> this.service.getUser(friendRequest.getIdFrom()))
                        .toList();
                this.friendRequestsListView.getItems().addAll(senders);
            }
        }
    }

    /**
     * Verifies if the friend list is empty.
     *
     * @return Boolean value encapsulating the emptiness of the friend list.
     */
    private boolean checkFriendListViewEmpty() {
        if (this.friendsListView.getItems().isEmpty()) {
            PopupEnum identifier = PopupEnum.EMPTY_LIST_EXCEPTION;
            String header = AdminController.getPopups().get(identifier).getLeft();
            String text = AdminController.getPopups().get(identifier).getRight();
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
            return true;
        }
        return false;
    }

    /**
     * Verifies if a selection was made in the friend list.
     *
     * @return Boolean value encapsulating if a selection was made in the friend list.
     */
    private boolean checkFriendSelectionEmpty() {
        if (this.friendsListView.getSelectionModel().isEmpty()) {
            PopupEnum identifier = PopupEnum.NONE_SELECTED_EXCEPTION;
            String header = AdminController.getPopups().get(identifier).getLeft();
            String text = AdminController.getPopups().get(identifier).getRight();
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
            return true;
        }
        return false;
    }

    /**
     * Sends a friend request from a user to another.
     *
     * @throws IOException If the FXMLLoader couldn't be loaded.
     */
    public void friendshipAdd() throws IOException {
        // Opening the adding dialogue.
        FXMLLoader friendshipAddDialogueLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/friendship-add-dialogue.fxml"));
        Scene friendshipAddScene = new Scene(friendshipAddDialogueLoader.load());

        // Creating a stage for the dialogue.
        Stage friendshipAddStage = new Stage();
        friendshipAddStage.setScene(friendshipAddScene);

        // Setting the behavior for closing.
        FriendshipAddDialogue friendshipAddDialogue = friendshipAddDialogueLoader.getController();
        friendshipAddStage.setOnCloseRequest(event -> friendshipAddDialogue.handleCancel());

        // Setting the service for the dialogue controller.
        friendshipAddDialogue.initDialogue(this.service);

        // Showing the dialogue and waiting for execution.
        friendshipAddStage.showAndWait();

        // Verifying if the action was canceled.
        if (friendshipAddDialogue.isCancelled()) {
            return;
        }

        // Extracting the users.
        Tuple<User, User> userTuple = friendshipAddDialogue.handleAdd();

        // Verifying if there were users selected.
        if (userTuple.getLeft() == null && userTuple.getRight() == null) {
            String header = "No users were selected.";
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, "");
            return;
        } else if (userTuple.getLeft() == null) {
            String header = "Sender wasn't selected.";
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, "");
            return;
        } else if (userTuple.getRight() == null) {
            String header = "Receiver wasn't selected.";
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, "");
            return;
        }

        try {
            // Sending a friend request from the sender to the receiver.
            this.service.sendFriendRequest(userTuple.getLeft(), userTuple.getRight());
            // Updating the lists.
            this.userComboAction();

            // Showing a message of success.
            PopupEnum identifier = PopupEnum.FRIENDSHIP_REQUEST_SUCCESS;
            String header = AdminController.getPopups().get(identifier).getLeft();
            String text = "Friend request sent from " + this.service.getUser(userTuple.getLeft().getId()) + " to " + this.service.getUser(userTuple.getRight().getId()) + ".";
            PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, header, text);

        } catch (ServiceException sE) {
            PopupEnum identifier = PopupEnum.FRIENDSHIP_REQUEST_EXCEPTION;
            String header = AdminController.getPopups().get(identifier).getLeft();
            String text = sE.getMessage();
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
        }
    }

    /**
     * Removes friendships between users.
     */
    public void friendshipRemove() {
        // Verifying if the current friend list is empty.
        if (this.checkFriendListViewEmpty()) {
            return;
        }

        // Verifying if there was a selection made.
        if (this.checkFriendSelectionEmpty()) {
            return;
        }

        // Removing all the selected friends.
        List<User> friends = this.friendsListView.getSelectionModel().getSelectedItems().stream().toList();

        friends.forEach(friend -> this.service.removeFriendship(this.userComboBox.getValue().getId(), friend.getId()));

        // Showing a message of success.
        PopupEnum identifier = PopupEnum.REMOVE_FRIENDSHIP_SUCCESS;
        String header = AdminController.getPopups().get(identifier).getLeft();
        PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, header, "");
    }

    /**
     * @return The current selected value in the friend request view.
     */
    private User extractSelected() {
        User selected = this.friendRequestsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            PopupEnum identifier = PopupEnum.NONE_SELECTED_EXCEPTION;
            String header = AdminController.getPopups().get(identifier).getLeft();
            String text = AdminController.getPopups().get(identifier).getRight();
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
            return null;
        }
        return selected;
    }

    /**
     * Accepts a friend request between two users.
     */
    public void friendshipAccept() {
        // Verifying if the value in the combo box is not null.
        User receiver = this.userComboBox.getValue();

        if (receiver != null) {
            // Getting the selected user.
            User sender = this.extractSelected();

            if (sender != null) {
                // If there is a selected user, we accept the friend request.
                Optional<FriendRequest> friendRequestOptional = this.service.getFriendRequestsOfUser(receiver.getId()).stream()
                        .filter(friendRequest -> friendRequest.getIdFrom().equals(sender.getId()))
                        .findFirst();

                friendRequestOptional.ifPresent(friendRequest -> {
                    // Adding the friendship between the two users.
                    this.service.acceptFriendRequest(friendRequest);

                    // Removing the user from the friend request list.
                    this.friendRequestsListView.getItems().remove(sender);

                    // Showing a message of success.
                    PopupEnum identifier = PopupEnum.ADD_FRIENDSHIP_SUCCESS;
                    String header = AdminController.getPopups().get(identifier).getLeft();
                    String text = "Friendship was added between " + this.service.getUser(friendRequest.getIdFrom()) + " and " + this.service.getUser(friendRequest.getIdTo()) + ".";
                    PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, header, text);
                });
            }
        }
    }

    /**
     * Rejects a friend request between two users.
     */
    public void friendshipReject() {
        // Verifying if the value in the combo box is not null.
        User receiver = this.userComboBox.getValue();

        if (receiver != null) {
            // Getting the selected user.
            User sender = this.extractSelected();

            if (sender != null) {
                // If there is a selected user, we accept the friend request.
                Optional<FriendRequest> friendRequestOptional = this.service.getFriendRequestsOfUser(receiver.getId()).stream()
                        .filter(friendRequest -> friendRequest.getIdFrom().equals(sender.getId()))
                        .findFirst();

                friendRequestOptional.ifPresent(friendRequest -> {
                    this.service.rejectFriendRequest(friendRequest);

                    // Removing the user from the friend request list.
                    this.friendRequestsListView.getItems().remove(sender);

                    // Showing a message of success.
                    String header = "Friend request rejected!";
                    String text = "Friend request from " + this.service.getUser(friendRequest.getIdFrom()) + " was rejected.";
                    PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, header, text);
                });
            }
        }
    }

    @Override
    public void update(SocialNetworkEvent event) {
        if (event.getClass().equals(UserEvent.class)) {
            // Updating on user event.
            UserEvent userEvent = (UserEvent) event;
            switch (userEvent.getEventType()) {
                case ADD_USER -> this.addUserComboBox(userEvent.getNewUser());
                case REMOVE_USER -> {
                    this.removeUserComboBox(userEvent.getOldUser());

                    // Removing a friend if necessary.
                    if (this.userComboBox.getValue() != null) {
                        if (this.service.getFriendsOf(this.userComboBox.getValue().getId()).contains(userEvent.getOldUser())) {
                            this.friendsListView.getItems().remove(userEvent.getOldUser());
                        }
                    }
                }
                case UPDATE_USER -> {
                    this.updateUserComboBox(userEvent.getOldUser(), userEvent.getNewUser());

                    // Updating a friend if necessary.
                    if (this.userComboBox.getValue() != null) {
                        if (this.service.getFriendsOf(this.userComboBox.getValue().getId()).contains(userEvent.getOldUser())) {
                            this.friendsListView.getItems().set(this.friendsListView.getItems().indexOf(userEvent.getOldUser()), userEvent.getNewUser());
                        }
                    }
                }
            }
        } else if (event.getClass().equals(FriendshipEvent.class)) {
            FriendshipEvent friendshipEvent = (FriendshipEvent) event;
            switch (friendshipEvent.getEventType()) {
                case ADD_FRIENDSHIP -> this.addFriendList(friendshipEvent.getNewFriendship());
                case REMOVE_FRIENDSHIP -> this.removeFriendList(friendshipEvent.getOldFriendship());
            }
        }
    }

    /**
     * Adds a new friend to the friend list.
     *
     * @param newFriendship Friendship that encapsulates the two friends.
     */
    private void addFriendList(Friendship newFriendship) {
        User friend1 = this.service.getUser(newFriendship.getId().getLeft());
        User friend2 = this.service.getUser(newFriendship.getId().getRight());

        if (this.userComboBox.getValue().equals(friend1)) {
            this.friendsListView.getItems().add(friend2);
        } else if (this.userComboBox.getValue().equals(friend2)) {
            this.friendsListView.getItems().add(friend1);
        }
    }

    /**
     * Removes a friend from the friend list.
     *
     * @param oldFriendship Friendship that encapsulates the two old friends.
     */
    private void removeFriendList(Friendship oldFriendship) {
        User friend1 = this.service.getUser(oldFriendship.getId().getLeft());
        User friend2 = this.service.getUser(oldFriendship.getId().getRight());

        if (this.userComboBox.getValue().equals(friend1)) {
            this.friendsListView.getItems().remove(friend2);
        } else if (this.userComboBox.getValue().equals(friend2)) {
            this.friendsListView.getItems().remove(friend1);
        }
    }

    /**
     * Adds a new value to the combo box.
     *
     * @param newUser Value to be added.
     */
    private void addUserComboBox(User newUser) {
        this.userComboBox.getItems().add(newUser);
    }

    /**
     * Removes a value from the combo box.
     *
     * @param oldUser Value to be removed.
     */
    private void removeUserComboBox(User oldUser) {
        User selected = this.userComboBox.getValue();
        this.userComboBox.getItems().remove(oldUser);

        if (selected == null) {
            return;
        }

        if (selected.equals(oldUser)) {
            this.userComboBox.getSelectionModel().clearSelection();
            this.friendsListView.getItems().clear();
        }
    }

    /**
     * Updates a value in the combo box.
     *
     * @param oldUser Old value.
     * @param newUser New value.
     */
    private void updateUserComboBox(User oldUser, User newUser) {
        this.userComboBox.getItems().set(this.userComboBox.getItems().indexOf(oldUser), newUser);
        this.userComboAction();
    }
}
