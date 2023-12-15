import ro.ubbcluj.map.socialnetworkfx.entity.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class TestEntity {
    public static void run() {
        // Testing User creation
        User user1 = new User("Marian", "Chiriac", "marian.chiriac@mail.com");
        User user2 = new User("Marinela", "Smith", "marinela.smith@mail.com");

        // Testing user-specific methods
        assert (UUID.fromString(user1.getId().toString()).toString().equals(user1.getId().toString()));
        assert (user1.getFirstName().equals("Marian"));
        assert (user1.getLastName().equals("Chiriac"));
        assert (user1.getEmail().equals("marian.chiriac@mail.com"));

        // Testing the setters of the user
        UUID oldId = user1.getId();
        user1.setId(user2.getId());
        assert (user1.getId() == user2.getId());
        user1.setId(oldId);

        user1.setFirstName("Laurentiu");
        user1.setLastName("Muresan");
        user1.setEmail("laurentiu.muresan@mail.com");
        assert (user1.getFirstName().equals("Laurentiu") && user1.getLastName().equals("Muresan") && user1.getEmail().equals("laurentiu.muresan@mail.com"));

        // Testing equals on User
        User user3 = new User("Laurentiu", "Muresan", "laurentiu.muresan@mail.com");
        user3.setId(user1.getId());
        assert (!user1.equals(user2) && user1.hashCode() != user2.hashCode());
        assert (user1.equals(user3));

        // Testing friendship
        Friendship friendship = new Friendship(user1.getId(), user2.getId());
        assert (friendship.getFriendshipDate().getHour() == LocalDateTime.now().getHour());
        assert (friendship.getId().getLeft() == user1.getId());
        assert (friendship.getId().getRight() == user2.getId());

        Friendship newFriendship = new Friendship(user2.getId(), user1.getId());
        newFriendship.setId(friendship.getId());
        assert (friendship.equals(newFriendship));
        assert (!(friendship.hashCode() == newFriendship.hashCode()));

        // Testing Friend Request.
        FriendRequest friendRequest = new FriendRequest(user1.getId(), user2.getId());
        assert friendRequest.getIdFrom().equals(user1.getId());
        assert friendRequest.getIdTo().equals(user2.getId());
        assert friendRequest.getStatus().equals("pending");
        assert friendRequest.getDate().getHour() == LocalDateTime.now().getHour();
        assert friendRequest.getId().equals(new Tuple<>(new Tuple<>(user1.getId(), user2.getId()), friendRequest.getDate()));

        // Testing Message.
        Message message = new Message(user1.getId(), Arrays.asList(user2.getId(), user3.getId()), "hello receivers!");
        assert message.getFrom().equals(user1.getId());
        assert message.getTo().size() == 2;
        assert message.getMessageText().equals("hello receivers!");
        assert message.getDate().getHour() == LocalDateTime.now().getHour();

        // Testing Reply message.
        ReplyMessage replyMessage = new ReplyMessage(user2.getId(), Collections.singletonList(user1.getId()), "hello sender!", message.getId());
        assert replyMessage.getFrom().equals(user2.getId());
        assert replyMessage.getTo().get(0).equals(user1.getId());
        assert replyMessage.getMessageText().equals("hello sender!");
        assert replyMessage.getMessage().equals(message.getId());
        assert replyMessage.getDate().getHour() == LocalDateTime.now().getHour();

        System.out.println("Entity tests passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }
}
