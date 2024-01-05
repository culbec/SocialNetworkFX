package ro.ubbcluj.map.socialnetworkfx.service;

import ro.ubbcluj.map.socialnetworkfx.entity.FriendRequest;
import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.EventType;
import ro.ubbcluj.map.socialnetworkfx.events.FriendRequestEvent;
import ro.ubbcluj.map.socialnetworkfx.events.FriendshipEvent;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendRequestDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.InMemoryRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.Repository;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

public class ServiceFriendRequest implements IService {
    private final Repository<Tuple<Tuple<UUID, UUID>, LocalDateTime>, FriendRequest> friendRequestRepository;
    private final Set<Observer<SocialNetworkEvent>> observers = new HashSet<>();

    public ServiceFriendRequest(Repository<Tuple<Tuple<UUID, UUID>, LocalDateTime>, FriendRequest> friendRequestRepository) {
        this.friendRequestRepository = friendRequestRepository;
    }

    /**
     * @param userId User ID of the user that received friend requests.
     * @return A list with all friend requests.
     */

    public List<FriendRequest> getFriendRequestsOfUser(UUID userId) {
        if (this.friendRequestRepository.getClass().equals(InMemoryRepository.class)) {
            return StreamSupport.stream(this.friendRequestRepository.getAll().spliterator(), false)
                    .filter(friendRequest -> friendRequest.getIdTo().equals(userId) && friendRequest.getStatus().equals("pending"))
                    .toList();
        }

        return ((FriendRequestDBRepository) this.friendRequestRepository).getPendingRequestsTo(userId);
    }

    /**
     * Sends a friend request from a user to another.
     *
     * @param user1 User that sends the friend request.
     * @param user2 User that receives the friend request.
     */
    public void sendFriendRequest(User user1, User user2) {
        // Excluding cases when the values are not parsed -> null.
        if (user1 == null || user2 == null) {
            throw new ServiceException("Null parameter!");
        }
        // Excluding cases when the user tries to send a friend request to itself.
        if (user1.equals(user2)) {
            throw new ServiceException("Cannot send friend request to the same user!");
        }
        /*// Excluding cases when the users are already friends.
        if (this.getFriendsOf(user2.getId()).contains(user1)) {
            throw new ServiceException("The users are already friends!");
        }*/

        // Verifying if there is already a pending friend request between the two users.
        if (this.friendRequestRepository.getClass().equals(InMemoryRepository.class)) {
            StreamSupport.stream(this.friendRequestRepository.getAll().spliterator(), false)
                    .filter(friendRequest -> friendRequest.getIdFrom().equals(user1.getId()) && friendRequest.getIdTo().equals(user2.getId()))
                    .forEach(friendRequest -> {
                        if (friendRequest.getStatus().equals("pending")) {
                            throw new ServiceException("There is already a pending friend request between the two users!");
                        }
                    });
        } else if (friendRequestRepository.getClass().equals(FriendRequestDBRepository.class)) {
            try {
                if (((FriendRequestDBRepository) this.friendRequestRepository).verifyPending(user1.getId(), user2.getId())) {
                    throw new ServiceException("There is already a pending friend request between the two users!");
                }
            } catch (RepositoryException rE) {
                throw new ServiceException(rE.getMessage());
            }
        }

        // If not, we can proceed on sending the friend request.
        FriendRequest friendRequest = new FriendRequest(user1.getId(), user2.getId());
        this.friendRequestRepository.save(friendRequest);
        this.notify(new FriendRequestEvent(EventType.ADD_FRIEND_REQUEST, null, friendRequest));
    }

    /**
     * Accepts a friend request between to users and make the two users friends.
     *
     * @param friendRequest Friend request between two users.
     */
    public void acceptFriendRequest(FriendRequest friendRequest) {
        // Verifying that the friend request is not null.
        if (friendRequest != null) {
            // Updating the friend request internally.
            FriendRequest newFriendRequest = new FriendRequest(friendRequest.getIdFrom(), friendRequest.getIdTo(), "accepted", friendRequest.getDate());
            this.friendRequestRepository.update(newFriendRequest);
            this.notify(new FriendRequestEvent(EventType.REMOVE_FRIEND_REQUEST, friendRequest, null));
        }
    }

    /**
     * Rejects a friend request between two users.
     *
     * @param friendRequest Friend request between two users.
     */
    public void rejectFriendRequest(FriendRequest friendRequest) {
        // Verifying that the friend request is not null.
        if (friendRequest != null) {
            // Updating the status of the friend request.
            FriendRequest newFriendRequest = new FriendRequest(friendRequest.getIdFrom(), friendRequest.getIdTo(), "rejected", friendRequest.getDate());
            this.friendRequestRepository.update(newFriendRequest);
            this.notify(new FriendRequestEvent(EventType.REMOVE_FRIEND_REQUEST, friendRequest, null));
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
