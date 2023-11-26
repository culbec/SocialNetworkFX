import ro.ubbcluj.map.socialnetworkfx.entity.FriendRequest;
import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendRequestDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.FriendshipDBRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.UserDBRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public static void runFriendrequestDBRepository() {
        FriendRequestDBRepository friendRequestDBRepository = new FriendRequestDBRepository("jdbc:postgresql://localhost:5432/socialNetworkTests", "postgres", "postgres");
        UserDBRepository userDBRepository = new UserDBRepository("jdbc:postgresql://localhost:5432/socialNetworkTests", "postgres", "postgres");
        clearDBFriendrequest(friendRequestDBRepository);
        clearDBUser(userDBRepository);

        User user1 = new User("Ioan", "Botezatu", "ioan.botezatu@mail.com");
        User user2 = new User("Andrei", "Marian", "andrei.marian@mail.com");

        userDBRepository.save(user1);
        userDBRepository.save(user2);

        assert friendRequestDBRepository.isEmpty();

        FriendRequest friendRequest = new FriendRequest(user1.getId(), user2.getId(), "pending");
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
}
