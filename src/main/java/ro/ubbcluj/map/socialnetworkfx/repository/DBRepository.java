package ro.ubbcluj.map.socialnetworkfx.repository;

import ro.ubbcluj.map.socialnetworkfx.entity.Entity;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database repository based on the Repository interface
 *
 * @param <ID> ID of the stored entity.
 * @param <E>  Entity stored.
 */
public abstract class DBRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    // URL of the database
    private static String DB_URL;
    // User credentials for the connection with the database
    private static String USERNAME;
    private static String PASSWORD;

    /**
     * Initializes a database Repository.
     *
     * @param db_url   URL of the database.
     * @param username Username for the connection.
     * @param password Password for the connection.
     */

    public DBRepository(String db_url, String username, String password) {
        DB_URL = db_url;
        USERNAME = username;
        PASSWORD = password;
    }

    /**
     * Returns the SQL Interrogation for counting the rows in a table.
     *
     * @return SQL Interrogation for counting the rows in a table.
     */
    public abstract PreparedStatement statementCount(Connection connection) throws RepositoryException;

    /**
     * Returns the SQL Interrogation for selecting all entries in a table.
     *
     * @return SQL Interrogation for selecting all entries in a table.
     */
    public abstract PreparedStatement statementSelectAll(Connection connection) throws RepositoryException;

    /**
     * Returns the SQL Interrogation for selection by ID.
     *
     * @param id ID on which the interrogation will proceed.
     * @return SQL Interrogation for selection by ID.
     */
    public abstract PreparedStatement statementSelectOnID(Connection connection, ID id) throws RepositoryException;

    /**
     * Returns the SQL Interrogation for selection by fields.
     *
     * @param entity Entity on which the interrogation will proceed.
     * @return SQL Interrogation for selection by fields.
     */
    public abstract PreparedStatement statementSelectOnFields(Connection connection, E entity) throws RepositoryException;

    /**
     * Returns the SQL Interrogation for inserting into a table.
     *
     * @param entity Entity on which the interrogation will proceed.
     * @return SQL Interrogation for inserting into a table.
     */
    public abstract PreparedStatement statementInsert(Connection connection, E entity) throws RepositoryException;

    /**
     * Returns the SQL Interrogation for deleting from a table.
     *
     * @param id ID on which the interrogation will proceed.
     * @return SQL Interrogation for deleting from a table.
     */
    public abstract PreparedStatement statementDelete(Connection connection, ID id) throws RepositoryException;

    /**
     * Returns the SQL Interrogation for updating rows in a table.
     *
     * @param entity Entity on which the interrogation will proceed.
     * @return SQL Interrogation for updating rows in table.
     */
    public abstract PreparedStatement statementUpdate(Connection connection, E entity) throws RepositoryException;

    /**
     * Connects to the database.
     *
     * @return A connection to the database.
     */
    public Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    /**
     * Extracts an Entity from a given result set.
     *
     * @param resultSet Given result set.
     * @return Entity extracted from the result set.
     * @throws SQLException Resulted from the extraction if a problem was encountered.
     */
    protected abstract E extractFromResultSet(ResultSet resultSet) throws SQLException;

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public int size() throws RepositoryException {
        try (Connection connection = this.connect()) {
            try (PreparedStatement statement = this.statementCount(connection)) {
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }

        return 0;
    }

    @Override
    public Iterable<E> getAll() throws RepositoryException {
        List<E> entities = new ArrayList<>();

        try (Connection connection = this.connect()) {
            try (PreparedStatement statement = this.statementSelectAll(connection)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    E entity = this.extractFromResultSet(resultSet);
                    entities.add(entity);
                }
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }

        return entities;
    }

    @Override
    public Optional<E> getOne(ID id) throws IllegalArgumentException, RepositoryException {
        if (id == null) {
            throw new IllegalArgumentException("The id cannot be null!");
        }

        try (Connection connection = this.connect()) {
            try (PreparedStatement statement = this.statementSelectOnID(connection, id)) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(this.extractFromResultSet(resultSet));
                }
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<E> save(E entity) throws RepositoryException, IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null!");
        }

        try (Connection connection = this.connect()) {
            try (PreparedStatement statementSelect = this.statementSelectOnFields(connection, entity)) {
                ResultSet resultSetSelect = statementSelect.executeQuery();

                if (resultSetSelect.next()) {
                    E _entity = this.extractFromResultSet(resultSetSelect);
                    return Optional.of(_entity);
                }

                try (PreparedStatement statementInsert = this.statementInsert(connection, entity)) {
                    statementInsert.execute();
                } catch (SQLException sqlException) {
                    throw new RepositoryException(sqlException.getMessage());
                }

            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<E> delete(ID id) throws RepositoryException, IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null!");
        }

        try (Connection connection = this.connect()) {
            try (PreparedStatement statementSelect = this.statementSelectOnID(connection, id)) {
                ResultSet resultSetSelect = statementSelect.executeQuery();
                if (resultSetSelect.next()) {
                    try (PreparedStatement statementDelete = this.statementDelete(connection, id)) {
                        statementDelete.execute();
                        return Optional.of(this.extractFromResultSet(resultSetSelect));
                    } catch (SQLException sqlException) {
                        throw new RepositoryException(sqlException.getMessage());
                    }
                }
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<E> update(E entity) throws RepositoryException, IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("Id cannot be null!");
        }

        try (Connection connection = this.connect()) {
            try (PreparedStatement statementSelect = this.statementSelectOnID(connection, entity.getId())) {
                ResultSet resultSetSelect = statementSelect.executeQuery();

                if (resultSetSelect.next()) {
                    try (PreparedStatement statementUpdate = this.statementUpdate(connection, entity)) {
                        Optional<E> old = Optional.of(this.extractFromResultSet(resultSetSelect));
                        statementUpdate.execute();
                        return old;
                    } catch (SQLException sqlException) {
                        throw new RepositoryException(sqlException.getMessage());
                    }
                }
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }

        return Optional.empty();
    }
}
