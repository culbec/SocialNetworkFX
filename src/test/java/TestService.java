import ro.ubbcluj.map.socialnetworkfx.entity.*;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.repository.InMemoryRepository;
import ro.ubbcluj.map.socialnetworkfx.service.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TestService {
    public static void run() {
        Service service = getService();

        // new service shouldn't have values in it
        assert (service.getUsers().isEmpty());
        assert (service.getFriendships().isEmpty());

        // adding a user
        try {
            service.addUser("", "", "", "");
            assert false;
        } catch (ServiceException sE) {
            assert true;
        }


        // testing add
        service.addUser("Marius", "Chiriac", "marius.chiriac@mail.com", "");
        assert (!service.getUsers().isEmpty());

        // testing remove
        service.removeUser(service.getUsers().get(0).getId());
        assert (service.getUsers().isEmpty());

        service.addUser("Ion", "Remus", "ion.remus@mail.com", "");

        try {
            service.addUser("Ion", "Remus", "ion.remus@mail.com", "");
            assert false;
        } catch (ServiceException sE) {
            assert true;
        }

        service.addUser("Mariana", "Chiriac", "mariana.chiriac@mail.com", "");

        // adding a friendship
        User user1 = service.getUsers().get(0);
        User user2 = service.getUsers().get(service.getUsers().size() - 1);
        service.addFriendship(user1.getId(), user2.getId());
        assert (!service.getFriendsOf(user1.getId()).isEmpty());
        assert (!service.getFriendships().isEmpty());

        // trying to add the same friendship
        try {
            service.addFriendship(user1.getId(), user2.getId());
            assert false;
        } catch (ServiceException sE) {
            assert true;
        }

        // removing a user which has friendships
        service.removeUser(user1.getId());
        assert (service.getFriendsOf(user2.getId()).isEmpty());

        // trying to remove a user who doesn't exist
        try {
            service.removeUser(user1.getId());
            assert false;
        } catch (ServiceException sE) {
            assert true;
        }

        // trying to remove a friendship that doesn't exist
        try {
            service.removeFriendship(user1.getId(), user2.getId());
            assert false;
        } catch (ServiceException sE) {
            assert true;
        }

        // trying to get a user that doesn't exist
        try {
            service.getUser(user1.getId());
            assert false;
        } catch (ServiceException sE) {
            assert true;
        }
        // verifying if the getter works correctly
        assert (service.getUser(user2.getId()).equals(user2));

        // adding back the user to test removal of friendship
        service.addUser("Marius", "Chiriac", "marius.chiriac@mail.com", "");
        User friend1 = service.getUsers().get(0);
        User friend2 = service.getUsers().get(service.getUsers().size() - 1);
        service.addFriendship(friend1.getId(), friend2.getId());

        Friendship removedFriendship = service.removeFriendship(friend1.getId(), friend2.getId());
        assert (removedFriendship.getId().getLeft().equals(friend1.getId()));
        assert (service.getFriendships().isEmpty());

        assert service.friendsFromMonth(friend1.getId(), "10").isEmpty();

        service.addFriendship(friend1.getId(), friend2.getId());
        assert !service.friendsFromMonth(friend1.getId(), String.valueOf(LocalDateTime.now().getMonth().getValue())).isEmpty();

        // testing updateUser method
        User newUser = new User(friend1.getId(), friend2.getFirstName(), friend2.getLastName(), friend2.getEmail());
        String oldFirstname = friend1.getFirstName();
        User old = service.updateUser(newUser);
        assert old.getFirstName().equals(oldFirstname);

        System.out.println("Service tests passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }

    private static Service getService() {
        InMemoryRepository<UUID, User> userInMemoryRepository = new InMemoryRepository<>();
        InMemoryRepository<Tuple<UUID, UUID>, Friendship> friendshipInMemoryRepository = new InMemoryRepository<>();
        InMemoryRepository<Tuple<Tuple<UUID, UUID>, LocalDateTime>, FriendRequest> friendRequestInMemoryRepository = new InMemoryRepository<>();
        InMemoryRepository<UUID, Message> messageInMemoryRepository = new InMemoryRepository<>();

        return new Service(userInMemoryRepository, friendshipInMemoryRepository, friendRequestInMemoryRepository, messageInMemoryRepository);
    }
}
