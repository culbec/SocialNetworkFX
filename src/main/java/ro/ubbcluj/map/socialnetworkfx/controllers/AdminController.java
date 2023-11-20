package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkfx.SocialNetworkApplication;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserChangeEvent;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendshipDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.UserDBRepository;
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.utility.PopupEnum;
import ro.ubbcluj.map.socialnetworkfx.utility.RandomUserGenerator;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for the admin interface of the social network.
 */
public class AdminController implements Initializable, Observer<SocialNetworkEvent> {
    // Popup headers and texts.
    private final HashMap<PopupEnum, Tuple<String, String>> popups = new HashMap<>();
    // FXML elements.
    @FXML
    public TableView<User> userTableView;
    @FXML
    public TableColumn<User, UUID> userID;
    @FXML
    public TableColumn<User, String> userFirstName;
    @FXML
    public TableColumn<User, String> userLastName;
    @FXML
    public TableColumn<User, String> userEmail;
    @FXML
    public Button userAddButton;
    @FXML
    public Button userRemoveButton;
    @FXML
    public Button userUpdateButton;
    @FXML
    public TableView<User> friendsTableView;
    @FXML
    public TableColumn<User, String> userFriend;
    @FXML
    public TableColumn<LocalDateTime, LocalDateTime> friendshipDate;
    @FXML
    public Button friendshipAddButton;
    @FXML
    public Button friendshipRemoveButton;
    // Service dependency.
    private Service service;

    /**
     * Adds headers and texts for the popup alerts.
     *
     * @param hashMap {@code HashMap} that contains identifiers, along with headers and texts for the popup alerts.
     */
    private static void addPopups(HashMap<PopupEnum, Tuple<String, String>> hashMap) {
        hashMap.put(PopupEnum.EMPTY_TABLE_EXCEPTION, new Tuple<>("Empty Table", "The user table is empty!"));
        hashMap.put(PopupEnum.NONE_SELECTED_EXCEPTION, new Tuple<>("No Selection Was Made", "No users were selected!"));
        hashMap.put(PopupEnum.REMOVE_USER_EXCEPTION, new Tuple<>("Remove Exception", null));
        hashMap.put(PopupEnum.REMOVE_USER_SUCCESS, new Tuple<>("Removed Successfully!\nRemoved users are listed below", ""));
        hashMap.put(PopupEnum.ADD_USER_EXCEPTION, new Tuple<>("Add Exception", null));
        hashMap.put(PopupEnum.ADD_USER_SUCCESS, new Tuple<>("Added Successfully!", null));
        hashMap.put(PopupEnum.UPDATE_USER_EXCEPTION, new Tuple<>("Update Exception", null));
        hashMap.put(PopupEnum.UPDATE_USER_SUCCESS, new Tuple<>("Update Successfully!", null));
    }

    /**
     * Retrieving content from a table view by copying it.
     *
     * @param tableView Table view to copy data from.
     * @return Copied content.
     */
    private static ClipboardContent getClipboardContent(TableView<?> tableView) {
        // Retrieving the selected positions in the table.
        var positions = tableView.getSelectionModel().getSelectedCells();

        // String builder that will build our copied contents.
        StringBuilder stringBuilder = new StringBuilder();

        positions.forEach(position -> {
            // Selecting the row of the selected item in the table view.
            int rowSelected = position.getRow();

            // Retrieving its value from the table view.
            var observableValue = position.getTableColumn().getCellObservableValue(rowSelected);

            stringBuilder.append(observableValue.getValue().toString()).append("\t");
        });

        final ClipboardContent clipboardContent = new ClipboardContent();

        // Putting the result in a clipboard content.
        clipboardContent.putString(stringBuilder.toString());

        return clipboardContent;
    }

    /**
     * Enables cell copy for a specific table view.
     *
     * @param tableView Table view to copy data from.
     */
    private static void enableCellCopy(TableView<?> tableView) {
        // Retrieving the clipboard content.
        final ClipboardContent clipboardContent = getClipboardContent(tableView);

        // Setting the content of the system clipboard.
        Clipboard.getSystemClipboard().setContent(clipboardContent);

        // Actually setting the keycode combination to enable CTRL-C copying.
        final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);

        // Adding a listener to the table view for copying the values on each pressing.
        // After copying the values from the table view, the selection will be cleared.
        tableView.setOnKeyPressed(event -> {
            if (keyCodeCopy.match(event)) {
                enableCellCopy(tableView);
                tableView.getSelectionModel().clearSelection();
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Specifying the database.
        String DB_URL = "jdbc:postgresql://localhost:5432/socialnetwork";
        String USERNAME = "postgres";
        String PASSWORD = "postgres";

        // Initializing the database repositories.
        UserDBRepository userDBRepository = new UserDBRepository(DB_URL, USERNAME, PASSWORD);
        FriendshipDBRepository friendshipDBRepository = new FriendshipDBRepository(DB_URL, USERNAME, PASSWORD);

        // Initializing the service.
        this.service = new Service(userDBRepository, friendshipDBRepository);

        // Trying to add 20 new users if there are less than 10 users.
        if (this.service.getUsers().size() < 10) {
            ArrayList<User> users = RandomUserGenerator.generate20Users();

            users.forEach(user -> {
                try {
                    this.service.addUser(user.getFirstName(), user.getLastName(), user.getEmail());
                } catch (ServiceException sE) {
                    System.err.println(sE.getMessage());
                }
            });
        }

        // Adding the controller as an observer.
        this.service.addObserver(this);

        // Initializing the model for the user table view.
        this.initializeUserModel();

        // Enabling CTRL-C for the user table view.
        enableCellCopy(this.userTableView);

        // Adding popup headers and texts.
        addPopups(this.popups);
    }

    /**
     * Initializes the user table view model.
     */
    private void initializeUserModel() {
        // Enabling cell selection on the user table view.
        this.userTableView.getSelectionModel().setCellSelectionEnabled(true);

        // Setting selection mode to multiple.
        this.userTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Disabling the posibility of column resize.
        this.userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setting how each cell will build its contents.
        this.userID.setCellValueFactory(new PropertyValueFactory<>("id"));
        this.userFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.userLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        this.userEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Retrieving the user list as an observable one.
        // This occurs for the fact that anyone in the app may want to update itself based on the contents of the list.
        ObservableList<User> userObservableList = FXCollections.observableList(this.service.getUsers());

        // Setting the items of the table view based on the observable list contents.
        this.userTableView.setItems(userObservableList);
    }

    /**
     * Checking if the table view is empty.
     *
     * @return Boolean value which indicates if the table view is empty or not.
     */
    private boolean checkUserTableViewEmpty() {
        if (this.userTableView.getItems().isEmpty()) {
            PopupEnum identifier = PopupEnum.EMPTY_TABLE_EXCEPTION;
            String header = this.popups.get(identifier).getLeft();
            String text = this.popups.get(identifier).getRight();
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
            return true;
        }
        return false;
    }

    /**
     * Checking if the selection model made a selection or not.
     *
     * @return Boolean value which indicates if the selection model made a selection or not.
     */
    private boolean checkUserSelectionEmpty() {
        if (this.userTableView.getSelectionModel().isEmpty()) {
            PopupEnum identifier = PopupEnum.NONE_SELECTED_EXCEPTION;
            String header = this.popups.get(identifier).getLeft();
            String text = this.popups.get(identifier).getRight();
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
            return true;
        }
        return false;
    }

    /**
     * Action based on the pressing of the 'add' button for the users.
     *
     * @throws IOException If the {@code FXMLLoader} couldn't load the scene.
     */
    @FXML
    public void userAddAction() throws IOException {
        // Loading the scene for showing.
        FXMLLoader userAddFXMLLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user-add-dialogue.fxml"));
        Scene userAddScene = new Scene(userAddFXMLLoader.load());

        // Retrieving the controller of the dialogue.
        UserAddDialogue userAddDialogue = userAddFXMLLoader.getController();

        // Preparing the stage for showing.
        Stage userAddStage = new Stage();
        userAddStage.setScene(userAddScene);

        // Handle for the case of 'X' button pressing.
        userAddStage.setOnCloseRequest(event -> userAddDialogue.handleCancel());

        // Showing and waiting for execution.
        userAddStage.showAndWait();

        // Verifying if the user pressed the cancel button.
        if (userAddDialogue.isCancelled()) {
            return;
        }

        // Retrieving data from the dialogue.
        Map<String, String> values = userAddDialogue.handleAdd();
        String firstName = values.get("firstName");
        String lastName = values.get("lastName");
        String email = values.get("email");

        try {
            // Adding the user.
            this.service.addUser(firstName, lastName, email);

            // Showing a message of success.
            PopupEnum identifier = PopupEnum.ADD_USER_SUCCESS;
            String header = this.popups.get(identifier).getLeft();
            String text = "";
            PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, header, text);
        } catch (ServiceException sE) {
            // Showing a message of error.
            PopupEnum identifier = PopupEnum.ADD_USER_EXCEPTION;
            String header = this.popups.get(identifier).getLeft();
            String text = sE.getMessage();
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
        }
    }

    /**
     * Returns information about the removed users and removes selected users.
     *
     * @return Information about the removed users.
     */
    private StringBuilder getRemovedUsersInformation() {
        StringBuilder stringBuilder = new StringBuilder();

        // Retrieving a copy of the selected items so that we don't modify a list that is iterated.
        List<User> selectedItemsCopy = new ArrayList<>(this.userTableView.getSelectionModel().getSelectedItems());

        // Removing all the selected users.
        selectedItemsCopy.forEach(user -> {
            User removed = this.service.removeUser(user.getId());
            stringBuilder.append(removed.toString()).append("\n");
        });

        // Clearing the selection.
        this.userTableView.getSelectionModel().clearSelection();

        return stringBuilder;
    }

    /**
     * Action based on the pressing of the 'remove' button for the users.
     */
    @FXML
    public void userRemoveAction() {
        // Verifying if the table view is empty -> we cannot remove users.
        if (this.checkUserTableViewEmpty()) {
            return;
        }

        // Verifying if the table view made no selection -> cannot remove unselected users.
        if (this.checkUserSelectionEmpty()) {
            return;
        }

        // Removing the users and retrieving the information about them.
        StringBuilder stringBuilder = getRemovedUsersInformation();

        // Showing a success message.
        PopupEnum identifier = PopupEnum.REMOVE_USER_SUCCESS;
        String header = this.popups.get(identifier).getLeft();
        PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, header, stringBuilder.toString());
    }

    /**
     * Action based on the pressing of the 'update' button for the users.
     *
     * @throws IOException If the {@code FXMLLoader} couldn't load the scene.
     */
    @FXML
    public void userUpdateAction() throws IOException {
        // Verifying if the table view is empty -> we cannot update users.
        if (this.checkUserTableViewEmpty()) {
            return;
        }

        // Verifying if the table view made no selection -> cannot update unselected users.
        if (this.checkUserSelectionEmpty()) {
            return;
        }

        // Loading the scene.
        FXMLLoader userUpdateFXMLLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user-update-dialogue.fxml"));
        Scene userUpdateScene = new Scene(userUpdateFXMLLoader.load());

        // Retrieving a copy of the selected items so that we don't modify a list that is iterated.
        List<User> selectedItemsCopy = new ArrayList<>(this.userTableView.getSelectionModel().getSelectedItems());

        // Updating each user.
        selectedItemsCopy.forEach(user -> {
            // Retrieving the controller of the dialogue.
            UserUpdateDialogue userUpdateDialogue = userUpdateFXMLLoader.getController();

            // Setting text & prompt fields for each field in the dialogue.
            userUpdateDialogue.setFields(user.getId().toString(), user.getFirstName(), user.getLastName(), user.getEmail());

            // Preparing the stage for showing.
            Stage userUpdateStage = new Stage();
            userUpdateStage.setScene(userUpdateScene);

            // Handle for the case of 'X' button pressing.
            userUpdateStage.setOnCloseRequest(event -> userUpdateDialogue.handleCancel());

            // Showing and waiting for execution.
            userUpdateStage.showAndWait();

            // Checking if the cancel button was pressed.
            if (userUpdateDialogue.isCancelled()) {
                return;
            }

            // Retrieving data from the dialogue.
            Map<String, String> values = userUpdateDialogue.handleUpdate();
            String id = values.get("id");
            String newFirstName = values.get("firstName");
            String newLastName = values.get("lastName");
            String newEmail = values.get("email");

            try {
                // Updating the user.
                User old = this.service.updateUser(new User(UUID.fromString(id), newFirstName, newLastName, newEmail));

                // Showing a message of success.
                PopupEnum identifier = PopupEnum.UPDATE_USER_SUCCESS;
                String header = this.popups.get(identifier).getLeft();
                String text = "Old user: " + old.toString() + "\nNew user: " + this.service.getUser(old.getId());
                PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
            } catch (ServiceException sE) {
                // Showing a message of error.
                PopupEnum identifier = PopupEnum.UPDATE_USER_EXCEPTION;
                String header = this.popups.get(identifier).getLeft();
                String text = sE.getMessage();
                PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
            }
        });
    }

    @Override
    public void update(SocialNetworkEvent event) {
        // Checking which type of event occurred.
        if (event.getClass().equals(UserChangeEvent.class)) {
            // Casting to the corresponding event.
            UserChangeEvent userChangeEvent = (UserChangeEvent) event;

            // Running an action based on the type of event occurred.
            switch (userChangeEvent.getEventType()) {
                case ADD_USER -> this.addUserRow(userChangeEvent.getNewUser());
                case UPDATE_USER -> this.updateUserRow(userChangeEvent.getOldUser(), userChangeEvent.getNewUser());
                case REMOVE_USER -> this.removeUserRow(userChangeEvent.getOldUser());
            }
        }
    }

    /**
     * Adds a new user to the table view.
     *
     * @param userAdded User that was added.
     */
    private void addUserRow(User userAdded) {
        this.userTableView.getItems().add(userAdded);
    }

    /**
     * Updating a user in the table view.
     *
     * @param userOld User that was updated.
     * @param userNew New user.
     */
    private void updateUserRow(User userOld, User userNew) {
        // Retrieving the index in the table view of the old user.
        int index = this.userTableView.getItems().indexOf(userOld);

        // Setting the data of the item at 'index' to 'userNew'.
        this.userTableView.getItems().set(index, userNew);
    }

    /**
     * Removing a user from the table view.
     *
     * @param userRemoved User that was removed.
     */
    private void removeUserRow(User userRemoved) {
        this.userTableView.getItems().remove(userRemoved);
    }
}