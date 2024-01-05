package ro.ubbcluj.map.socialnetworkfx.controllers.UserInterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ro.ubbcluj.map.socialnetworkfx.controllers.AdminInterface.AdminController;
import ro.ubbcluj.map.socialnetworkfx.controllers.PopupAlert;
import ro.ubbcluj.map.socialnetworkfx.entity.Message;
import ro.ubbcluj.map.socialnetworkfx.entity.ReplyMessage;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.FriendshipEvent;
import ro.ubbcluj.map.socialnetworkfx.events.MessageEvent;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserEvent;
import ro.ubbcluj.map.socialnetworkfx.service.IService;
import ro.ubbcluj.map.socialnetworkfx.service.ServiceFriendship;
import ro.ubbcluj.map.socialnetworkfx.service.ServiceMessage;
import ro.ubbcluj.map.socialnetworkfx.service.ServiceUser;
import ro.ubbcluj.map.socialnetworkfx.utility.MessageDTO;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.util.List;
import java.util.UUID;

public class MessageAreaController implements Observer<SocialNetworkEvent> {
    // Current page.
    int page = 0;
    // Number of items per page.
    int numberOfItems = -1;
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
    private ListView<MessageDTO> messageListView;
    @FXML
    private TextArea messageTextArea;
    // User of the layout.
    private User user;
    // Service dependencies.
    private ServiceUser serviceUser;
    private ServiceFriendship serviceFriendship;
    private ServiceMessage serviceMessage;

    public void setUser(User user) {
        this.user = user;
    }

    public void initController(IService serviceUser, IService serviceFriendship, IService serviceMessage) {
        // Setting the service dependencies.
        this.serviceUser = (ServiceUser) serviceUser;
        this.serviceFriendship = (ServiceFriendship) serviceFriendship;
        this.serviceMessage = (ServiceMessage) serviceMessage;

        // Initializing the model of the table view and the list view.
        this.initializeFriendModel();
    }

    private void initializeFriendModel() {
        // Enabling cell selection on the user table view.
        this.friendTableView.getSelectionModel().setCellSelectionEnabled(true);

        // Setting the selection policy of the messages to be single selection for the message list view.
        this.messageListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Setting selection mode to single for the friend list view.
        this.friendTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Disabling the possibility of column resize over the grid.
        this.friendTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setting how each cell will build its contents.
        this.firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        this.emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Enabling CTRL-C copying.
        AdminController.enableCellCopy(this.friendTableView);

        // Retrieving the user list as an observable one.
        // This occurs for the fact that anyone in the app may want to update itself based on the contents of the list.
        List<UUID> friendIds = this.serviceFriendship.getFriendIdsOf(user.getId());
        List<User> friends = this.serviceUser.getFriends(friendIds);
        ObservableList<User> friendObservableList = FXCollections.observableArrayList(friends);

        // Setting the items of the table view based on the observable list contents.
        this.friendTableView.setItems(friendObservableList);

        // Setting the behavior of user selection in the table.
        this.friendTableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldUser, newUser) -> {
            // If the new user is null, then we do not update the list view.
            if (newUser == null) {
                return;
            }

            this.initializeMessageModel(newUser);

        });
    }

    /**
     * Initializes the message model to contain messages between {@code a selected user and} {@code our user}.
     *
     * @param selectedUser User selected in the friend table.
     */
    private void initializeMessageModel(User selectedUser) {
        // Getting the list of messages between the current user and the selected user.
        List<Message> messageList = this.serviceMessage.getMessagesBetweenUsers(this.user, selectedUser);

        // Clearing the list view.
        this.messageListView.getItems().clear();

        // Adding the messages to the list view.
        for (Message message : messageList) {
            MessageDTO messageDTO = new MessageDTO(this.serviceUser.getUser(message.getFrom()), message);
            this.messageListView.getItems().add(messageDTO);
        }
    }

    private List<User> getFriendsFromPage(int pageNumber, int numberOfItems) {
        if (pageNumber < 0 || numberOfItems < 0) {
            return this.serviceUser.getFriends(this.serviceFriendship.getFriendIdsOf(user.getId()));
        }

        return this.serviceFriendship.getFriendsFromPage(pageNumber, numberOfItems, this.user);
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

    public void replyAction() {
        // If no users were selected, then it does nothing.
        if (this.friendTableView.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        // If the message text area is empty, then it does nothing.
        if (this.messageTextArea.getText().isEmpty()) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Something went wrong!", "Cannot send an empty message.");
            return;
        }

        // Case when the user only replies to a user, not to a message.
        if (this.messageListView.getSelectionModel().getSelectedItem() == null) {
            // Getting the selected user.
            User selectedUser = this.friendTableView.getSelectionModel().getSelectedItem();

            // Creating the message.
            Message message = new Message(this.user.getId(), List.of(selectedUser.getId()), this.messageTextArea.getText());

            // Sending the message.
            this.serviceMessage.sendMessage(message);

            // Clearing the message text area.
            this.messageTextArea.clear();

            return;
        }

        // If a message is selected, then we reply to that message.
        Message selectedMessage = this.messageListView.getSelectionModel().getSelectedItem().getMessage();

        // Creating the reply message.
        ReplyMessage replyMessage = new ReplyMessage(this.user.getId(), List.of(selectedMessage.getFrom()), this.messageTextArea.getText(), selectedMessage.getId());

        // Sending the reply message.
        this.serviceMessage.sendMessage(replyMessage);

        // Clearing the message text area.
        this.messageTextArea.clear();
    }

    @Override
    public void update(SocialNetworkEvent event) {
        if (event.getClass().equals(UserEvent.class)) {
            UserEvent userEvent = (UserEvent) event;

            switch (userEvent.getEventType()) {
                case ADD_USER -> {
                    if (this.serviceFriendship.getFriendIdsOf(user.getId()).contains(userEvent.getNewUser().getId())) {
                        this.friendTableView.getItems().add(userEvent.getNewUser());
                    }
                }
                case REMOVE_USER -> {
                    if (this.serviceFriendship.getFriendIdsOf(user.getId()).contains(userEvent.getOldUser().getId())) {
                        if (this.friendTableView.getSelectionModel().getSelectedItem().equals(userEvent.getOldUser())) {
                            this.messageListView.getItems().clear();
                        }
                        this.friendTableView.getItems().remove(userEvent.getOldUser());
                    }
                }
                case UPDATE_USER -> {
                    if (this.serviceFriendship.getFriendIdsOf(user.getId()).contains(userEvent.getNewUser().getId())) {
                        this.friendTableView.getItems().set(this.friendTableView.getItems().indexOf(userEvent.getOldUser()), userEvent.getNewUser());

                        if (this.friendTableView.getSelectionModel().getSelectedItem().equals(userEvent.getNewUser())) {
                            // Updating the users for each DTO that contains the specific user if that user is selected.
                            this.messageListView.getItems().stream()
                                    .filter(messageDTO -> messageDTO.getUser().equals(userEvent.getOldUser()))
                                    .forEach(messageDTO -> messageDTO.setUser(userEvent.getNewUser()));

                            // Refreshing the list view to present the changes.
                            this.messageListView.refresh();
                        }
                    }
                }
            }
            return;
        }
        if (event.getClass().equals(FriendshipEvent.class)) {
            FriendshipEvent friendshipEvent = (FriendshipEvent) event;
            switch (friendshipEvent.getEventType()) {
                case ADD_FRIENDSHIP -> {
                    if (friendshipEvent.getNewFriendship().getId().getLeft().equals(this.user.getId())) {
                        this.friendTableView.getItems().add(this.serviceUser.getUser(friendshipEvent.getNewFriendship().getId().getRight()));
                    } else if (friendshipEvent.getNewFriendship().getId().getRight().equals(this.user.getId())) {
                        this.friendTableView.getItems().add(this.serviceUser.getUser(friendshipEvent.getNewFriendship().getId().getLeft()));
                    }
                }
                case REMOVE_FRIENDSHIP -> {
                    User selectedUser = this.friendTableView.getSelectionModel().getSelectedItem();
                    if (friendshipEvent.getOldFriendship().getId().getLeft().equals(this.user.getId())) {
                        // Clearing the message list view if the removed friend was selected.
                        if (selectedUser != null && selectedUser.getId().equals(friendshipEvent.getOldFriendship().getId().getRight())) {
                            this.messageListView.getItems().clear();
                        }
                        this.friendTableView.getItems().remove(this.serviceUser.getUser(friendshipEvent.getOldFriendship().getId().getRight()));
                    } else if (friendshipEvent.getOldFriendship().getId().getRight().equals(this.user.getId())) {
                        // Clearing the message list view if the removed friend was selected.
                        if (selectedUser != null && selectedUser.getId().equals(friendshipEvent.getOldFriendship().getId().getLeft())) {
                            this.messageListView.getItems().clear();
                        }
                        this.friendTableView.getItems().remove(this.serviceUser.getUser(friendshipEvent.getOldFriendship().getId().getLeft()));
                    }
                }
            }
            return;
        }
        if (event.getClass().equals(MessageEvent.class)) {
            MessageEvent messageEvent = (MessageEvent) event;

            if (messageEvent.getMessage().getTo().contains(this.user.getId()) || messageEvent.getMessage().getFrom().equals(this.user.getId())) {
                MessageDTO messageDTO = new MessageDTO(this.serviceUser.getUser(messageEvent.getMessage().getFrom()), messageEvent.getMessage());
                this.messageListView.getItems().add(messageDTO);
            }
        }
    }
}
