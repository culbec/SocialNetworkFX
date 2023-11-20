package ro.ubbcluj.map.socialnetworkfx.utility;

import java.util.*;

public class Graph {

    /**
     * Lee algorithm to calculate the longest path from a source to the rest of the members.
     *
     * @param source  Source from where we compute the longest path.
     * @param users   Set of users.
     * @param friends Collection of User/Friends_of_User
     * @return Longest path from source to all other members in the network.
     */
    private int lee(UUID source, Set<UUID> users, HashMap<UUID, List<UUID>> friends) {
        final int[] max = {-1};

        for (UUID uuid : friends.get(source)) {
            if (!users.contains(uuid)) {
                users.add(uuid);
                int tempPath = lee(uuid, users, friends);
                if (tempPath > max[0]) {
                    max[0] = tempPath;
                }
                users.remove(uuid);
            }
        }

        return max[0] + 1;
    }

    /**
     * Longest path from a source to the rest of the members.
     *
     * @param source  Source where we start the computing.
     * @param friends Collection of User/Friends_of_User
     * @return Longest path from source to the rest of the members.
     */
    private int longestPathFromSource(UUID source, HashMap<UUID, List<UUID>> friends) {
        Set<UUID> set = new HashSet<>();
        return lee(source, set, friends);
    }

    /**
     * Longest path of the whole graph on the network.
     *
     * @param users   Users from the network.
     * @param friends Collection of User/Friends_of_User
     * @return Longest path in the graph.
     */
    public int longestPath(Iterable<UUID> users, HashMap<UUID, List<UUID>> friends) {
        final int[] max = {0};

        for (UUID uuid : users) {
            int path = this.longestPathFromSource(uuid, friends);
            if (max[0] < path) {
                max[0] = path;
            }
        }

        return max[0];
    }

    /**
     * DFS on the network.
     *
     * @param userId  UserId from where we start the search.
     * @param users   Set of users.
     * @param friends Friends with the user with userId.
     * @return List of a community of users.
     */
    public List<UUID> runDFS(UUID userId, Set<UUID> users, HashMap<UUID, List<UUID>> friends) {
        List<UUID> list = new ArrayList<>();
        list.add(userId);
        users.add(userId);

        friends.get(userId).forEach(_userId -> {
            if (!users.contains(_userId)) {
                List<UUID> uuidFriends = runDFS(_userId, users, friends);
                list.addAll(uuidFriends);
            }
        });

        return list;
    }

    /**
     * Calculates the number of communities from a given graph using DFS.
     *
     * @return Each user with its community number.
     */
    public List<List<UUID>> communities(Iterable<UUID> users, HashMap<UUID, List<UUID>> friends) {
        Set<UUID> set = new HashSet<>();
        List<List<UUID>> list = new ArrayList<>();

        users.forEach(userId -> {
            if (!set.contains(userId)) {
                list.add(runDFS(userId, set, friends));
            }
        });

        return list;
    }
}
