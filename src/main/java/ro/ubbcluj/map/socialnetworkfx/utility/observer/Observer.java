package ro.ubbcluj.map.socialnetworkfx.utility.observer;

import ro.ubbcluj.map.socialnetworkfx.events.Event;

public interface Observer<E extends Event> {
    /**
     * Updates the observer based on the passed event.
     *
     * @param event Event that will affect the observer.
     */
    void update(E event);
}
