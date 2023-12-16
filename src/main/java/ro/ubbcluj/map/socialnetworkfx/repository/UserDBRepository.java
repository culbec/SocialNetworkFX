package ro.ubbcluj.map.socialnetworkfx.repository;

import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCrypt;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDBRepository extends DBRepository<UUID, User> {
    public UserDBRepository(String db_url, String username, String password) {
        super(db_url, username, password);
    }

    @Override
    public Optional<User> save(User user) throws RepositoryException, IllegalArgumentException {
        user.setPassword(this.encryptPassword(user.getPassword()));
        return super.save(user);
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
        String sql = "select * from users where email = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getEmail());
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementInsert(Connection connection, User user) throws RepositoryException {
        String sql = "insert into users values(?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, user.getId());
            statement.setString(2, user.getFirstName());
            statement.setObject(3, user.getLastName());
            statement.setObject(4, user.getEmail());
            statement.setObject(5, user.getPassword());
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

    @Override
    protected PreparedStatement statementSelectOnPage(Connection connection, int noOfItems, int selectOffset) throws RepositoryException {
        String sql = "select * from users limit ? offset ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, noOfItems);
            statement.setInt(2, selectOffset);
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
        String password = resultSet.getString("password");

        return new User(id, first_name, last_name, email, password);
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
                    String password = resultSet.getString("password");

                    User user = new User(UUID.fromString(ID), first_name, last_name, email, password);
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

    /**
     * Encrypts a password entered by a user on sign up.
     *
     * @param plainPassword Un-encrypted password of the user.
     * @return Encrypted password.
     */
    private String encryptPassword(String plainPassword) {
        if (plainPassword != null) {
            return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        }
        return null;
    }

    /**
     * Verifies if a user entered it's correct password.
     *
     * @param password  Password entered by a user.
     * @param encrypted Encrypted password.
     * @return True if the password is correct, false otherwise.
     */
    private boolean checkPassword(String password, String encrypted) {
        return BCrypt.checkpw(password, encrypted);
    }

    /**
     * Retrieves an existing user based on its email and password, if both of them are correct.
     *
     * @param userEmail Email of the user.
     * @param password  Password entered by the user.
     * @return The user with {@code userEmail} as email and {@code password} as password.
     * @throws RepositoryException If the data entered is incorrect.
     */
    public User retrieveExistingUser(String userEmail, String password) throws RepositoryException {
        // SQL statement that retrieves a user based on its email.
        String sql = "select * from users where email = ?";

        try (Connection connection = this.connect()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, userEmail);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new RepositoryException("No user with the specified email exists!");
                    }
                    // Retrieving the encrypted password from the database.
                    String encrypted = resultSet.getString("password");
                    if (!this.checkPassword(password, encrypted)) {
                        throw new RepositoryException("Invalid password!");
                    }

                    // Returning the user if the password entered is correct.
                    return this.extractFromResultSet(resultSet);

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

