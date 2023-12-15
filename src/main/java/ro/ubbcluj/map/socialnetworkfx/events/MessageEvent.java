package ro.ubbcluj.map.socialnetworkfx.events;

import ro.ubbcluj.map.socialnetworkfx.entity.Message;

public class MessageEvent extends SocialNetworkEvent {
    // Message that was sent.
    private final Message message;

    public MessageEvent(EventType eventType, Message message) {
        super(eventType);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
