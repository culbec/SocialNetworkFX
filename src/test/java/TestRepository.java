import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;
import ro.ubbcluj.map.socialnetworkfx.repository.InMemoryRepository;
import ro.ubbcluj.map.socialnetworkfx.repository.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class TestRepository {
    public static void run() throws RepositoryException {
        Repository<UUID, User> userRepository = new InMemoryRepository<>();

        User user1 = new User("Laurentiu", "Muresan", "laurentiu.muresan@mail.com");
        User user2 = new User("Marian", "Chiriac", "marian.chiriac@mail.com");

        assert (userRepository.isEmpty());

        try {
            userRepository.getOne(null);
            assert false;
        } catch (IllegalArgumentException exception) {
            assert true;
        }

        assert (userRepository.getOne(user1.getId()).isEmpty());

        try {
            userRepository.save(null);
            assert false;
        } catch (IllegalArgumentException exception) {
            assert true;
        }
        assert (userRepository.save(user1).isEmpty());
        try {
            userRepository.save(user1);
            assert false;
        } catch (RepositoryException rE) {
            assert true;
        }

        assert (!userRepository.isEmpty());
        assert (userRepository.size() == 1);

        assert (userRepository.getOne(user1.getId()).get().equals(user1));

        try {
            userRepository.delete(null);
            assert false;
        } catch (IllegalArgumentException exception) {
            assert true;
        }

        assert (userRepository.delete(user2.getId()).isEmpty());

        Optional<User> deleted = userRepository.delete(user1.getId());
        assert (deleted.isPresent() && deleted.get().equals(user1));
        userRepository.save(user1);

        try {
            userRepository.update(null);
            assert false;
        } catch (IllegalArgumentException exception) {
            assert true;
        }

        try {
            userRepository.update(user2);
            assert false;
        } catch (RepositoryException rE) {
            assert true;
        }

        user2.setId(user1.getId());
        Optional<User> updated = userRepository.update(user2);
        assert (updated.isPresent() && updated.get().equals(user1));

        System.out.println("Repository tests passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }
}
