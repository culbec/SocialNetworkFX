package ro.ubbcluj.map.socialnetworkfx.service;

import ro.ubbcluj.map.socialnetworkfx.entity.Message;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.EventType;
import ro.ubbcluj.map.socialnetworkfx.events.MessageEvent;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.repository.InMemoryRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.MessageDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.Repository;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observable;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.util.*;
import java.util.stream.StreamSupport;

public class ServiceMessage implements IService {
    private final Repository<UUID, Message> messageRepository;
    private final Set<Observer<SocialNetworkEvent>> observers = new HashSet<>();

    public ServiceMessage(Repository<UUID, Message> messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Returns a list of messages between given users sorted by the date of sending.
     *
     * @param sender   User that sent messages.
     * @param receiver User that received messages.
     * @return A list of messages between given users.
     */
    public List<Message> getMessagesBetweenUsers(User sender, User receiver) {
        if (messageRepository.getClass().equals(InMemoryRepository.class)) {
            return StreamSupport.stream(this.messageRepository.getAll().spliterator(), false)
                    .filter(message -> message.getFrom().equals(sender.getId()) && message.getTo().contains(receiver.getId()))
                    .toList();
        }

        List<Message> messages = ((MessageDBRepository) this.messageRepository).getMessagesBetweenUsers(sender.getId(), receiver.getId());
        messages.sort(Comparator.comparing(Message::getDate));
        return messages;
    }

    /**
     * Sends a message from a user to other users.
     *
     * @param message Message to be sent.
     * @throws ServiceException If something went wrong with sending the message.
     */
    public void sendMessage(Message message) throws ServiceException {
        // Attempting to save the message.
        try {
            this.messageRepository.save(message);
            // Notifying the observers.
            this.notify(new MessageEvent(EventType.ADD_MESSAGE, message));
        } catch (RepositoryException repositoryException) {
            throw new ServiceException(repositoryException.getMessage(), repositoryException.getCause());
        }
    }

    @Override
    public void addObserver(Observer<SocialNetworkEvent> observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<SocialNetworkEvent> observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notify(SocialNetworkEvent event) {
        this.observers.forEach(observer -> observer.update(event));
    }
}
