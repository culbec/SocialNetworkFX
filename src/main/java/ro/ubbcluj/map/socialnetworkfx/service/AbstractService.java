package ro.ubbcluj.map.socialnetworkfx.service;

import ro.ubbcluj.map.socialnetworkfx.entity.*;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;

import java.util.ArrayList;
import java.util.List;

public interface AbstractService<ID> {
    /**
     * Adds a user to the list of users.
     *
     * @param firstName The First name of the user to be added.
     * @param lastName  The Last name of the user to be added.
     * @param email     Email of the user to be added.
     * @throws ServiceException If the user couldn't be added.
     */

    void addUser(String firstName, String lastName, String email) throws ServiceException;

    /**
     * Removes a user from the list of users.
     *
     * @param id ID of the user to be removed
     * @return an {@code Optional} encapsulating the removed user.
     * @throws ServiceException If the user can't be removed.
     */
    User removeUser(ID id) throws ServiceException;

    /**
     * Gets a user based on its email.
     *
     * @param id ID of the user to get.
     * @return an {@code Optional} encapsulating the user.
     * @throws ServiceException If the user couldn't be found.
     */
    User getUser(ID id) throws ServiceException;

    /**
     * Update the user with the specified ID with user.
     *
     * @param user New user.
     * @return Old user.
     * @throws ServiceException if the user couldn't be updated.
     */
    User updateUser(User user) throws ServiceException;

    /**
     * Returns the user list.
     *
     * @return User list.
     */
    ArrayList<User> getUsers();

    /**
     * Returns an iterable of users which are friends with the user with the specified id.
     *
     * @param id ID of user.
     * @return Iterable with the friends of user with id = id
     */
    ArrayList<User> getFriendsOf(ID id) throws RepositoryException;

    /**
     * Adds a friendship between two users.
     *
     * @param id1 ID of the first user in the friendship.
     * @param id2 ID of the second user in the friendship.
     * @throws ServiceException If the friendship already exists
     */
    void addFriendship(ID id1, ID id2) throws ServiceException, RepositoryException;

    /**
     * Removes the friendship between two users.
     *
     * @param id1 ID of the first user.
     * @param id2 ID of the second user.
     * @return Friendship that was removed.
     * @throws ServiceException If the friendship couldn't be found.
     */
    Entity<Tuple<ID, ID>> removeFriendship(ID id1, ID id2) throws ServiceException;


    /**
     * Returns the list of friendships.
     *
     * @return The list of friendships.
     */
    ArrayList<Friendship> getFriendships();


    /**
     * Returns the number of communities and a list of the most active communities.
     */
    Tuple<Integer, List<List<ID>>> communities();

    /**
     * Computes a list with users that have minimum N friends.
     *
     * @param N Minimum number of friends.
     * @return List of users that have minimum N friends.
     */
    List<User> usersWithMinimumFriends(int N);

    /**
     * Returns the list of friends from a given month of the given user.
     *
     * @param id    ID of the user.
     * @param month Month of the friendship date.
     * @return List of friends from a given month of the given user.
     */
    List<User> friendsFromMonth(ID id, String month);

    /**
     * Returns a list of users for which the last name contains a given string.
     *
     * @param string String to verify.
     * @return List of users for which the last name contains a given string.
     */
    List<User> usersWithStringInLastName(String string) throws ServiceException;

    /**
     * @param userId User ID of the user that received friend requests.
     * @return A list with all friend requests.
     */
    List<FriendRequest> getFriendRequestsOfUser(ID userId);

    /**
     * Sends a friend request from a user to another.
     * @param user1 User that sends the friend request.
     * @param user2 User that receives the friend request.
     */
    void sendFriendRequest(User user1, User user2);

    /**
     * Accepts a friend request between to users and make the two users friends.
     * @param friendRequest Friend request between two users.
     */
    void acceptFriendRequest(FriendRequest friendRequest);

    /**
     * Rejects a friend request between two users.
     * @param friendRequest Friend request between two users.
     */
    void rejectFriendRequest(FriendRequest friendRequest);
}
