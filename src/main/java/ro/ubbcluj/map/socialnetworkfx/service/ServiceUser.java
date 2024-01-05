package ro.ubbcluj.map.socialnetworkfx.service;

import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.events.EventType;
import ro.ubbcluj.map.socialnetworkfx.events.SocialNetworkEvent;
import ro.ubbcluj.map.socialnetworkfx.events.UserEvent;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.exception.ValidatorException;
import ro.ubbcluj.map.socialnetworkfx.repository.Repository;
import ro.ubbcluj.map.socialnetworkfx.repository.UserDBRepository;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observable;
import ro.ubbcluj.map.socialnetworkfx.utility.observer.Observer;
import ro.ubbcluj.map.socialnetworkfx.validator.UserValidator;

import java.util.*;

public class ServiceUser implements IService {
    private final Repository<UUID, User> userRepository;
    private final Set<Observer<SocialNetworkEvent>> observers = new HashSet<>();

    public ServiceUser(Repository<UUID, User> userRepository) {
        this.userRepository = userRepository;
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
     * @return User list contained by the Repository.
     */

    public ArrayList<User> getUsers() {
        ArrayList<User> userList = new ArrayList<>();

        // Populating the user list.
        this.userRepository.getAll().forEach(userList::add);
        return userList;
    }


    /**
     * Adds a user to the Repository.
     *
     * @param firstName The first name of the user to be added.
     * @param lastName  The last name of the user to be added.
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
     * Retrieves the friends of a user based on the friend ID list passed.
     *
     * @param friendIdList IDs of the friends of the user.
     * @return A list that contains the friends of a user based on its friend ID list.
     */
    public List<User> getFriends(List<UUID> friendIdList) {
        return friendIdList.stream()
                .map(this::getUser)
                .toList();
    }

    /**
     * Returns a list of users for which the last name contains a given string.
     *
     * @param string String to verify.
     * @return List of users for which the last name contains a given string.
     */
    public List<User> getUsers_NameContainsString(String string) {
        if (this.userRepository.getClass().equals(UserDBRepository.class)) {
            try {
                // DB implemented operation, that's the explanation for the cast.
                UserDBRepository userDBRepository = (UserDBRepository) this.userRepository;
                return userDBRepository.usersLastNameContainsString(string);
            } catch (RepositoryException repositoryException) {
                throw new ServiceException(repositoryException.getMessage());
            }
        }

        throw new ServiceException("Unsupported operation!");
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
