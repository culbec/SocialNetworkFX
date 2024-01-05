package ro.ubbcluj.map.socialnetworkfx.repository;

import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendshipDBRepository extends DBRepository<Tuple<UUID, UUID>, Friendship> {
    public FriendshipDBRepository(String db_url, String username, String password) {
        super(db_url, username, password);
    }

    @Override
    protected PreparedStatement statementCount(Connection connection) throws RepositoryException {
        try {
            return connection.prepareStatement("select COUNT(*) from friendships");
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectAll(Connection connection) throws RepositoryException {
        try {
            return connection.prepareStatement("select * from friendships");
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    /**
     * Sets the user IDs of the friendship to a specific prepared statement.
     *
     * @param statement PreparedStatement to edit.
     * @param id        ID in cause.
     * @param offset    From which position to insert the ids.
     * @throws SQLException Related to the statement editing.
     */
    private void setIdStatement(PreparedStatement statement, Tuple<UUID, UUID> id, int offset) throws SQLException {
        statement.setObject(offset, id.getLeft());
        statement.setObject(offset + 1, id.getRight());
        statement.setObject(offset + 2, id.getRight());
        statement.setObject(offset + 3, id.getLeft());
    }

    @Override
    protected PreparedStatement statementSelectOnID(Connection connection, Tuple<UUID, UUID> id) throws RepositoryException {
        String sql = "select * from friendships where (id_user1 = ? AND id_user2 = ?) OR (id_user1 = ? AND id_user2 = ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            setIdStatement(statement, id, 1);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnFields(Connection connection, Friendship friendship) throws RepositoryException {
        String sql = "select * from friendships where (id_user1 = ? AND id_user2 = ?) OR (id_user1 = ? AND id_user2 = ?) AND date = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            setIdStatement(statement, friendship.getId(), 1);
            statement.setObject(5, friendship.getFriendshipDate());
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementInsert(Connection connection, Friendship friendship) throws RepositoryException {
        String sql = "insert into friendships(id_user1, id_user2, date) values(?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, friendship.getId().getLeft());
            statement.setObject(2, friendship.getId().getRight());
            statement.setObject(3, friendship.getFriendshipDate());
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementDelete(Connection connection, Tuple<UUID, UUID> id) throws RepositoryException {
        String sql = "delete from friendships where (id_user1 = ? AND id_user2 = ?) OR (id_user1 = ? AND id_user2 = ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            setIdStatement(statement, id, 1);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementUpdate(Connection connection, Friendship friendship) throws RepositoryException {
        String sql = "update friendships set date = ? where (id_user1 = ? AND id_user2 = ?) OR (id_user2 = ? AND id_user1 = ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, friendship.getFriendshipDate());
            setIdStatement(statement, friendship.getId(), 2);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnPage(Connection connection, int page, int noOfItems) throws RepositoryException {
        int selectOffset = page * noOfItems - 1;
        String sql = "select * from friendships limit ? offset ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, noOfItems);
            statement.setInt(2, selectOffset);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    /**
     * Retrieves the friends of a user from a specific page.
     *
     * @param page      Page from which to retrieve the friends.
     * @param noOfItems Number of items per page.
     * @param userId    User ID of the user we search the friends of.
     * @return List of users that are friends with the user.
     * @throws RepositoryException If the retrieval fails.
     */
    public List<User> getFriendsFromPage(int page, int noOfItems, UUID userId) throws RepositoryException {
        List<User> friends = new ArrayList<>();
        int selectOffset = page * noOfItems;

        String sql = "select * from users " +
                "inner join friendships on friendships.id_user1 = ? or friendships.id_user2 = ? " +
                "where (users.id = friendships.id_user1 or users.id = friendships.id_user2) and users.id != ? " +
                "limit ? offset ?";
        try (Connection connection = this.connect()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, userId);
                statement.setObject(2, userId);
                statement.setObject(3, userId);
                statement.setInt(4, noOfItems);
                statement.setInt(5, selectOffset);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        UUID id = UUID.fromString(resultSet.getString("id"));
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");
                        String email = resultSet.getString("email");
                        String password = resultSet.getString("password");

                        friends.add(new User(id, firstName, lastName, email, password));
                    }
                } catch (SQLException sqlException) {
                    throw new RepositoryException(sqlException.getMessage());
                }
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }

//        String sql = "select * from friendships where id_user1 = ? OR id_user2 = ? limit ? offset ?";
//        try (Connection connection = this.connect()) {
//            try (PreparedStatement statement = connection.prepareStatement(sql)) {
//                statement.setObject(1, userId);
//                statement.setObject(2, userId);
//                statement.setInt(3, noOfItems);
//                statement.setInt(4, selectOffset);
//                try (ResultSet resultSet = statement.executeQuery()) {
//                    while (resultSet.next()) {
//                        String sqlUser = "select * from users where id = ?";
//                        try (PreparedStatement statementUser = connection.prepareStatement(sqlUser)) {
//                            if (resultSet.getString("id_user1").equals(userId.toString())) {
//                                statementUser.setObject(1, resultSet.getObject("id_user2"));
//                            } else {
//                                statementUser.setObject(1, resultSet.getObject("id_user1"));
//                            }
//                            try (ResultSet resultSetUser = statementUser.executeQuery()) {
//                                while (resultSetUser.next()) {
//                                    UUID id = UUID.fromString(resultSetUser.getString("id"));
//                                    String firstName = resultSetUser.getString("first_name");
//                                    String lastName = resultSetUser.getString("last_name");
//                                    String email = resultSetUser.getString("email");
//                                    String password = resultSetUser.getString("password");
//
//                                    friends.add(new User(id, firstName, lastName, email, password));
//                                }
//                            } catch (SQLException sqlException) {
//                                throw new RepositoryException(sqlException.getMessage());
//                            }
//                        } catch (SQLException sqlException) {
//                            throw new RepositoryException(sqlException.getMessage());
//                        }
//                    }
//                } catch (SQLException sqlException) {
//                    throw new RepositoryException(sqlException.getMessage());
//                }
//            } catch (SQLException sqlException) {
//                throw new RepositoryException(sqlException.getMessage());
//            }
//        } catch (SQLException sqlException) {
//            throw new RepositoryException(sqlException.getMessage());
//        }
        return friends;
    }

    @Override
    protected Friendship extractFromResultSet(ResultSet resultSet) throws SQLException {
        UUID idUser1 = UUID.fromString(resultSet.getString("id_user1"));
        UUID idUser2 = UUID.fromString(resultSet.getString("id_user2"));
        Timestamp date = resultSet.getTimestamp("date");
        return new Friendship(idUser1, idUser2, date.toLocalDateTime());
    }
}
