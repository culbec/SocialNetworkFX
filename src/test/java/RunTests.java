import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RunTests {
    public static void main(String[] args) throws RepositoryException {
        TestEntity.run();
        TestRepository.run();
        TestValidator.run();
        TestService.run();
        TestCommunities.run();

        TestDBRepository.runUserDBRepository();
        TestDBRepository.runFriendshipDBRepository();

        System.out.println("All tests passed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));
    }
}
