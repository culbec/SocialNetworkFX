package ro.ubbcluj.map.socialnetworkfx.events;

import ro.ubbcluj.map.socialnetworkfx.entity.FriendRequest;

public class FriendRequestEvent extends SocialNetworkEvent {
    private final FriendRequest oldFriendRequest;
    private final FriendRequest newFriendRequest;
    public FriendRequestEvent(EventType eventType, FriendRequest oldFriendRequest, FriendRequest newFriendRequest) {
        super(eventType);
        this.oldFriendRequest = oldFriendRequest;
        this.newFriendRequest = newFriendRequest;
    }

    public FriendRequest getOldFriendRequest() {
        return this.oldFriendRequest;
    }

    public FriendRequest getNewFriendRequest() {
        return this.newFriendRequest;
    }
}
