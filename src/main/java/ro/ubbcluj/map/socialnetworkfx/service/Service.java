package ro.ubbcluj.map.socialnetworkfx.service;

import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.EventType;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserChangeEvent;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.exception.ValidatorException;
import ro.ubbcluj.map.socialnetworkfx.repository.Repository;
import ro.ubbcluj.map.socialnetworkfx.repository.UserDBRepository;
import ro.ubbcluj.map.socialnetworkfx.utility.Graph;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observable;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;
import ro.ubbcluj.map.socialnetworkfx.validator.FriendshipValidator;
import ro.ubbcluj.map.socialnetworkfx.validator.UserValidator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Service implements AbstractService<UUID>, Observable<SocialNetworkEvent> {
    // Repository that stores Users.
    private final Repository<UUID, User> userRepository;
    // Repository that stores Friendships.
    private final Repository<Tuple<UUID, UUID>, Friendship> friendshipRepository;

    // Set of observers to the Service.
    private final Set<Observer<SocialNetworkEvent>> observers = new HashSet<>();

    public Service(Repository<UUID, User> userRepo, Repository<Tuple<UUID, UUID>, Friendship> friendshipRepo) {
        this.userRepository = userRepo;
        this.friendshipRepository = friendshipRepo;
    }

    @Override
    public void addUser(String firstName, String lastName, String email) throws ServiceException {
        // User that will be saved in the Repository
        User user = new User(firstName, lastName, email);
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
        this.notify(new UserChangeEvent(EventType.ADD_USER, user, null));
    }

    @Override
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
        this.notify(new UserChangeEvent(EventType.REMOVE_USER, null, deleted.get()));

        // Returning the deleted user.
        return deleted.get();
    }

    @Override
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
            this.notify(new UserChangeEvent(EventType.UPDATE_USER, user, old.get()));

            // Returning the old user.
            return old.get();
        } catch (RepositoryException rE) {
            throw new ServiceException(rE.getMessage());
        }
    }

    @Override
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

    @Override
    public ArrayList<User> getUsers() {
        ArrayList<User> userList = new ArrayList<>();

        // Populating the user list.
        this.userRepository.getAll().forEach(userList::add);
        return userList;
    }

    @Override
    public ArrayList<User> getFriendsOf(UUID uuid) throws RepositoryException {
        ArrayList<User> friends = new ArrayList<>();

        // Retrieving all friends of the user.
        this.friendshipRepository.getAll().forEach(friendship -> {
            // Deciding which one of the users is our user.
            if (friendship.getId().getLeft().equals(uuid)) {
                Optional<User> friend = this.userRepository.getOne(friendship.getId().getRight());
                friend.ifPresent(friends::add);
            } else if (friendship.getId().getRight().equals(uuid)) {
                Optional<User> friend = this.userRepository.getOne(friendship.getId().getLeft());
                friend.ifPresent(friends::add);
            }
        });

        return friends;
    }

    @Override
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
        } catch (ValidatorException | RepositoryException exception) {
            throw new ServiceException("Couldn't add friendship.", exception);
        }
    }

    @Override
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

        // Returning the deleted friendship.
        return friendship.get();
    }

    @Override
    public ArrayList<Friendship> getFriendships() {
        ArrayList<Friendship> friendshipList = new ArrayList<>();

        // Populating the friendship list.
        this.friendshipRepository.getAll().forEach(friendshipList::add);
        return friendshipList;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public List<User> usersWithStringInLastName(String string) throws ServiceException {
        try {
            // DB implemented operation, that's the explanation for the cast.
            UserDBRepository userDBRepository = (UserDBRepository) this.userRepository;
            return userDBRepository.usersLastNameContainsString(string);
        } catch (RepositoryException repositoryException) {
            throw new ServiceException(repositoryException.getMessage());
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
