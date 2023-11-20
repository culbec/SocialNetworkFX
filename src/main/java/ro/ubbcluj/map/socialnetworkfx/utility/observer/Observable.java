package ro.ubbcluj.map.socialnetworkfx.utility.observer;

import ro.ubbcluj.map.socialnetworkfx.events.Event;

public interface Observable<E extends Event> {
    /**
     * Adds an observer to the observer list.
     *
     * @param observer Observer to be added.
     */
    void addObserver(Observer<E> observer);

    /**
     * Removes an observer from the observer list.
     *
     * @param observer Observer to be removed.
     */
    void removeObserver(Observer<E> observer);

    /**
     * Notifies all the observers.
     *
     * @param event Event that should affect the observers.
     */
    void notify(E event);
}
