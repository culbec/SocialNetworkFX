package ro.ubbcluj.map.socialnetworkfx.service;

import ro.ubbcluj.map.socialnetworkfx.entity.*;
import ro.ubbcluj.map.socialnetworkfx.events.*;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.exception.ValidatorException;
import ro.ubbcluj.map.socialnetworkfx.repository.*;
import ro.ubbcluj.map.socialnetworkfx.utility.Graph;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observable;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;
import ro.ubbcluj.map.socialnetworkfx.validator.FriendshipValidator;
import ro.ubbcluj.map.socialnetworkfx.validator.UserValidator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Service implements Observable<SocialNetworkEvent> {
    // Repository that stores Users.
    private final Repository<UUID, User> userRepository;
    // Repository that stores Friendships.
    private final Repository<Tuple<UUID, UUID>, Friendship> friendshipRepository;
    // Repository that stores FriendRequests.
    private final Repository<Tuple<Tuple<UUID, UUID>, LocalDateTime>, FriendRequest> friendRequestRepository;
    // Repository that stores messages.
    private final Repository<UUID, Message> messageRepository;

    // Set of observers to the Service.
    private final Set<Observer<SocialNetworkEvent>> observers = new HashSet<>();

    public Service(Repository<UUID, User> userRepo,
                   Repository<Tuple<UUID, UUID>, Friendship> friendshipRepo,
                   Repository<Tuple<Tuple<UUID, UUID>, LocalDateTime>, FriendRequest> friendRequestRepository,
                   Repository<UUID, Message> messageRepository) {
        this.userRepository = userRepo;
        this.friendshipRepository = friendshipRepo;
        this.friendRequestRepository = friendRequestRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Adds a user to the list of users.
     *
     * @param firstName The First name of the user to be added.
     * @param lastName  The Last name of the user to be added.
     * @param email     Email of the user to be added.
     * @param password  Password of the user to be added.
     * @throws ServiceException If the user couldn't be added.
     */


    public void addUser(String firstName, String lastName, String email, String password) throws ServiceException {
        // User that will be saved in the Repository
        User user = new User(firstName, lastName, email, password);
        try {
            // Validating the user so that it reaches the Repository in a valid state.
            new UserValidator().validate(user);
        } catch (ValidatorException vE) {
            throw new ServiceException("User wasn't validated.", vE);
        }

        try {
            // Trying to save the user in the Repository.
            Optional<User> saved = this.userRepository.save(user);

            // Verifying if the user already exists in the Repository.
            if (saved.isPresent()) {
                throw new ServiceException("An user with the same ID already exists.");
            }
        } catch (RepositoryException rE) {
            throw new ServiceException("Couldn't add user.", rE);
        }

        // Notifying the observers with the event of adding that occurred.
        this.notify(new UserEvent(EventType.ADD_USER, user, null));
    }

    /**
     * Removes a user from the list of users.
     *
     * @param userId ID of the user to be removed
     * @return an {@code Optional} encapsulating the removed user.
     * @throws ServiceException If the user can't be removed.
     */

    public User removeUser(UUID userId) throws ServiceException {
        // Trying to delete the user from the Repository.
        Optional<User> deleted = this.userRepository.delete(userId);

        // Verifying if the user was deleted.
        if (deleted.isEmpty()) {
            throw new ServiceException("The user with the specified ID does not exist.");
        }

        // Deleting all the friendships that contained the user.
        // This implementation was relevant for a type of Repository which
        // wouldn't perform deleting on cascade.

        Iterable<User> friendsOf = this.getFriendsOf(userId);
        friendsOf.forEach(user -> {
            if (this.friendshipRepository.getOne(new Tuple<>(userId, user.getId())).isEmpty()) {
                this.friendshipRepository.delete(new Tuple<>(user.getId(), userId));
            } else {
                this.friendshipRepository.delete(new Tuple<>(userId, user.getId()));
            }
        });

        // Notifying the observers with the event of removing that occurred.
        this.notify(new UserEvent(EventType.REMOVE_USER, null, deleted.get()));

        // Returning the deleted user.
        return deleted.get();
    }

    /**
     * Update the user with the specified ID with user.
     *
     * @param user New user.
     * @return Old user.
     * @throws ServiceException if the user couldn't be updated.
     */

    public User updateUser(User user) throws ServiceException {
        try {
            // Validating the user so that it reaches the Repository in a valid state.
            new UserValidator().validate(user);
        } catch (ValidatorException vE) {
            throw new ServiceException(vE.getMessage());
        }

        try {
            // Retrieving the old user from the update action on the repository.
            Optional<User> old = this.userRepository.update(user);

            // Verifying if the user was actually updated.
            if (old.isEmpty()) {
                throw new ServiceException("The user to update does not exist!");
            }

            // Notifying the observers with the event of updating that occurred.
            this.notify(new UserEvent(EventType.UPDATE_USER, user, old.get()));

            // Returning the old user.
            return old.get();
        } catch (RepositoryException rE) {
            throw new ServiceException(rE.getMessage());
        }
    }

    /**
     * Gets a user based on its id.
     *
     * @param userId ID of the user to get.
     * @return an {@code Optional} encapsulating the user.
     * @throws ServiceException If the user couldn't be found.
     */

    public User getUser(UUID userId) throws ServiceException {
        try {
            // Retrieving the user found in the repository.
            Optional<User> userFound = this.userRepository.getOne(userId);

            // Verifying if a user was actually found.
            if (userFound.isEmpty()) {
                throw new ServiceException("No user was found.");
            }

            // Returning the found user.
            return userFound.get();
        } catch (IllegalArgumentException iAE) {
            throw new ServiceException("Couldn't find user.", iAE);
        }
    }

    /**
     * Returns the user list.
     *
     * @return User list.
     */

    public ArrayList<User> getUsers() {
        ArrayList<User> userList = new ArrayList<>();

        // Populating the user list.
        this.userRepository.getAll().forEach(userList::add);
        return userList;
    }

    /**
     * Returns an iterable of users which are friends with the user with the specified id.
     *
     * @param userId ID of user.
     * @return Iterable with the friends of user with id = id
     */

    public ArrayList<User> getFriendsOf(UUID userId) throws RepositoryException {
        ArrayList<User> friends = new ArrayList<>();

        // Retrieving all friends of the user.
        this.friendshipRepository.getAll().forEach(friendship -> {
            // Deciding which one of the users is our user.
            if (friendship.getId().getLeft().equals(userId)) {
                Optional<User> friend = this.userRepository.getOne(friendship.getId().getRight());
                friend.ifPresent(friends::add);
            } else if (friendship.getId().getRight().equals(userId)) {
                Optional<User> friend = this.userRepository.getOne(friendship.getId().getLeft());
                friend.ifPresent(friends::add);
            }
        });

        return friends;
    }

    /**
     * Returns the list of friendships.
     *
     * @return The list of friendships.
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
     * Returns the number of communities and a list of the most active communities.
     */
    public Tuple<Integer, List<List<UUID>>> communities() {
        // List, which will contain the members of the most active community.
        List<List<UUID>> communityMembers = new ArrayList<>();

        // Set, which will contain all the users from the app.
        Set<UUID> userSet = new HashSet<>();

        // List, which will contain the user IDs.
        ArrayList<UUID> userIds = new ArrayList<>();

        // Map, which will contain the friends for each user.
        HashMap<UUID, List<UUID>> friends = new HashMap<>();

        // Populating the user IDs list.
        this.userRepository.getAll().forEach(user -> userIds.add(user.getId()));

        // Populating the friend's map with empty lists.
        userIds.forEach(userId -> friends.put(userId, new ArrayList<>()));

        // Populating the friend's map with all the friends.
        userIds.forEach(userId -> {
            Iterable<User> friendsOf = this.getFriendsOf(userId);
            friendsOf.forEach(user -> friends.get(userId).add(user.getId()));
        });

        // Initializing a new Graph.
        Graph graph = new Graph();

        // 'atomic' variable which will contain the community with the most interactions.
        final int[] max = {-1};

        // Finding the longest path of interactions between users.
        userIds.forEach(userId -> {
            if (!userSet.contains(userId)) {
                // Extracting a component of the graph based on a user.
                List<UUID> component = graph.runDFS(userId, userSet, friends);

                // Computing the longest path of the component.
                int path = graph.longestPath(component, friends);

                // Verifying if the maximum length should be updated.
                if (path > max[0]) {
                    // Clearing the community list, meaning that a longer path was found.
                    communityMembers.clear();
                    communityMembers.add(component);
                    max[0] = path;
                } else if (path == max[0]) {
                    // Adding a new component to the community list.
                    communityMembers.add(component);
                }
            }
        });

        // Retrieving the total number of communities.
        Integer noCommunities = graph.communities(userIds, friends).size();

        // Returning the total number of communities and all the communities.
        return new Tuple<>(noCommunities, communityMembers);
    }

    /**
     * Computes a list with users that have minimum N friends.
     *
     * @param N Minimum number of friends.
     * @return List of users that have minimum N friends.
     */
    public List<User> usersWithMinimumFriends(int N) {
        // Returning the list of users sorted by: Number of friends -> First name -> Last name, in ascending order.
        return this.getUsers().stream()
                .filter(user -> this.getFriendsOf(user.getId()).size() >= N)
                .sorted(Comparator
                        .comparingInt((User user) -> this.getFriendsOf(user.getId()).size()).reversed()
                        .thenComparing(User::getFirstName).reversed()
                        .thenComparing(User::getLastName).reversed())
                .toList();
    }

    /**
     * Returns the list of friends from a given month of the given user.
     *
     * @param userId ID of the user.
     * @param month  Month of the friendship date.
     * @return List of friends from a given month of the given user.
     */
    public List<User> friendsFromMonth(UUID userId, String month) {
        // Retrieving the user from the repository.
        Optional<User> userOptional = this.userRepository.getOne(userId);

        // Verifying if a user was actually retrieved.
        if (userOptional.isEmpty()) {
            throw new ServiceException("The user does not exist!");
        }

        // Retrieving all the friends of the user from the month 'month'.
        Iterable<Friendship> friendshipIterable = this.friendshipRepository.getAll();
        return StreamSupport.stream(friendshipIterable.spliterator(), false)
                .filter(friendship -> friendship.getFriendshipDate().getMonthValue() == Integer.parseInt(month))
                .filter(friendship -> friendship.getId().getLeft().equals(userId) || friendship.getId().getRight().equals(userId))
                .map(friendship -> {
                    Optional<User> user;
                    if (friendship.getId().getLeft().equals(userId)) {
                        user = this.userRepository.getOne(friendship.getId().getRight());
                    } else {
                        user = this.userRepository.getOne(friendship.getId().getLeft());
                    }
                    return user.orElse(null);
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of users for which the last name contains a given string.
     *
     * @param string String to verify.
     * @return List of users for which the last name contains a given string.
     */
    public List<User> usersWithStringInLastName(String string) throws ServiceException {
        try {
            // DB implemented operation, that's the explanation for the cast.
            UserDBRepository userDBRepository = (UserDBRepository) this.userRepository;
            return userDBRepository.usersLastNameContainsString(string);
        } catch (RepositoryException repositoryException) {
            throw new ServiceException(repositoryException.getMessage());
        }
    }

    /**
     * @param userId User ID of the user that received friend requests.
     * @return A list with all friend requests.
     */

    public List<FriendRequest> getFriendRequestsOfUser(UUID userId) {
        return StreamSupport.stream(this.friendRequestRepository.getAll().spliterator(), false)
                .filter(friendRequest -> friendRequest.getIdTo().equals(userId) && friendRequest.getStatus().equals("pending"))
                .toList();
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
        // Excluding cases when the users are already friends.
        if (this.getFriendsOf(user2.getId()).contains(user1)) {
            throw new ServiceException("The users are already friends!");
        }

        // Verifying if there is already a pending friend request between the two users.
        try {
            if (((FriendRequestDBRepository) this.friendRequestRepository).verifyPending(user1.getId(), user2.getId())) {
                throw new ServiceException("There is already a pending friend request between the two users!");
            }
        } catch (RepositoryException rE) {
            throw new ServiceException(rE.getMessage());
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

            // Making the two users friends.
            Friendship friendship = new Friendship(friendRequest.getIdFrom(), friendRequest.getIdTo());
            this.addFriendship(friendship.getId().getLeft(), friendship.getId().getRight());
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

    /**
     * Returns a list of messages between given users sorted by the date of sending.
     *
     * @param sender   User that sent messages.
     * @param receiver User that received messages.
     * @return A list of messages between given users.
     */
    public List<Message> getMessagesBetweenUsers(User sender, User receiver) {
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
            if (message.getClass().equals(Message.class)) {
                this.notify(new MessageEvent(EventType.SEND_MESSAGE, message));
            } else if (message.getClass().equals(ReplyMessage.class)) {
                this.notify(new MessageEvent(EventType.REPLY_MESSAGE, message));
            }
        } catch (RepositoryException repositoryException) {
            throw new ServiceException(repositoryException.getMessage(), repositoryException.getCause());
        }
    }

    /**
     * Determines if a user can log in to the network.
     *
     * @param userEmail Email of the user that wants to log in.
     * @param password  Password entered by the user.
     * @return The data of the User that tries to log in.
     * @throws ServiceException If the user cannot log in to the network.
     */
    public User tryLoginUser(String userEmail, String password) throws ServiceException {
        try {
            return ((UserDBRepository) this.userRepository).retrieveExistingUser(userEmail, password);
        } catch (RepositoryException rE) {
            throw new ServiceException(rE.getMessage());
        }
    }

    /**
     * Retrieves maximum {@code noOfUsers} users of the page {@code page}.
     *
     * @param page      Page to retrieve.
     * @param noOfUsers Number of users on the page.
     * @return A list of users.
     * @throws ServiceException If something went wrong with retrieving the users.
     */
    public List<User> getUsersFromPage(int page, int noOfUsers) throws ServiceException {
        ((UserDBRepository) this.userRepository).setCurrentPage(page);
        ((UserDBRepository) this.userRepository).setNoItemsPerPage(noOfUsers);
        return (List<User>) ((UserDBRepository) this.userRepository).getItemsOnPage();
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
}
