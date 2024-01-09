package ro.ubbcluj.map.socialnetworkfx.repository;

import ro.ubbcluj.map.socialnetworkfx.entity.Message;
import ro.ubbcluj.map.socialnetworkfx.entity.ReplyMessage;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MessageDBRepository extends DBRepository<UUID, Message> {
    public MessageDBRepository(String db_url, String username, String password) {
        super(db_url, username, password);
    }

    @Override
    protected PreparedStatement statementCount(Connection connection) throws RepositoryException {
        try {
            return connection.prepareStatement("select COUNT(*) from messages");
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectAll(Connection connection) throws RepositoryException {
        try {
            return connection.prepareStatement("select * from messages");
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnID(Connection connection, UUID messageId) throws RepositoryException {
        String sql = "select * from messages where id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, messageId);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnFields(Connection connection, Message message) throws RepositoryException {
        String sql = "select * from messages where id = ? AND message = ? AND date = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, message.getId());
            statement.setObject(2, message.getMessageText());
            statement.setObject(3, message.getDate());
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementInsert(Connection connection, Message message) throws RepositoryException {
        String sql = "insert into messages(id, message, date, reply_id) values(?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, message.getId());
            statement.setObject(2, message.getMessageText());
            statement.setObject(3, message.getDate());

            if (message.getClass().equals(ReplyMessage.class)) {
                statement.setObject(4, ((ReplyMessage) message).getMessage());
            } else {
                statement.setObject(4, null);
            }

            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementDelete(Connection connection, UUID messageId) throws RepositoryException {
        String sql = "delete from messages where id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, messageId);
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementUpdate(Connection connection, Message message) throws RepositoryException {
        String sql = "update messages set message = ?, date = ? where id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, message.getMessageText());
            statement.setObject(2, message.getDate());
            statement.setObject(3, message.getId());
            return statement;
        } catch (SQLException sqlException) {
            throw new RepositoryException(sqlException.getMessage());
        }
    }

    @Override
    protected PreparedStatement statementSelectOnPage(Connection connection, int noOfItems, int selectOffset) throws RepositoryException {
        String sql = "select * from messages limit ? offset ?";
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
    protected Message extractFromResultSet(ResultSet resultSet) throws SQLException {
        UUID messageId = UUID.fromString(resultSet.getString(1));
        String messageText = resultSet.getString(2);
        Timestamp date = resultSet.getTimestamp(3);
        Object replyId = resultSet.getObject(4);
        UUID idSender = null;
        List<UUID> idReceivers = new ArrayList<>();

        // Extracting the users involved in the message.
        try (Connection connection = this.connect()) {
            // Extracting the sender
            String sqlSender = "select id_sender from \"messagesUsers\" where id_message = ?";
            try (PreparedStatement statementSender = connection.prepareStatement(sqlSender)) {
                statementSender.setObject(1, messageId);
                try (ResultSet resultSetSender = statementSender.executeQuery()) {
                    if (resultSetSender.next()) {
                        idSender = UUID.fromString(resultSetSender.getString(1));
                    }
                } catch (SQLException sqlException) {
                    throw new RepositoryException(sqlException.getMessage());
                }
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
            // Extracting the receivers.
            String sqlReceivers = "select id_receiver from \"messagesUsers\" where id_message = ?";
            try (PreparedStatement statementReceivers = connection.prepareStatement(sqlReceivers)) {
                statementReceivers.setObject(1, messageId);
                try (ResultSet resultSetReceivers = statementReceivers.executeQuery()) {
                    while (resultSetReceivers.next()) {
                        UUID idReceiver = UUID.fromString(resultSetReceivers.getString(1));
                        idReceivers.add(idReceiver);
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

        Message message = new Message(messageId, idSender, idReceivers, messageText, date.toLocalDateTime());

        if (replyId == null) {
            // Returning the message.
            return message;
        }
        // Returning a reply.
        return new ReplyMessage(messageId, idSender, idReceivers, messageText, date.toLocalDateTime(), UUID.fromString(replyId.toString()));

    }

    @Override
    public Optional<Message> save(Message message) throws RepositoryException, IllegalArgumentException {
        // Saving the message into the database.
        Optional<Message> optionalMessage = super.save(message);
        // Linking the message to its users if the message hasn't been saved yet.
        if (optionalMessage.isEmpty()) {
            this.linkUsers(message);
        }
        // Returning the optional.
        return optionalMessage;
    }

    /**
     * Inserts the users in a link table with the message.
     *
     * @param message Message to be sent between users.
     */
    private void linkUsers(Message message) {
        String sql = "insert into \"messagesUsers\"(id_message, id_sender, id_receiver) values(?, ?, ?)";
        message.getTo().forEach(idReceiver -> {
            // Preparing a statement for each receiver to be added in the table.
            try (Connection connection = this.connect()) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setObject(1, message.getId());
                statement.setObject(2, message.getFrom());
                statement.setObject(3, idReceiver);
                // Adding the value into the table.
                statement.execute();
            } catch (SQLException sqlException) {
                throw new RepositoryException(sqlException.getMessage());
            }
        });
    }

    /**
     * Returns a list of messages between given users.
     *
     * @param idSender   Sender of the message.
     * @param idReceiver Receiver of the message.
     */
    public List<Message> getMessagesBetweenUsers(UUID idSender, UUID idReceiver) {
        // Initializing a list of messages.
        List<Message> messages = new ArrayList<>();

        String sql = "select * from messages " +
                "inner join \"messagesUsers\" mu on messages.id = mu.id_message " +
                "where (mu.id_sender = ? and mu.id_receiver = ?) or (mu.id_receiver = ? and mu.id_sender = ?)";
        try (Connection connection = this.connect()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, idSender);
                statement.setObject(2, idReceiver);
                statement.setObject(3, idSender);
                statement.setObject(4, idReceiver);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        // Adding the message.
                        messages.add(this.extractFromResultSet(resultSet));
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

        return messages;
    }
}
