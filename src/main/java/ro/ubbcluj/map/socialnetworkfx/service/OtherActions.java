package ro.ubbcluj.map.socialnetworkfx.service;

import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.utility.Graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Contains other functionalities of a social network.
 */
public class OtherActions {
    private final ServiceUser serviceUser;
    private final ServiceFriendship serviceFriendship;

    public OtherActions(ServiceUser serviceUser, ServiceFriendship serviceFriendship) {
        this.serviceUser = serviceUser;
        this.serviceFriendship = serviceFriendship;
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
        this.serviceUser.getUsers().forEach(user -> userIds.add(user.getId()));

        // Populating the friend's map with empty lists.
        userIds.forEach(userId -> friends.put(userId, new ArrayList<>()));

        // Populating the friend's map with all the friends.
        userIds.forEach(userId -> {
            Iterable<User> friendsOf = this.serviceUser.getFriends(this.serviceFriendship.getFriendIdsOf(userId));
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
        return this.serviceUser.getUsers().stream()
                .filter(user -> this.serviceFriendship.getFriendIdsOf(user.getId()).size() >= N)
                .sorted(Comparator
                        .comparingInt((User user) -> this.serviceFriendship.getFriendIdsOf(user.getId()).size()).reversed()
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
     * @throws ServiceException If a user couldn't be found.
     */
    public List<User> friendsFromMonth(UUID userId, String month) throws ServiceException {
        // Retrieving the user from the repository.
        User user = this.serviceUser.getUser(userId);

        // Retrieving all the friends of the user from the month 'month'.
        Iterable<Friendship> friendshipIterable = this.serviceFriendship.getFriendships();
        return StreamSupport.stream(friendshipIterable.spliterator(), false)
                .filter(friendship -> friendship.getFriendshipDate().getMonthValue() == Integer.parseInt(month))
                .filter(friendship -> friendship.getId().getLeft().equals(userId) || friendship.getId().getRight().equals(userId))
                .map(friendship -> {
                    User friend;
                    if (friendship.getId().getLeft().equals(userId)) {
                        friend = this.serviceUser.getUser(friendship.getId().getRight());
                    } else {
                        friend = this.serviceUser.getUser(friendship.getId().getLeft());
                    }
                    return friend;
                })
                .collect(Collectors.toList());
    }
}
