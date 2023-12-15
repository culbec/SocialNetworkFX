package ro.ubbcluj.map.socialnetworkfx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import ro.ubbcluj.map.socialnetworkfx.SocialNetworkApplication;
import ro.ubbcluj.map.socialnetworkfx.service.Service;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class MessageController {
    // Specific layouts for the message.
    private final Map<String, Parent> layouts = new TreeMap<>();
    @FXML
    public BorderPane messageLayout;
    // Service dependency.
    private Service service;

    public void setService(Service service) {
        this.service = service;
    }

    public void initController(Service service) throws IOException {
        this.setService(service);

        // Adding the layouts.
        this.addLayouts();
    }

    private void addLayouts() throws IOException {
        // Adding the view message layout.
        FXMLLoader viewMessageLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/view-messages-view.fxml"));
        Parent viewMessageRoot = viewMessageLoader.load();
        ViewMessageController viewMessageController = viewMessageLoader.getController();
        viewMessageController.initController(this.service);

        // Adding the send message layout.
        FXMLLoader sendMessageLoader = new FXMLLoader(SocialNetworkApplication.class.getResource("views/send-message-view.fxml"));
        Parent sendMessageRoot = sendMessageLoader.load();
        SendMessageController sendMessageController = sendMessageLoader.getController();
        sendMessageController.initController(this.service);

        // Adding the layouts.
        this.layouts.put("viewMessages", viewMessageRoot);
        this.layouts.put("sendMessage", sendMessageRoot);

        // Adding the observers.
        this.service.addObserver(viewMessageController);
        this.service.addObserver(sendMessageController);
    }

    public void viewMessagesLayout() {
        // Loading the layout.
        Parent viewMessageRoot = this.layouts.get("viewMessages");

        // Setting the view as the main layout for the message layout.
        this.messageLayout.setCenter(viewMessageRoot);
    }

    public void sendMessageLayout() {
        // Loading the layouts.
        Parent sendMessageRoot = this.layouts.get("sendMessage");

        // Setting the view as the main layout for the message layout.
        this.messageLayout.setCenter(sendMessageRoot);
    }
}
