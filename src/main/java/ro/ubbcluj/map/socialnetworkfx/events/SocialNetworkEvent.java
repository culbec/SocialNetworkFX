package ro.ubbcluj.map.socialnetworkfx.events;

public abstract class SocialNetworkEvent implements Event {
    // Event type.
    private final EventType eventType;

    public SocialNetworkEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }
}
