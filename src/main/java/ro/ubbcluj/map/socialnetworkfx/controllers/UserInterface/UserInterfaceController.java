package ro.ubbcluj.map.socialnetworkfx.controllers.UserInterface;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import ro.ubbcluj.map.socialnetworkfx.SocialNetworkApplication;
import ro.ubbcluj.map.socialnetworkfx.controllers.Controller;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.service.Service;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class UserInterfaceController extends Controller {
    // Parent Map that stores different loaders for different layouts.
    private final Map<String, Parent> layouts = new TreeMap<>();
    // FXML elements.
    @FXML
    private AnchorPane userInterfaceLayout;
    @FXML
    private BorderPane activeLayout;

    // User on that the user interface is built on.
    protected User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void initController(Service service) {
        super.initController(service);

        // Adding the layouts specific for the user interface.
        try {
            this.addLayouts();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the close requests for all the layouts present.
     * If the layout is visible, then the close request will be set.
     * @param layout The layout to set the close request on.
     * @param controller The controller that will be removed from the observer list.
     */
    private void setCloseRequests(Parent layout, Observer<SocialNetworkEvent> controller) {
        layout.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // userAreaLayout is now visible, set the OnCloseRequest event
                this.userInterfaceLayout.getScene().getWindow().setOnCloseRequest(event ->
                        this.service.removeObserver(controller));
            }
        });
    }

    /**
     * Adds the specific layouts for the user interface.
     * <p>
     * This method is responsible for initializing the controllers of all the layouts and adding
     * the loaders in a map for a responsive design.
     * <p>
     * Also, almost all layouts will be observers to the service, so they will be added accordingly.
     */
    private void addLayouts() throws IOException {
        // Profile layout.
        FXMLLoader profileLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/profile-layout.fxml"));
        Parent profileLayout = profileLoader.load();
        ProfileController profileController = profileLoader.getController();
        profileController.setUser(this.user);
        profileController.initController(this.service);

        // User area layout.
        FXMLLoader userAreaLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/user-area.fxml"));
        Parent userAreaLayout = userAreaLoader.load();
        UserAreaController userAreaController = userAreaLoader.getController();
        userAreaController.setUser(this.user);
        userAreaController.initController(this.service);

        // Friend area layout.
        FXMLLoader friendAreaLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/friend-area.fxml"));
        Parent friendAreaLayout = friendAreaLoader.load();
        FriendAreaController friendAreaController = friendAreaLoader.getController();
        friendAreaController.setUser(this.user);
        friendAreaController.initController(this.service);

        // Message area layout.
        FXMLLoader messageAreaLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/message-area.fxml"));
        Parent messageAreaLayout = messageAreaLoader.load();
        MessageAreaController messageAreaController = messageAreaLoader.getController();
        messageAreaController.setUser(this.user);
        messageAreaController.initController(this.service);

        // Adding the layouts in the map.
        this.layouts.put("profile", profileLayout);
        this.layouts.put("user-area", userAreaLayout);
        this.layouts.put("friend-area", friendAreaLayout);
        this.layouts.put("message-area", messageAreaLayout);

        // Setting the profile layout as the starting layout.
        this.profileButtonAction();

        // Adding the observer controllers as observers.
        this.service.addObserver(userAreaController);
        this.service.addObserver(friendAreaController);
        this.service.addObserver(messageAreaController);

        // Adding on close requests based on the state of visibility of the layouts.
        this.setCloseRequests(userAreaLayout, userAreaController);
        this.setCloseRequests(friendAreaLayout, friendAreaController);
        this.setCloseRequests(messageAreaLayout, messageAreaController);
    }

    public void profileButtonAction() {
        Parent profileLayout = this.layouts.get("profile");
        this.activeLayout.setCenter(profileLayout);
    }

    public void userButtonAction() {
        Parent userAreaLayout = this.layouts.get("user-area");
        this.activeLayout.setCenter(userAreaLayout);
    }

    public void friendButtonAction() {
        Parent friendAreaLayout = this.layouts.get("friend-area");
        this.activeLayout.setCenter(friendAreaLayout);
    }

    public void messageButtonAction() {
        Parent messageAreaLayout = this.layouts.get("message-area");
        this.activeLayout.setCenter(messageAreaLayout);
    }
}
