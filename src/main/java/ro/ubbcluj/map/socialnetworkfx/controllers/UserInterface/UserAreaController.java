package ro.ubbcluj.map.socialnetworkfx.controllers.UserInterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ro.ubbcluj.map.socialnetworkfx.controllers.AdminInterface.AdminController;
import ro.ubbcluj.map.socialnetworkfx.controllers.PopupAlert;
import ro.ubbcluj.map.socialnetworkfx.entity.Message;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserEvent;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.service.*;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserAreaController implements Observer<SocialNetworkEvent> {
    // Current page.
    int page = 0;
    // Number of items per page.
    int numberOfItems = -1;
    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, String> firstNameColumn;
    @FXML
    private TableColumn<User, String> lastNameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TextArea messageTextArea;
    @FXML
    private TextField numberOfUsers;
    // User of the layout.
    private User user;
    // Service dependencies.
    private ServiceUser serviceUser;
    private ServiceFriendRequest serviceFriendRequest;
    private ServiceMessage serviceMessage;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void initController(IService serviceUser, IService serviceFriendRequest, IService serviceMessage) {
        // Setting the service dependencies.
        this.serviceUser = (ServiceUser) serviceUser;
        this.serviceFriendRequest = (ServiceFriendRequest) serviceFriendRequest;
        this.serviceMessage = (ServiceMessage) serviceMessage;

        // Initializing the model of the table view.
        this.initializeUserModel();
    }

    private void initializeUserModel() {
        // Enabling cell selection on the user table view.
        this.userTableView.getSelectionModel().setCellSelectionEnabled(true);

        // Setting selection mode to multiple.
        this.userTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Disabling the posibility of column resize over the grid.
        this.userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setting how each cell will build its contents.
        this.firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        this.emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Enabling CTRL-C copying.
        AdminController.enableCellCopy(this.userTableView);

        if (this.serviceUser != null) {
            // Retrieving the user list as an observable one.
            // This occurs for the fact that anyone in the app may want to update itself based on the contents of the list.
            ObservableList<User> userObservableList = FXCollections.observableList(this.serviceUser.getUsers());

            // Setting the items of the table view based on the observable list contents.
            this.userTableView.setItems(userObservableList);
        }
    }

    public void sendMessageAction() {
        // Managing the case when the text area is empty.
        if (this.messageTextArea.getText().isEmpty()) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Cannot send an empty message!", "");
            return;
        }

        // Managing the case when no users where selected.
        if (this.userTableView.getSelectionModel().getSelectedItems().isEmpty()) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "No users were selected!", "Cannot send a message if no users are selected.");
            return;
        }

        List<UUID> ids = new ArrayList<>();
        this.userTableView.getSelectionModel().getSelectedItems().forEach(userSelected -> ids.add(userSelected.getId()));

        Message message = new Message(this.user.getId(), ids, messageTextArea.getText());
        try {
            this.serviceMessage.sendMessage(message);
            PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "Message sent successfully!", "");
        } catch (ServiceException sE) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", sE.getMessage());
        }
    }

    private List<User> getUsersFromPage(int pageNumber, int numberOfItems) {
        if (pageNumber < 0 || numberOfItems < 0) {
            return this.serviceUser.getUsers();
        }

        return this.serviceUser.getUsersFromPage(pageNumber, numberOfItems);
    }

    public void numberOfUsersAction() {
        // Resetting the page number.
        this.page = 0;
        // Resetting the number of items per page.
        int number = -1;

        // Parsing the number of users.
        try {
            int parsed = Integer.parseInt(this.numberOfUsers.getText());
            if (parsed >= 0) {
                number = parsed;
            }
        } catch (NumberFormatException numberFormatException) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", "Passed value is not a number.");
        }

        // Updating the table view and the number of items per page.
        List<User> userList = this.getUsersFromPage(this.page, number);
        this.userTableView.getItems().setAll(userList);
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
        List<User> userList = this.getUsersFromPage(pageNumber, this.numberOfItems);

        // If the list is the same as the current one, we do not update the table view.
        if (userList.equals(this.userTableView.getItems())) {
            return;
        }

        // Updating the table view.
        this.userTableView.getItems().setAll(userList);
        this.page = pageNumber;
    }

    public void nextPageAction() {
        // Getting the next page number.
        int pageNumber = this.page + 1;

        // Getting the list of users from the next page.
        List<User> userList = this.getUsersFromPage(pageNumber, this.numberOfItems);

        // If the list is empty, we cannot go to the next page.
        if (userList.isEmpty()) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", "Cannot go to a page with no users.");
            return;
        }

        // If the list is the same as the current one, we do not update the table view.
        if (userList.equals(this.userTableView.getItems())) {
            return;
        }

        // Updating the table view.
        this.userTableView.getItems().setAll(userList);
        this.page = pageNumber;
    }

    public void sendFriendRequestAction() {
        // Managing the case when no users where selected.
        if (this.userTableView.getSelectionModel().getSelectedItems().isEmpty()) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "No users were selected!", "Cannot send a friend request if no users are selected.");
            return;
        }

        List<User> selectedUsers = this.userTableView.getSelectionModel().getSelectedItems();

        selectedUsers.forEach(selectedUser -> {
            try {
                this.serviceFriendRequest.sendFriendRequest(this.user, selectedUser);
                PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, "Friend request(s) sent successfully!", "");
            } catch (ServiceException sE) {
                PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", sE.getMessage());
            }
        });
    }

    @Override
    public void update(SocialNetworkEvent event) {
        if (event.getClass().equals(UserEvent.class)) {
            UserEvent userEvent = (UserEvent) event;

            switch (userEvent.getEventType()) {
                case ADD_USER -> this.userTableView.getItems().add(userEvent.getNewUser());
                case REMOVE_USER -> {
                    this.userTableView.getItems().remove(userEvent.getOldUser());

                    // Going to a previous page if the current one is empty.
                    if (this.userTableView.getItems().isEmpty()) {
                        this.previousPageAction();
                    }
                }
                case UPDATE_USER ->
                        this.userTableView.getItems().set(this.userTableView.getItems().indexOf(userEvent.getOldUser()), userEvent.getNewUser());
            }
        }
    }
}
