package ro.ubbcluj.map.socialnetworkfx.controllers.UserInterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ro.ubbcluj.map.socialnetworkfx.controllers.AdminInterface.AdminController;
import ro.ubbcluj.map.socialnetworkfx.controllers.Controller;
import ro.ubbcluj.map.socialnetworkfx.controllers.PopupAlert;
import ro.ubbcluj.map.socialnetworkfx.entity.FriendRequest;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.FriendRequestEvent;
import ro.ubbcluj.map.socialnetworkfx.events.FriendshipEvent;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserEvent;
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.util.List;
import java.util.Optional;

public class FriendAreaController extends Controller implements Observer<SocialNetworkEvent> {
    @FXML
    private TableView<User> friendTableView;
    @FXML
    private TableColumn<User, String> firstNameColumn;
    @FXML
    private TableColumn<User, String> lastNameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TextField numberOfFriends;
    @FXML
    private ListView<User> friendRequestsListView;

    // Current page.
    int page = 0;

    // Number of items per page.
    int numberOfItems = -1;

    // User of the layout.
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void initController(Service service) {
        super.initController(service);

        // Initializing the model of the table view and the list view.
        this.initializeFriendModel();
    }

    private void initializeFriendModel() {
        // Enabling cell selection on the user table view.
        this.friendTableView.getSelectionModel().setCellSelectionEnabled(true);

        // Setting selection mode to multiple.
        this.friendTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Disabling the possibility of column resize over the grid.
        this.friendTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setting how each cell will build its contents.
        this.firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        this.emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Enabling CTRL-C copying.
        AdminController.enableCellCopy(this.friendTableView);

        if (this.service != null) {
            // Retrieving the user list as an observable one.
            // This occurs for the fact that anyone in the app may want to update itself based on the contents of the list.
            ObservableList<User> friendObservableList = FXCollections.observableList(this.service.getFriendsOf(user.getId()));

            // Setting the items of the table view based on the observable list contents.
            this.friendTableView.setItems(friendObservableList);

            // Setting the friend request list view.
            List<FriendRequest> friendRequests = this.service.getFriendRequestsOfUser(user.getId());
            List<User> friendRequestSenders = friendRequests.stream().map(friendRequest -> this.service.getUser(friendRequest.getIdFrom())).toList();
            this.friendRequestsListView.getItems().addAll(friendRequestSenders);
        }
    }

    private List<User> getFriendsFromPage(int pageNumber, int numberOfItems) {
        if (pageNumber < 0 || numberOfItems < 0) {
            return this.service.getFriendsOf(this.user.getId());
        }

        return this.service.getFriendsFromPage(pageNumber, numberOfItems, this.user);
    }

    public void numberOfFriendsAction() {
        // Resetting the page number.
        this.page = 0;
        // Resetting the number of items per page.
        int number = -1;

        // Parsing the number of users.
        try {
            int parsed = Integer.parseInt(this.numberOfFriends.getText());
            if (parsed >= 0) {
                number = parsed;
            }
        } catch (NumberFormatException numberFormatException) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", "Passed value is not a number.");
        }

        // Updating the table view and the number of items per page.
        List<User> userList = this.getFriendsFromPage(this.page, number);
        this.friendTableView.getItems().setAll(userList);
        this.numberOfItems = number;
    }

    public void previousPageAction() {
        // Getting the previous page number.
        int pageNumber = this.page - 1;

        // If the page number is negative, we cannot go to a previous page.
        if (pageNumber < 0) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", "Cannot go to a page with a negative index.");
            return;
        }

        // Getting the list of users from the previous page.
        List<User> userList = this.getFriendsFromPage(pageNumber, this.numberOfItems);

        // If the list is the same as the current one, we do not update the table view.
        if (userList.equals(this.friendTableView.getItems())) {
            return;
        }

        // Updating the table view.
        this.friendTableView.getItems().setAll(userList);
        this.page = pageNumber;
    }

    public void nextPageAction() {
        // Getting the next page number.
        int pageNumber = this.page + 1;

        // Getting the list of users from the next page.
        List<User> userList = this.getFriendsFromPage(pageNumber, this.numberOfItems);

        // If the list is empty, we cannot go to the next page.
        if (userList.isEmpty()) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", "Cannot go to a page with no users.");
            return;
        }

        // If the list is the same as the current one, we do not update the table view.
        if (userList.equals(this.friendTableView.getItems())) {
            return;
        }

        // Updating the table view.
        this.friendTableView.getItems().setAll(userList);
        this.page = pageNumber;
    }

    public void rejectFriendRequestAction() {
        // Verifying if a friend request was selected.
        if (this.friendRequestsListView.getSelectionModel().getSelectedItem() == null) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "No friend request selected!", "No friend request was selected!");
            return;
        }

        // Getting the selected users.
        List<User> selectedUsers = this.friendRequestsListView.getSelectionModel().getSelectedItems().stream().toList();

        // Clearing the selection so that the selection model is not in an invalid state.
        this.friendRequestsListView.getSelectionModel().clearSelection();

        selectedUsers.forEach(selectedUser -> {
            // Getting the friend request.
            Optional<FriendRequest> friendRequestOptional = this.service.getFriendRequestsOfUser(user.getId()).stream()
                    .filter(friendRequest -> friendRequest.getIdFrom().equals(selectedUser.getId()))
                    .findFirst();

            // Removing the friend request from the database.
            friendRequestOptional.ifPresent(friendRequest -> this.service.rejectFriendRequest(friendRequest));
        });

        PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "Friend request(s) rejected!", "");
    }

    public void acceptFriendRequestAction() {
        // Verifying if a friend request was selected.
        if (this.friendRequestsListView.getSelectionModel().getSelectedItem() == null) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "No friend request selected!", "No friend request was selected!");
            return;
        }

        // Getting the selected users.
        List<User> selectedUsers = this.friendRequestsListView.getSelectionModel().getSelectedItems().stream().toList();

        // Clearing the selection so that the selection model is not in an invalid state.
        this.friendRequestsListView.getSelectionModel().clearSelection();

        selectedUsers.forEach(selectedUser -> {
            // Getting the friend request.
            Optional<FriendRequest> friendRequestOptional = this.service.getFriendRequestsOfUser(user.getId()).stream()
                    .filter(friendRequest -> friendRequest.getIdFrom().equals(selectedUser.getId()))
                    .findFirst();

            // Removing the friend request from the database.
            friendRequestOptional.ifPresent(friendRequest -> this.service.acceptFriendRequest(friendRequest));
        });

        PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "Friend request(s) accepted!", "");
    }

    public void removeFriendAction() {
        // Verifying if a friend was selected.
        if (this.friendTableView.getSelectionModel().getSelectedItem() == null) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "No friend selected!", "No friend was selected!");
            return;
        }

        // Getting the selected users.
        List<User> selectedUsers = this.friendTableView.getSelectionModel().getSelectedItems().stream().toList();

        // Clearing the selection so that the selection model is not in an invalid state.
        this.friendTableView.getSelectionModel().clearSelection();

        selectedUsers.forEach(selectedUser -> {
            // Removing the friend from the friend list.
            this.service.removeFriendship(user.getId(), selectedUser.getId());
        });

        PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "Friend(s) removed!", "");
    }

    @Override
    public void update(SocialNetworkEvent event) {
        if(event.getClass().equals(UserEvent.class)) {
            UserEvent userEvent = (UserEvent) event;

            switch (userEvent.getEventType()) {
                case ADD_USER -> {
                    if (this.service.getFriendsOf(user.getId()).contains(userEvent.getNewUser())) {
                        this.friendTableView.getItems().add(userEvent.getNewUser());
                    }
                }
                case REMOVE_USER -> {
                    if (this.service.getFriendsOf(user.getId()).contains(userEvent.getOldUser())) {
                        this.friendTableView.getItems().remove(userEvent.getOldUser());
                    }
                }
                case UPDATE_USER -> {
                    if (this.service.getFriendsOf(user.getId()).contains(userEvent.getOldUser())) {
                        this.friendTableView.getItems().set(this.friendTableView.getItems().indexOf(userEvent.getOldUser()), userEvent.getNewUser());
                    }
                }
            }
            return;
        }
        if (event.getClass().equals(FriendshipEvent.class)) {
            FriendshipEvent friendshipEvent = (FriendshipEvent) event;
            switch (friendshipEvent.getEventType()) {
                case ADD_FRIENDSHIP -> {
                    if (friendshipEvent.getNewFriendship().getLeft().equals(this.user.getId())) {
                        this.friendTableView.getItems().add(this.service.getUser(friendshipEvent.getNewFriendship().getRight()));
                    } else if (friendshipEvent.getNewFriendship().getRight().equals(this.user.getId())) {
                        this.friendTableView.getItems().add(this.service.getUser(friendshipEvent.getNewFriendship().getLeft()));
                    }
                }
                case REMOVE_FRIENDSHIP -> {
                    if (friendshipEvent.getOldFriendship().getLeft().equals(this.user.getId())) {
                        this.friendTableView.getItems().remove(this.service.getUser(friendshipEvent.getOldFriendship().getRight()));
                    } else if (friendshipEvent.getOldFriendship().getRight().equals(this.user.getId())) {
                        this.friendTableView.getItems().remove(this.service.getUser(friendshipEvent.getOldFriendship().getLeft()));
                    }
                }
            }
            return;
        }
        if (event.getClass().equals(FriendRequestEvent.class)) {
            FriendRequestEvent friendRequestEvent = (FriendRequestEvent) event;
            switch (friendRequestEvent.getEventType()) {
                case ADD_FRIEND_REQUEST -> {
                    if (friendRequestEvent.getNewFriendRequest().getIdTo().equals(this.user.getId())) {
                        this.friendRequestsListView.getItems().add(this.service.getUser(friendRequestEvent.getNewFriendRequest().getIdFrom()));
                    }
                }
                case REMOVE_FRIEND_REQUEST -> {
                    if (friendRequestEvent.getOldFriendRequest().getIdTo().equals(this.user.getId())) {
                        this.friendRequestsListView.getItems().remove(this.service.getUser(friendRequestEvent.getOldFriendRequest().getIdFrom()));
                    }
                }
            }
        }
    }
}
