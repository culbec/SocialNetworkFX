import ro.ubbcluj.map.socialnetworkfx.entity.*;
import ro.ubbcluj.map.socialnetworkfx.exception.ServiceException;
import ro.ubbcluj.map.socialnetworkfx.repository.InMemoryRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.Repository;
import ro.ubbcluj.map.socialnetworkfx.service.ServiceFriendRequest;
import ro.ubbcluj.map.socialnetworkfx.service.ServiceFriendship;
import ro.ubbcluj.map.socialnetworkfx.service.ServiceMessage;
import ro.ubbcluj.map.socialnetworkfx.service.ServiceUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestService {
    private static void Test_ServiceUser_ServiceFriendship() {
        Repository<UUID, User> userRepository = new InMemoryRepository<>();
        Repository<Tuple<UUID, UUID>, Friendship> friendshipRepository = new InMemoryRepository<>();

        ServiceUser serviceUser = new ServiceUser(userRepository);
        ServiceFriendship serviceFriendship = new ServiceFriendship(friendshipRepository);

        // New services -> shouldn't have values in them.
        assert serviceUser.getUsers().isEmpty();
        assert serviceFriendship.getFriendships().isEmpty();

        // USER TESTING

        try {
            serviceUser.addUser("", "", "", "");
            assert false;
        } catch (ServiceException sE) {
            assert true;
        }

        serviceUser.addUser("Marius", "Chiriac", "marius.chiriac@mail.com", "");
        assert (!serviceUser.getUsers().isEmpty());

        serviceUser.removeUser(serviceUser.getUsers().get(0).getId());
        assert (serviceUser.getUsers().isEmpty());

        serviceUser.addUser("Ion", "Remus", "ion.remus@mail.com", "");
        try {
            serviceUser.addUser("Ion", "Remus", "ion.remus@mail.com", "");
            assert false;
        } catch (ServiceException sE) {
            assert true;
        }

        User toUpdate = new User(serviceUser.getUser(serviceUser.getUsers().get(0).getId()).getId(), "Marius", "Andrei", "marius.andrei@mail.com", "");
        assert serviceUser.updateUser(toUpdate).getFirstName().equals("Ion");


        serviceUser.addUser("Mariana", "Chiriac", "mariana.chiriac@mail.com", "");

        try {
            assert serviceUser.getUsers_NameContainsString("r").size() == 2;
        } catch (ServiceException ignored) {
        }

        // TESTING FRIENDSHIPS
        User user1 = serviceUser.getUser(serviceUser.getUsers().get(0).getId());
        User user2 = serviceUser.getUser(serviceUser.getUsers().get(1).getId());
        serviceFriendship.addFriendship(user1.getId(), user2.getId());
        assert !serviceFriendship.getFriendships().isEmpty();

        assert serviceFriendship.getFriendIdsOf(user1.getId()).size() == 1;
        assert serviceUser.getFriends(serviceFriendship.getFriendIdsOf(user1.getId())).size() == 1;

        serviceFriendship.removeFriendshipsOf(user1.getId());
        assert serviceFriendship.getFriendships().isEmpty();
    }

    private static void Test_ServiceFriendRequest() throws InterruptedException {
        Repository<Tuple<Tuple<UUID, UUID>, LocalDateTime>, FriendRequest> friendRequestRepository = new InMemoryRepository<>();
        Repository<Tuple<UUID, UUID>, Friendship> friendshipRepository = new InMemoryRepository<>();

        ServiceFriendship serviceFriendship = new ServiceFriendship(friendshipRepository);
        ServiceFriendRequest serviceFriendRequest = new ServiceFriendRequest(friendRequestRepository);

        // Adding the friendship service as an observer of the friend request service.
        serviceFriendRequest.addObserver(serviceFriendship);

        User user1 = new User("Ion", "Popescu", "ion.popescu@mail.com", "");
        User user2 = new User("Andrei", "Ionescu", "andrei.ionescu@mail.com", "");

        serviceFriendRequest.sendFriendRequest(user1, user2);
        List<FriendRequest> friendRequests = serviceFriendRequest.getFriendRequestsOfUser(user2.getId());
        assert serviceFriendRequest.getFriendRequestsOfUser(user2.getId()).size() == 1;

        serviceFriendRequest.rejectFriendRequest(serviceFriendRequest.getFriendRequestsOfUser(user2.getId()).get(0));
        assert serviceFriendRequest.getFriendRequestsOfUser(user2.getId()).isEmpty();

        Thread.sleep(100); // To prevent date truncation to result in the same date of sending.

        serviceFriendRequest.sendFriendRequest(user1, user2);
        serviceFriendRequest.acceptFriendRequest(serviceFriendRequest.getFriendRequestsOfUser(user2.getId()).get(0));
        assert serviceFriendship.getFriendships().size() == 1;
    }

    private static void Test_ServiceMessage() {
        Repository<UUID, Message> messageRepository = new InMemoryRepository<>();
        ServiceMessage serviceMessage = new ServiceMessage(messageRepository);

        User user1 = new User("Ion", "Popescu", "ion.popescu@mail.com", "");
        User user2 = new User("Andrei", "Ionescu", "andrei.ionescu@mail.com", "");

        Message message = new Message(user1.getId(), Collections.singletonList(user2.getId()), "test");
        serviceMessage.sendMessage(message);

        assert serviceMessage.getMessagesBetweenUsers(user1, user2).size() == 1;
    }


    public static void run() throws InterruptedException {
        Test_ServiceUser_ServiceFriendship();
        Test_ServiceFriendRequest();
        Test_ServiceMessage();
        System.out.println("Service tests passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }

}
