package ro.ubbcluj.map.socialnetworkfx.repository;

import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;

import java.sql.*;
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
    protected Friendship extractFromResultSet(ResultSet resultSet) throws SQLException {
        String idUser1 = resultSet.getString("id_user1");
        String idUser2 = resultSet.getString("id_user2");
        Timestamp date = resultSet.getTimestamp("date");
        return new Friendship(UUID.fromString(idUser1), UUID.fromString(idUser2), date.toLocalDateTime());
    }
}
