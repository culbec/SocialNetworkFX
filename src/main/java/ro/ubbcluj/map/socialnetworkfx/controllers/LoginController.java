package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.utility.RandomUserGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class LoginController extends Controller {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox showPasswordCheckbox;

    @Override
    public void initController(Service service) {
        // Setting the service.
        super.initController(service);

        // Trying to add new users.
        AdminController.generateRandomUsers(this.service);

        // Setting the behavior of the checkbox.
        this.showPasswordCheckbox.setOnAction(event -> Controller.showPasswordAction(showPasswordCheckbox, passwordField));
    }

    // Logins a user in the network and opens the user interface for that user.
    public void loginAction() {
        try {
            // Retrieving the user from the repository.
            User user = this.service.tryLoginUser(this.emailField.getText(), this.passwordField.getText());

            // Opening a user interface with the above user as data.
            FXMLLoader userInterfaceLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/user-interface.fxml"));
            AnchorPane userInterfacePane = userInterfaceLoader.load();

            UserInterfaceController userInterfaceController = userInterfaceLoader.getController();
            userInterfaceController.setUser(user);
            userInterfaceController.initController(this.service);

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
        Scene signupScene = signupLoader.load();
        // Loading the stage.
        Stage signupStage = new Stage();
        signupStage.setScene(signupScene);
        // Initializing the signup controller.
        SignupController signupController = signupLoader.getController();
        signupController.initController(this.service);

        // Showing and waiting for execution.
        signupStage.showAndWait();
    }

}
