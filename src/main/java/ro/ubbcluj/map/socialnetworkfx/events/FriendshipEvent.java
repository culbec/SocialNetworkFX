package ro.ubbcluj.map.socialnetworkfx.events;

import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;

public class FriendshipEvent extends SocialNetworkEvent {
    private final Friendship oldFriendship;
    private final Friendship newFriendship;

    public FriendshipEvent(EventType eventType, Friendship oldFriendship, Friendship newFriendship) {
        super(eventType);
        this.oldFriendship = oldFriendship;
        this.newFriendship = newFriendship;
    }

    public Friendship getOldFriendship() {
        return oldFriendship;
    }

    public Friendship getNewFriendship() {
        return newFriendship;
    }
}
