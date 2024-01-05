package ro.ubbcluj.map.socialnetworkfx.controllers.UserInterface;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import ro.ubbcluj.map.socialnetworkfx.SocialNetworkApplication;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.service.IService;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class UserInterfaceController {
    // Parent Map that stores different loaders for different layouts.
    private final Map<String, Parent> layouts = new TreeMap<>();
    // User on that the user interface is built on.
    protected User user;
    // FXML elements.
    @FXML
    private AnchorPane userInterfaceLayout;
    @FXML
    private BorderPane activeLayout;
    // Map of IService objects that will serve the app.
    private Map<String, IService> serviceMap;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void initController(Map<String, IService> serviceMap) {
        this.serviceMap = serviceMap;

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
     *
     * @param layout     The layout to set the close request on.
     * @param controller The controller that will be removed from the observer list.
     */
    private void setCloseRequests(Parent layout, Observer<SocialNetworkEvent> controller) {
        layout.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.serviceMap.forEach((string, service) -> {
                    // Layout is now visible, set the OnCloseRequest event
                    this.userInterfaceLayout.getScene().getWindow().setOnCloseRequest(event ->
                            service.removeObserver(controller));
                });
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
        // Initializing the service for all the controllers used.

        // Profile layout.
        FXMLLoader profileLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/profile-layout.fxml"));
        Parent profileLayout = profileLoader.load();
        ProfileController profileController = profileLoader.getController();
        profileController.setUser(this.user);
        profileController.initController(this.serviceMap.get("userService"));

        // User area layout.
        FXMLLoader userAreaLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/user-area.fxml"));
        Parent userAreaLayout = userAreaLoader.load();
        UserAreaController userAreaController = userAreaLoader.getController();
        userAreaController.setUser(this.user);
        userAreaController.initController(this.serviceMap.get("userService"), this.serviceMap.get("friendrequestService"), this.serviceMap.get("messageService"));

        // Friend area layout.
        FXMLLoader friendAreaLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/friend-area.fxml"));
        Parent friendAreaLayout = friendAreaLoader.load();
        FriendAreaController friendAreaController = friendAreaLoader.getController();
        friendAreaController.setUser(this.user);
        friendAreaController.initController(this.serviceMap.get("userService"), this.serviceMap.get("friendshipService"), this.serviceMap.get("friendrequestService"));

        // Message area layout.
        FXMLLoader messageAreaLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/user/message-area.fxml"));
        Parent messageAreaLayout = messageAreaLoader.load();
        MessageAreaController messageAreaController = messageAreaLoader.getController();
        messageAreaController.setUser(this.user);
        messageAreaController.initController(this.serviceMap.get("userService"), this.serviceMap.get("friendshipService"), this.serviceMap.get("messageService"));

        // Adding the layouts in the map.
        this.layouts.put("profile", profileLayout);
        this.layouts.put("user-area", userAreaLayout);
        this.layouts.put("friend-area", friendAreaLayout);
        this.layouts.put("message-area", messageAreaLayout);

        // Setting the profile layout as the starting layout.
        this.profileButtonAction();

        // Adding the observer controllers as observers.
        this.serviceMap.get("userService").addObserver(userAreaController);
        this.serviceMap.get("friendrequestService").addObserver(userAreaController);
        this.serviceMap.get("messageService").addObserver(userAreaController);

        this.serviceMap.get("userService").addObserver(friendAreaController);
        this.serviceMap.get("friendshipService").addObserver(friendAreaController);
        this.serviceMap.get("friendrequestService").addObserver(friendAreaController);

        this.serviceMap.get("userService").addObserver(messageAreaController);
        this.serviceMap.get("friendshipService").addObserver(messageAreaController);
        this.serviceMap.get("messageService").addObserver(messageAreaController);

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
