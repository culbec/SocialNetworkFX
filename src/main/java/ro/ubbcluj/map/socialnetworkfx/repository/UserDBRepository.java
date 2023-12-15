package ro.ubbcluj.map.socialnetworkfx.repository;

import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDBRepository extends DBRepository<UUID, User> {
    public UserDBRepository(String db_url, String username, String password) {
        super(db_url, username, password);
    }

    @Override
    protected PreparedStatement statementCount(Connection connection) throws RepositoryException {
        try {
            return connection.prepareStatement("select COUNT(*) from users");
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectAll(Connection connection) throws RepositoryException {
        try {
            return connection.prepareStatement("select * from users");
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnID(Connection connection, UUID uuid) throws RepositoryException {
        String sql = "select * from users where id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, uuid);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnFields(Connection connection, User user) throws RepositoryException {
        String sql = "select * from users where first_name = ? AND last_name = ? AND email = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementInsert(Connection connection, User user) throws RepositoryException {
        String sql = "insert into users(id, first_name, last_name, email) values(?, ?, ?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, user.getId());
            statement.setString(2, user.getFirstName());
            statement.setObject(3, user.getLastName());
            statement.setObject(4, user.getEmail());
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementDelete(Connection connection, UUID uuid) throws RepositoryException {
        String sql = "delete from users where id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, uuid);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementUpdate(Connection connection, User user) throws RepositoryException {
        String sql = "update users set first_name = ?, last_name = ?, email = ? where id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setObject(4, user.getId());
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    protected PreparedStatement statementLastNameLike(Connection connection, String string) throws RepositoryException {
        String sql = "select * from users where last_name like ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, "%" + string + "%");
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected User extractFromResultSet(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("ID"));
        String first_name = resultSet.getString("first_name");
        String last_name = resultSet.getString("last_name");
        String email = resultSet.getString("email");

        return new User(id, first_name, last_name, email);
    }

    /**
     * Returns the list of users that have a substring equal to 'string' in their last name.
     *
     * @param string Substring to find.
     * @return List of users that have a substring equal to 'string' in their last name.
     * @throws RepositoryException SQL related exceptions.
     */
    public List<User> usersLastNameContainsString(String string) throws RepositoryException {
        try (Connection connection = this.connect()) {
            try (PreparedStatement statement = this.statementLastNameLike(connection, string)) {
                ResultSet resultSet = statement.executeQuery();
                List<User> userList = new ArrayList<>();

                while (resultSet.next()) {
                    String ID = resultSet.getString("ID");
                    String first_name = resultSet.getString("first_name");
                    String last_name = resultSet.getString("last_name");
                    String email = resultSet.getString("email");

                    User user = new User(UUID.fromString(ID), first_name, last_name, email);
                    userList.add(user);
                }
                return userList;
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }
}
