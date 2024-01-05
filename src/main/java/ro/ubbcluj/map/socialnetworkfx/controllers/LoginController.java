package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkfx.SocialNetworkApplication;
import ro.ubbcluj.map.socialnetworkfx.controllers.AdminInterface.AdminController;
import ro.ubbcluj.map.socialnetworkfx.controllers.UserInterface.UserInterfaceController;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendRequestDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendshipDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.MessageDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.UserDBRepository;
import ro.ubbcluj.map.socialnetworkfx.service.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox showPasswordCheckbox;
    // Map of IService objects that will serve the app.
    private final Map<String, IService> serviceMap = new TreeMap<>();

    public void initController() {
        // Specifying the database.
        String DB_URL = "jdbc:postgresql://localhost:5432/socialnetwork";
        String USERNAME = "postgres";
        String PASSWORD = "postgres";

        // Initializing the database repositories.
        UserDBRepository userDBRepository = new UserDBRepository(DB_URL, USERNAME, PASSWORD);
        FriendshipDBRepository friendshipDBRepository = new FriendshipDBRepository(DB_URL, USERNAME, PASSWORD);
        FriendRequestDBRepository friendRequestDBRepository = new FriendRequestDBRepository(DB_URL, USERNAME, PASSWORD);
        MessageDBRepository messageDBRepository = new MessageDBRepository(DB_URL, USERNAME, PASSWORD);

        // Initializing each service.
        ServiceUser serviceUser = new ServiceUser(userDBRepository);
        ServiceFriendship serviceFriendship = new ServiceFriendship(friendshipDBRepository);
        ServiceFriendRequest serviceFriendRequest = new ServiceFriendRequest(friendRequestDBRepository);
        ServiceMessage serviceMessage = new ServiceMessage(messageDBRepository);

        // Adding the service objects to the map.
        this.serviceMap.put("userService", serviceUser);
        this.serviceMap.put("friendshipService", serviceFriendship);
        this.serviceMap.put("friendrequestService", serviceFriendRequest);
        this.serviceMap.put("messageService", serviceMessage);

        // Trying to add new users.
        // AdminController.generateRandomUsers(serviceUser);

        // Setting the behavior of the checkbox.
        this.showPasswordCheckbox.setOnAction(event -> Controller.showPasswordAction(showPasswordCheckbox, passwordField));
    }

    // Logins a user in the network and opens the user interface for that user.
    public void loginAction() {
        try {
            String plainPassword = (this.passwordField.getText().isEmpty()) ? this.passwordField.getPromptText() : this.passwordField.getText();

            // Retrieving the user from the repository.
            User user = ((ServiceUser)this.serviceMap.get("userService")).tryLoginUser(this.emailField.getText().trim(), plainPassword.trim());

            // Opening a user interface with the above user as data.
            FXMLLoader userInterfaceLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/user-interface.fxml"));
            AnchorPane userInterfacePane = userInterfaceLoader.load();

            UserInterfaceController userInterfaceController = userInterfaceLoader.getController();
            userInterfaceController.setUser(user);
            userInterfaceController.initController(this.serviceMap);

            Stage userInterfaceStage = new Stage();
            userInterfaceStage.setScene(new Scene(userInterfacePane));

            userInterfaceStage.show();

        } catch (ServiceException sE) {
            PopupAlert.showInformation(null, Alert.AlertType.ERROR, "Cannot login!", sE.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Opens a new window for user registration.
    public void signupAction() throws IOException {
        // Retrieving the loader of the signup window.
        FXMLLoader signupLoader = new FXMLLoader(Objects.requireNonNull(SocialNetworkApplication.class.getResource("views/signup-view.fxml")));
        // Loading the scene.
        Scene signupScene = new Scene(signupLoader.load());
        // Loading the stage.
        Stage signupStage = new Stage();
        signupStage.setScene(signupScene);
        // Initializing the signup controller.
        SignupController signupController = signupLoader.getController();
        signupController.initController(this.serviceMap.get("userService"));

        // Showing and waiting for execution.
        signupStage.showAndWait();
    }

}
