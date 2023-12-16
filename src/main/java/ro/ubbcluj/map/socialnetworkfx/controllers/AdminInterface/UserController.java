package ro.ubbcluj.map.socialnetworkfx.controllers.AdminInterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkfx.SocialNetworkApplication;
import ro.ubbcluj.map.socialnetworkfx.controllers.PopupAlert;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserEvent;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.utility.PopupEnum;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserController implements Observer<SocialNetworkEvent> {
    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, UUID> userID;
    @FXML
    private TableColumn<User, String> userFirstName;
    @FXML
    private TableColumn<User, String> userLastName;
    @FXML
    private TableColumn<User, String> userEmail;

    private Service service;

    /**
     * Setting the service and initializes the model for the table view.
     *
     * @param service Service to be set.
     */
    public void setService(Service service) {
        this.service = service;
    }

    public void initController(Service service) {
        this.setService(service);
        this.initializeUserModel();
    }

    /**
     * Initializes the user table view model.
     */
    private void initializeUserModel() {
        // Enabling cell selection on the user table view.
        this.userTableView.getSelectionModel().setCellSelectionEnabled(true);

        // Setting selection mode to multiple.
        this.userTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Disabling the posibility of column resize over the grid.
        this.userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setting how each cell will build its contents.
        this.userID.setCellValueFactory(new PropertyValueFactory<>("id"));
        this.userFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.userLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        this.userEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Enabling CTRL-C copying.
        AdminController.enableCellCopy(this.userTableView);

        if (this.service != null) {
            // Retrieving the user list as an observable one.
            // This occurs for the fact that anyone in the app may want to update itself based on the contents of the list.
            ObservableList<User> userObservableList = FXCollections.observableList(this.service.getUsers());

            // Setting the items of the table view based on the observable list contents.
            this.userTableView.setItems(userObservableList);
        }
    }

    /**
     * Checking if the table view is empty.
     *
     * @return Boolean value which indicates if the table view is empty or not.
     */
    private boolean checkUserTableViewEmpty() {
        if (this.userTableView.getItems().isEmpty()) {
            PopupEnum identifier = PopupEnum.EMPTY_TABLE_EXCEPTION;
            String header = AdminController.getPopups().get(identifier).getLeft();
            String text = AdminController.getPopups().get(identifier).getRight();
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
            String header = AdminController.getPopups().get(identifier).getLeft();
            String text = AdminController.getPopups().get(identifier).getRight();
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
        String password = values.get("password");

        try {
            // Adding the user.
            this.service.addUser(firstName, lastName, email, password);

            // Showing a message of success.
            PopupEnum identifier = PopupEnum.ADD_USER_SUCCESS;
            String header = AdminController.getPopups().get(identifier).getLeft();
            String text = "";
            PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, header, text);
        } catch (ServiceException sE) {
            // Showing a message of error.
            PopupEnum identifier = PopupEnum.ADD_USER_EXCEPTION;
            String header = AdminController.getPopups().get(identifier).getLeft();
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
        String header = AdminController.getPopups().get(identifier).getLeft();
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
            userUpdateDialogue.setFields(user.getId().toString(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());

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
            String password = values.get("password");

            try {
                // Updating the user.
                User old = this.service.updateUser(new User(UUID.fromString(id), newFirstName, newLastName, newEmail, password));

                // Showing a message of success.
                PopupEnum identifier = PopupEnum.UPDATE_USER_SUCCESS;
                String header = AdminController.getPopups().get(identifier).getLeft();
                String text = "Old user: " + old.toString() + "\nNew user: " + this.service.getUser(old.getId());
                PopupAlert.showInformation(null, Alert.AlertType.CONFIRMATION, header, text);
            } catch (ServiceException sE) {
                // Showing a message of error.
                PopupEnum identifier = PopupEnum.UPDATE_USER_EXCEPTION;
                String header = AdminController.getPopups().get(identifier).getLeft();
                String text = sE.getMessage();
                PopupAlert.showInformation(null, Alert.AlertType.ERROR, header, text);
            }
        });
    }

    @Override
    public void update(SocialNetworkEvent event) {
        if (this.userTableView.isVisible()) {
            if (event.getClass().equals(UserEvent.class)) {
                // Casting to the specific type of event.
                UserEvent userEvent = (UserEvent) event;
                // Running an action based on the type of event occurred.
                switch (userEvent.getEventType()) {
                    case ADD_USER -> this.addUserRow(userEvent.getNewUser());
                    case UPDATE_USER -> this.updateUserRow(userEvent.getOldUser(), userEvent.getNewUser());
                    case REMOVE_USER -> this.removeUserRow(userEvent.getOldUser());
                }
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
