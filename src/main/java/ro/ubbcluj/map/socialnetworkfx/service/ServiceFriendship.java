package ro.ubbcluj.map.socialnetworkfx.service;

import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.EventType;
import ro.ubbcluj.map.socialnetworkfx.events.FriendshipEvent;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.exception.ValidatorException;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendshipDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.Repository;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observable;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;
import ro.ubbcluj.map.socialnetworkfx.validator.FriendshipValidator;

import java.util.*;
import java.util.stream.StreamSupport;

public class ServiceFriendship implements IService, Observer<SocialNetworkEvent> {
    private final Repository<Tuple<UUID, UUID>, Friendship> friendshipRepository;
    private final Set<Observer<SocialNetworkEvent>> observers = new HashSet<>();

    public ServiceFriendship(Repository<Tuple<UUID, UUID>, Friendship> friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    /**
     * Returns an iterable of user IDs which are friends with the user with the specified id.
     *
     * @param userId ID of user.
     * @return Iterable with the IDs of the friends of user with id = {@code userId}.
     */
    public List<UUID> getFriendIdsOf(UUID userId) throws RepositoryException {
        List<UUID> friendIds = new ArrayList<>();

        // Retrieving all friend ids of the user.
        this.friendshipRepository.getAll().forEach(friendship -> {
            // Deciding which one of the users is our user.
            if (friendship.getId().getLeft().equals(userId)) {
                friendIds.add(friendship.getId().getRight());
            } else if (friendship.getId().getRight().equals(userId)) {
                friendIds.add(friendship.getId().getLeft());
            }
        });

        return friendIds;
    }

    /**
     * @return The list of friendships contained by the Repository.
     */

    public ArrayList<Friendship> getFriendships() {
        ArrayList<Friendship> friendshipList = new ArrayList<>();

        // Populating the friendship list.
        this.friendshipRepository.getAll().forEach(friendshipList::add);
        return friendshipList;
    }

    /**
     * Adds a friendship between two users.
     *
     * @param id1 ID of the first user in the friendship.
     * @param id2 ID of the second user in the friendship.
     * @throws ServiceException If the friendship already exists
     */


    public void addFriendship(UUID id1, UUID id2) throws ServiceException {
        try {
            // Initializing a friendship.
            Friendship friendship = new Friendship(id1, id2);

            // Validating the friendship so that it reaches the repository in a valid state.
            new FriendshipValidator().validate(friendship);

            // Verifying if the friendship already exists.
            if (this.friendshipRepository.save(friendship).isPresent()) {
                throw new ServiceException("A friendship with the same ID already exists!");
            }

            // Sending an event for the observers.
            this.notify(new FriendshipEvent(EventType.ADD_FRIENDSHIP, null, friendship));
        } catch (ValidatorException | RepositoryException exception) {
            throw new ServiceException("Couldn't add friendship.", exception);
        }
    }

    /**
     * Removes the friendship between two users.
     *
     * @param id1 ID of the first user.
     * @param id2 ID of the second user.
     * @return Friendship that was removed.
     * @throws ServiceException If the friendship couldn't be found.
     */

    public Friendship removeFriendship(UUID id1, UUID id2) throws ServiceException {
        // Retrieving the friendship between the two users.
        Optional<Friendship> friendship = this.friendshipRepository.getOne(new Tuple<>(id1, id2));

        // Verifying if the friendship is present, but reversed.
        if (friendship.isEmpty()) {
            friendship = this.friendshipRepository.getOne(new Tuple<>(id2, id1));
            if (friendship.isEmpty()) {
                throw new ServiceException("No friendship found.");

                // Deleting the friendship if present.
            } else {
                this.friendshipRepository.delete(friendship.get().getId());
            }
        } else {
            this.friendshipRepository.delete(friendship.get().getId());
        }

        // Notifying the observers.
        this.notify(new FriendshipEvent(EventType.REMOVE_FRIENDSHIP, friendship.get(), null));

        // Returning the deleted friendship.
        return friendship.get();
    }

    /**
     * Removes the friendships of a user specified by its ID.
     *
     * @param userId ID of the user to remove the friendships of.
     */
    public void removeFriendshipsOf(UUID userId) {
        // Deleting all the friendships that contained the user.
        // This implementation was relevant for a type of Repository which
        // wouldn't perform deleting on cascade.
        if (!this.friendshipRepository.getClass().equals(FriendshipDBRepository.class)) {
            List<Friendship> friendships = StreamSupport.stream(this.friendshipRepository.getAll().spliterator(), false)
                    .filter(friendship -> friendship.getId().getLeft().equals(userId) || friendship.getId().getRight().equals(userId)).toList();
            friendships.forEach(friendship -> this.removeFriendship(friendship.getId().getLeft(), friendship.getId().getRight()));
        }
    }

    public List<User> getFriendsFromPage(int page, int noOfUsers, User user) throws ServiceException {
        ((FriendshipDBRepository) this.friendshipRepository).setCurrentPage(page);
        ((FriendshipDBRepository) this.friendshipRepository).setNoItemsPerPage(noOfUsers);
        return ((FriendshipDBRepository) this.friendshipRepository).getFriendsFromPage(page, noOfUsers, user.getId());
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

    @Override
    public void update(SocialNetworkEvent event) {
        if (event.getClass().equals(FriendshipEvent.class)) {
            FriendshipEvent friendshipEvent = (FriendshipEvent) event;
            if (friendshipEvent.getEventType().equals(EventType.ACCEPT_FRIEND_REQUEST)) {
                this.addFriendship(friendshipEvent.getNewFriendship().getId().getLeft(), friendshipEvent.getNewFriendship().getId().getRight());
            }
        }
    }
}
