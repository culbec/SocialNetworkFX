package ro.ubbcluj.map.socialnetworkfx.events;

import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;

import java.util.UUID;

public class FriendshipEvent extends SocialNetworkEvent {
    private Tuple<UUID, UUID> oldFriendship;
    private Tuple<UUID, UUID> newFriendship;

    public FriendshipEvent(EventType eventType, Tuple<UUID, UUID> oldFriendship, Tuple<UUID, UUID> newFriendship) {
        super(eventType);
        this.oldFriendship = oldFriendship;
        this.newFriendship = newFriendship;
    }

    public Tuple<UUID, UUID> getOldFriendship() {
        return oldFriendship;
    }

    public Tuple<UUID, UUID> getNewFriendship() {
        return newFriendship;
    }
}
