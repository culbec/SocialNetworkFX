package ro.ubbcluj.map.socialnetworkfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ro.ubbcluj.map.socialnetworkfx.controllers.LoginController;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendRequestDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendshipDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.MessageDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.UserDBRepository;
import ro.ubbcluj.map.socialnetworkfx.service.Service;

import java.io.IOException;
import java.util.Objects;

public class SocialNetworkApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    // Initializes the service used by the app and its views.
    private Service initService() {
        // Specifying the database.
        String DB_URL = "jdbc:postgresql://localhost:5432/socialnetwork";
        String USERNAME = "postgres";
        String PASSWORD = "postgres";

        // Initializing the database repositories.
        UserDBRepository userDBRepository = new UserDBRepository(DB_URL, USERNAME, PASSWORD);
        FriendshipDBRepository friendshipDBRepository = new FriendshipDBRepository(DB_URL, USERNAME, PASSWORD);
        FriendRequestDBRepository friendRequestDBRepository = new FriendRequestDBRepository(DB_URL, USERNAME, PASSWORD);
        MessageDBRepository messageDBRepository = new MessageDBRepository(DB_URL, USERNAME, PASSWORD);

        // Returning the service.
        return new Service(userDBRepository, friendshipDBRepository, friendRequestDBRepository, messageDBRepository);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Service service = initService();
        FXMLLoader fxmlLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/login-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/mascot.jpg"))));
        stage.setTitle("Social Network");
        stage.setScene(scene);

        LoginController loginController = fxmlLoader.getController();
        loginController.initController(service);

        stage.show();
    }
}