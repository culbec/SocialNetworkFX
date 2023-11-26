import ro.ubbcluj.map.socialnetworkfx.entity.FriendRequest;
import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.repository.InMemoryRepository;
import ro.ubbcluj.map.socialnetworkfx.service.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestCommunities {
    public static void run() {
        InMemoryRepository<UUID, User> userInMemoryRepository = new InMemoryRepository<>();
        InMemoryRepository<Tuple<UUID, UUID>, Friendship> friendshipInMemoryRepository = new InMemoryRepository<>();
        InMemoryRepository<Tuple<Tuple<UUID, UUID>, LocalDateTime>, FriendRequest> friendRequestInMemoryRepository = new InMemoryRepository<>();
        Service service = new Service(userInMemoryRepository, friendshipInMemoryRepository, friendRequestInMemoryRepository);

        service.addUser("Ion", "Remus", "ion.remus@mail.com");
        service.addUser("Marius", "Chiriac", "marius.chiriac@mail.com");
        service.addUser("Vlad", "Remus", "vlad.remus@mail.com");
        service.addUser("Florin", "Remus", "florin.remus@mail.com");
        service.addUser("Cosmin", "Popovici", "cosmin.popovici@mail.com");
        service.addUser("Laura", "Matei", "laura.matei@mail.com");
        service.addUser("Ionut", "Andrei", "ionut.andrei@mail.com");

        ArrayList<User> users = service.getUsers();

        // making friends
        service.addFriendship(users.get(0).getId(), users.get(2).getId());
        service.addFriendship(users.get(0).getId(), users.get(3).getId());
        service.addFriendship(users.get(1).getId(), users.get(2).getId());
        service.addFriendship(users.get(1).getId(), users.get(3).getId());
        service.addFriendship(users.get(1).getId(), users.get(4).getId());

        service.addFriendship(users.get(5).getId(), users.get(6).getId());

        Tuple<Integer, List<List<UUID>>> integerListTuple = service.communities();
        assert (integerListTuple.getLeft() == 2 && integerListTuple.getRight().get(0).size() == 5);

        System.out.println("Communities tests passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }
}
