package ro.ubbcluj.map.socialnetworkfx.repository;

import ro.ubbcluj.map.socialnetworkfx.entity.FriendRequest;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendRequestDBRepository extends DBRepository<Tuple<Tuple<UUID, UUID>, LocalDateTime>, FriendRequest> {
    /**
     * Initializes a database Repository.
     *
     * @param db_url   URL of the database.
     * @param username Username for the connection.
     * @param password Password for the connection.
     */
    public FriendRequestDBRepository(String db_url, String username, String password) {
        super(db_url, username, password);
    }

    /**
     * Sets ID fields for a statement.
     *
     * @param statement Statement to be set.
     * @param id        ID of the object.
     */
    private void setFieldsID(PreparedStatement statement, Tuple<Tuple<UUID, UUID>, LocalDateTime> id) throws SQLException {
        statement.setObject(1, id.getLeft().getLeft());
        statement.setObject(2, id.getLeft().getRight());
        statement.setTimestamp(3, Timestamp.valueOf(id.getRight()));
    }

    /**
     * Sets fields for a statement.
     *
     * @param statement     Statement to be set.
     * @param friendRequest Friend request with specific fields.
     */
    private void setFields(PreparedStatement statement, FriendRequest friendRequest) throws SQLException {
        statement.setObject(1, friendRequest.getIdFrom());
        statement.setObject(2, friendRequest.getIdTo());
        statement.setObject(3, friendRequest.getStatus());
        statement.setTimestamp(4, Timestamp.valueOf(friendRequest.getDate()));
    }

    @Override
    protected PreparedStatement statementCount(Connection connection) throws RepositoryException {
        try {
            return connection.prepareStatement("select COUNT(*) from friendrequests");
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectAll(Connection connection) throws RepositoryException {
        try {
            return connection.prepareStatement("select * from friendrequests");
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnID(Connection connection, Tuple<Tuple<UUID, UUID>, LocalDateTime> id) throws RepositoryException {
        String sql = "select * from friendrequests where id_user1 = ? AND id_user2 = ? AND date = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            this.setFieldsID(statement, id);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnFields(Connection connection, FriendRequest friendRequest) throws RepositoryException {
        String sql = "select * from friendrequests where id_user1 = ? AND id_user2 = ? AND status = ? AND date = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            this.setFields(statement, friendRequest);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementInsert(Connection connection, FriendRequest friendRequest) throws RepositoryException {
        String sql = "insert into friendrequests(id_user1, id_user2, status, date) values(?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            this.setFields(statement, friendRequest);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementDelete(Connection connection, Tuple<Tuple<UUID, UUID>, LocalDateTime> id) throws RepositoryException {
        String sql = "delete from friendrequests where id_user1 = ? AND id_user2 = ? AND date = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            this.setFieldsID(statement, id);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementUpdate(Connection connection, FriendRequest friendRequest) throws RepositoryException {
        String sql = "update friendrequests set status = ? where id_user1 = ? AND id_user2 = ? AND status = 'pending'";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, friendRequest.getStatus());
            statement.setObject(2, friendRequest.getIdFrom());
            statement.setObject(3, friendRequest.getIdTo());
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnPage(Connection connection, int noOfItems, int selectOffset) throws RepositoryException {
        String sql = "select * from friendrequests limit ? offset ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, noOfItems);
            statement.setInt(2, selectOffset);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected FriendRequest extractFromResultSet(ResultSet resultSet) throws SQLException {
        UUID idUser1 = UUID.fromString(resultSet.getString("id_user1"));
        UUID idUser2 = UUID.fromString(resultSet.getString("id_user2"));
        String status = resultSet.getString("status");
        Timestamp timestamp = resultSet.getTimestamp("date");


        return new FriendRequest(idUser1, idUser2, status, timestamp.toLocalDateTime());
    }

    /**
     * Verifies if there is still a pending friend request between two users.
     *
     * @param idUser1 ID of the user who sent the friend request.
     * @param idUser2 ID of the user who received the friend request.
     */
    public boolean verifyPending(UUID idUser1, UUID idUser2) {
        try (Connection connection = this.connect()) {
            String sql = "select COUNT(*) from friendrequests where id_user1 = ? AND id_user2 = ? AND status = 'pending'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, idUser1);
                statement.setObject(2, idUser2);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1) != 0;
                    }
                }
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
        return false;
    }

    /**
     * @param userId User ID to find the pending requests to.
     * @return A list of pending friend requests to the user with {@code userId}.
     */
    public List<FriendRequest> getPendingRequestsTo(UUID userId) {
        String sql = "select * from friendrequests where id_user2 = ? and status = ?";
        try (Connection connection = this.connect()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, userId);
                statement.setString(2, "pending");
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<FriendRequest> friendRequests = new ArrayList<>();
                    while(resultSet.next()) {
                        friendRequests.add(this.extractFromResultSet(resultSet));
                    }
                    return friendRequests;
                } catch (SQLException sqlException) {
                    throw new RepositoryException(sqlException.getMessage());
                }
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }
}
