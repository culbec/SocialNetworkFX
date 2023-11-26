package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import ro.ubbcluj.map.socialnetworkfx.SocialNetworkApplication;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendRequestDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendshipDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.UserDBRepository;
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.utility.PopupEnum;
import ro.ubbcluj.map.socialnetworkfx.utility.RandomUserGenerator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for the admin interface of the social network.
 */
public class AdminController {
    // Popup headers and texts.
    private static final Map<PopupEnum, Tuple<String, String>> POPUPS = new HashMap<>();
    // FXML elements.
    @FXML
    private BorderPane mainLayout;
    @FXML
    private TableView<User> friendsTableView;
    @FXML
    private TableColumn<User, String> userFriend;
    @FXML
    private TableColumn<LocalDateTime, LocalDateTime> friendshipDate;
    // Service dependency.
    private Service service;

    // FXMLLoader Map that stores different loaders for different layouts.
    @FXML
    private final Map<String, FXMLLoader> fxmlLoaders = new TreeMap<>();

    /**
     * Adds headers and texts for the popup alerts.
     */
    private static void addPopups() {
        AdminController.POPUPS.put(PopupEnum.EMPTY_TABLE_EXCEPTION, new Tuple<>("Empty Table", "The table is empty!"));
        AdminController.POPUPS.put(PopupEnum.EMPTY_LIST_EXCEPTION, new Tuple<>("Empty List", "The list is empty!"));
        AdminController.POPUPS.put(PopupEnum.NONE_SELECTED_EXCEPTION, new Tuple<>("Empty Selection", "Nothing was selected!"));
        AdminController.POPUPS.put(PopupEnum.REMOVE_USER_EXCEPTION, new Tuple<>("Remove User Exception", null));
        AdminController.POPUPS.put(PopupEnum.REMOVE_USER_SUCCESS, new Tuple<>("Removed User(s) Successfully!\nRemoved users are listed below", ""));
        AdminController.POPUPS.put(PopupEnum.ADD_USER_EXCEPTION, new Tuple<>("Add User Exception", null));
        AdminController.POPUPS.put(PopupEnum.ADD_USER_SUCCESS, new Tuple<>("Added User Successfully!", null));
        AdminController.POPUPS.put(PopupEnum.UPDATE_USER_EXCEPTION, new Tuple<>("Update User Exception", null));
        AdminController.POPUPS.put(PopupEnum.UPDATE_USER_SUCCESS, new Tuple<>("Update User Successfully!", null));
        AdminController.POPUPS.put(PopupEnum.ADD_FRIENDSHIP_EXCEPTION, new Tuple<>("Add Friendship Exception", null));
        AdminController.POPUPS.put(PopupEnum.ADD_FRIENDSHIP_SUCCESS, new Tuple<>("Added Friendship Successfully!", null));
        AdminController.POPUPS.put(PopupEnum.REMOVE_FRIENDSHIP_EXCEPTION, new Tuple<>("Remove Friendship Exception", null));
        AdminController.POPUPS.put(PopupEnum.REMOVE_FRIENDSHIP_SUCCESS, new Tuple<>("Removed Friendship(s) Successfully!", null));
        AdminController.POPUPS.put(PopupEnum.FRIENDSHIP_REQUEST_EXCEPTION, new Tuple<>("Friendship Request Exception", null));
        AdminController.POPUPS.put(PopupEnum.FRIENDSHIP_REQUEST_SUCCESS, new Tuple<>("Friendship Request sent successfully!", null));
    }

    public static Map<PopupEnum, Tuple<String, String>> getPopups() {
        return POPUPS;
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
    public static void enableCellCopy(TableView<?> tableView) {
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

    /**
     * Adds different panes to the controller that will be used to
     * represent different actions based on the action wanted.
     */
    private void addLoaders() throws IOException {
        this.fxmlLoaders.put("users", new FXMLLoader(SocialNetworkApplication.class.getResource("views/user-view.fxml")));
        this.fxmlLoaders.put("friendships", new FXMLLoader(SocialNetworkApplication.class.getResource("views/friendship-view.fxml")));

        // Loading the root for each loader and setting its service.
        FXMLLoader userLoader = this.fxmlLoaders.get("users");
        userLoader.load();
        ((UserController) userLoader.getController()).setService(this.service);

        FXMLLoader friendshipLoader = this.fxmlLoaders.get("friendships");
        friendshipLoader.load();
        ((FriendshipController) friendshipLoader.getController()).initController(this.service);
    }

    /**
     * Adds the observers to the service.
     */
    private void addObservers() {
        UserController userController = this.fxmlLoaders.get("users").getController();
        FriendshipController friendshipController = this.fxmlLoaders.get("friendships").getController();

        this.service.addObserver(userController);
        this.service.addObserver(friendshipController);
    }

    public AdminController() throws IOException {
        // Specifying the database.
        String DB_URL = "jdbc:postgresql://localhost:5432/socialnetwork";
        String USERNAME = "postgres";
        String PASSWORD = "postgres";

        // Initializing the database repositories.
        UserDBRepository userDBRepository = new UserDBRepository(DB_URL, USERNAME, PASSWORD);
        FriendshipDBRepository friendshipDBRepository = new FriendshipDBRepository(DB_URL, USERNAME, PASSWORD);
        FriendRequestDBRepository friendRequestDBRepository = new FriendRequestDBRepository(DB_URL, USERNAME, PASSWORD);

        // Initializing the service.
        this.service = new Service(userDBRepository, friendshipDBRepository, friendRequestDBRepository);

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

            // Adding 20 hardcoded friendships
            this.service.addFriendship(this.service.getUsers().get(0).getId(), this.service.getUsers().get(1).getId());
            this.service.addFriendship(this.service.getUsers().get(0).getId(), this.service.getUsers().get(2).getId());
            this.service.addFriendship(this.service.getUsers().get(0).getId(), this.service.getUsers().get(3).getId());
            this.service.addFriendship(this.service.getUsers().get(0).getId(), this.service.getUsers().get(4).getId());
            this.service.addFriendship(this.service.getUsers().get(0).getId(), this.service.getUsers().get(5).getId());
            this.service.addFriendship(this.service.getUsers().get(1).getId(), this.service.getUsers().get(5).getId());
            this.service.addFriendship(this.service.getUsers().get(1).getId(), this.service.getUsers().get(6).getId());
            this.service.addFriendship(this.service.getUsers().get(1).getId(), this.service.getUsers().get(7).getId());
            this.service.addFriendship(this.service.getUsers().get(3).getId(), this.service.getUsers().get(2).getId());
            this.service.addFriendship(this.service.getUsers().get(4).getId(), this.service.getUsers().get(3).getId());
        }

        // Adding different loaders to the main app.
        this.addLoaders();

        // Adding the observers.
        this.addObservers();

        // Adding popup headers and texts.
        addPopups();
    }

    /**
     * Changes the layout to the user layout.
     */
    public void userLayoutChange() {
        // Retrieving the root of the loader.
        Parent userRoot = this.fxmlLoaders.get("users").getRoot();

        // Setting the new root of the center section.
        this.mainLayout.setCenter(userRoot);
    }

    public void friendshipLayoutChange() {
        // Retrieving the root of the loader.
        Parent friendshipRoot = this.fxmlLoaders.get("friendships").getRoot();

        // Repopulating the friend list on layout change.
        FriendshipController friendshipController = this.fxmlLoaders.get("friendships").getController();
        friendshipController.userComboAction();

        // Setting the new root of the center section.
        this.mainLayout.setCenter(friendshipRoot);
    }

    public void messageLayoutChange() {
    }

    public void otherLayoutChange() {
    }
}