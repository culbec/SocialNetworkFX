package ro.ubbcluj.map.socialnetworkfx.events;

import ro.ubbcluj.map.socialnetworkfx.entity.User;

public class UserEvent extends SocialNetworkEvent {
    // Useful data for the client based on the operation applied.
    private final User newUser;
    private final User oldUser;

    public UserEvent(EventType eventType, User newUser, User oldUser) {
        super(eventType);
        this.newUser = newUser;
        this.oldUser = oldUser;
    }

    /**
     * @return The new user based on the event.
     */
    public User getNewUser() {
        return newUser;
    }

    /**
     * @return The old user based on the event.
     */
    public User getOldUser() {
        return oldUser;
    }

}
