import ro.ubbcluj.map.socialnetworkfx.entity.*;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendRequestDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendshipDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.MessageDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.UserDBRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

public class TestDBRepository {
    private static void clearDBUser(UserDBRepository userDBRepository) {
        try (Connection connection = userDBRepository.connect()) {
            try (PreparedStatement statement = connection.prepareStatement("delete from users")) {
                statement.execute();
            } catch (SQLException sqlException) {
                System.err.println(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
    }

    private static void clearDBFriendship(FriendshipDBRepository friendshipDBRepository) {
        try (Connection connection = friendshipDBRepository.connect()) {
            try (PreparedStatement statement = connection.prepareStatement("delete from friendships")) {
                statement.execute();
            } catch (SQLException sqlException) {
                System.err.println(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
    }

    private static void clearDBFriendrequest(FriendRequestDBRepository friendRequestDBRepository) {
        try (Connection connection = friendRequestDBRepository.connect()) {
            try (PreparedStatement statement = connection.prepareStatement("delete from friendrequests")) {
                statement.execute();
            } catch (SQLException sqlException) {
                System.err.println(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
    }

    private static void clearDBMessages(MessageDBRepository messageDBRepository) {
        try (Connection connection = messageDBRepository.connect()) {
            // Deleting the rows which contain the FKs.
            try (PreparedStatement statementDeleteMessagesUsers = connection.prepareStatement("delete from \"messagesUsers\"")) {
                statementDeleteMessagesUsers.execute();
            } catch (SQLException sqlException) {
                System.err.println(sqlException.getMessage());
            }
            // Deleting the messages.
            try (PreparedStatement statementDeleteMessages = connection.prepareStatement("delete from messages")) {
                statementDeleteMessages.execute();
            } catch (SQLException sqlException) {
                System.err.println(sqlException.getMessage());
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
    }

    public static void runUserDBRepository() {
        UserDBRepository userDBRepository = new UserDBRepository("jdbc:postgresql://localhost:5432/socialNetworkTests", "postgres", "postgres");
        clearDBUser(userDBRepository);

        assert userDBRepository.isEmpty();

        User user1 = new User("Ion", "Lungu", "ion.lungu@mail.com");
        User user2 = new User("Maria", "Lungu", "maria.lungu@mail.com");

        assert userDBRepository.save(user1).isEmpty();
        assert userDBRepository.save(user1).isPresent();

        assert userDBRepository.save(user2).isEmpty();

        assert !userDBRepository.isEmpty();
        assert userDBRepository.size() == 2;

        assert userDBRepository.delete(user1.getId()).isPresent();
        assert userDBRepository.update(user1).isEmpty();

        User user3 = new User(user2.getId(), "Andreea", "Lungu", "andreea.lungu@mail.com");
        assert userDBRepository.update(user3).isPresent();

        assert userDBRepository.getOne(user3.getId()).isPresent();

        System.out.println("UserDBRepository passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }

    public static void runFriendshipDBRepository() {
        FriendshipDBRepository friendshipDBRepository = new FriendshipDBRepository("jdbc:postgresql://localhost:5432/socialNetworkTests", "postgres", "postgres");
        UserDBRepository userDBRepository = new UserDBRepository("jdbc:postgresql://localhost:5432/socialNetworkTests", "postgres", "postgres");
        clearDBFriendship(friendshipDBRepository);
        clearDBUser(userDBRepository);

        assert friendshipDBRepository.isEmpty();

        User user1 = new User("Ion", "Lungu", "ion.lungu@mail.com");
        User user2 = new User("Maria", "Lungu", "maria.lungu@mail.com");

        userDBRepository.save(user1);
        userDBRepository.save(user2);

        friendshipDBRepository.save(new Friendship(user1.getId(), user2.getId()));
        assert !friendshipDBRepository.isEmpty();
        assert friendshipDBRepository.size() == 1;

        friendshipDBRepository.delete(new Tuple<>(user2.getId(), user1.getId()));
        assert friendshipDBRepository.isEmpty();

        Friendship friendship = new Friendship(user1.getId(), user2.getId());
        friendshipDBRepository.save(friendship);

        Friendship friendshipUpdated = new Friendship(user1.getId(), user2.getId());
        friendshipDBRepository.update(friendshipUpdated);

        Optional<Friendship> friendshipRet = friendshipDBRepository.getOne(friendship.getId());
        assert friendshipRet.isEmpty() || friendshipRet.get().equals(friendship);

        System.out.println("FriendshipDBRepository passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }

    public static void runFriendRequestDBRepository() {
        FriendRequestDBRepository friendRequestDBRepository = new FriendRequestDBRepository("jdbc:postgresql://localhost:5432/socialNetworkTests", "postgres", "postgres");
        UserDBRepository userDBRepository = new UserDBRepository("jdbc:postgresql://localhost:5432/socialNetworkTests", "postgres", "postgres");
        clearDBFriendrequest(friendRequestDBRepository);
        clearDBUser(userDBRepository);

        User user1 = new User("Ioan", "Botezatu", "ioan.botezatu@mail.com");
        User user2 = new User("Andrei", "Marian", "andrei.marian@mail.com");

        userDBRepository.save(user1);
        userDBRepository.save(user2);

        assert friendRequestDBRepository.isEmpty();

        FriendRequest friendRequest = new FriendRequest(user1.getId(), user2.getId());
        friendRequestDBRepository.save(friendRequest);

        assert friendRequestDBRepository.size() == 1;

        Optional<FriendRequest> old = friendRequestDBRepository.update(new FriendRequest(friendRequest.getIdFrom(), friendRequest.getIdTo(), "accepted", friendRequest.getDate()));
        assert old.isPresent() && old.get().equals(friendRequest);

        Optional<FriendRequest> found = friendRequestDBRepository.getOne(old.get().getId());
        assert found.isPresent() && found.get().getStatus().equals("accepted");

        Optional<FriendRequest> deleted = friendRequestDBRepository.delete(friendRequest.getId());

        assert deleted.isPresent() && deleted.get().equals(friendRequest);
        assert friendRequestDBRepository.isEmpty();

        System.out.println("FriendshipRequestDBRepository passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }

    public static void runMessageDBRepository() {
        UserDBRepository userDBRepository = new UserDBRepository("jdbc:postgresql://localhost:5432/socialNetworkTests", "postgres", "postgres");
        MessageDBRepository messageDBRepository = new MessageDBRepository("jdbc:postgresql://localhost:5432/socialNetworkTests", "postgres", "postgres");
        clearDBUser(userDBRepository);
        clearDBMessages(messageDBRepository);

        // Creating some users.
        User user1 = new User("Ion", "Micu", "ion.micu@mail.com");
        User user2 = new User("Andrei", "Micu", "andrei.micu@mail.com");
        User user3 = new User("Maria", "Micu", "maria.micu@mail.com");

        // Adding the users.
        userDBRepository.save(user1);
        userDBRepository.save(user2);
        userDBRepository.save(user3);

        // Creating the message.
        Message message = new Message(user1.getId(), Arrays.asList(user2.getId(), user3.getId()), "hello receivers!");

        // Saving the message into the db.
        assert messageDBRepository.isEmpty();
        assert messageDBRepository.save(message).isEmpty();

        // Verifying if the message was actually added.
        assert !messageDBRepository.isEmpty();
        Optional<Message> optionalMessage = messageDBRepository.getOne(message.getId());
        assert optionalMessage.isPresent() && optionalMessage.get().equals(message);

        // Updating the message.
        Message updateMessage = new Message(message.getId(), message.getFrom(), message.getTo(), "hello receivers (update)!", LocalDateTime.now());
        Optional<Message> old = messageDBRepository.update(updateMessage);

        assert old.isPresent() && old.get().equals(message);

        // Finding the messages of a user.
        assert messageDBRepository.getMessagesBetweenUsers(user1.getId(), user2.getId()).size() == 1;

        // Deleting the message.
        Optional<Message> deleted = messageDBRepository.delete(message.getId());
        assert deleted.isPresent() && deleted.get().equals(updateMessage) && messageDBRepository.isEmpty();

        System.out.println("MessageDBRepository passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }
}
