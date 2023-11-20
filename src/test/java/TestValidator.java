import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.ValidatorException;
import ro.ubbcluj.map.socialnetworkfx.validator.FriendshipValidator;
import ro.ubbcluj.map.socialnetworkfx.validator.UserValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestValidator {
    public static void run() {
        UserValidator userValidator = new UserValidator();
        FriendshipValidator friendshipValidator = new FriendshipValidator();

        User allNullUser = new User(null, null, null);
        User firstNameNullUser = new User(null, "notnull", "notnull");
        User lastNameNullUser = new User("notnull", null, "notnull");
        User emailNullUser = new User("notnull", "notnull", null);

        User allEmptyUser = new User("", "", "");
        User firstNameEmptyUser = new User("", "notempty", "notempty");
        User lastNameEmptyUser = new User("notempty", "", "notempty");
        User emailEmptyUser = new User("notempty", "notempty", "");


        // Quick validators
        try {
            userValidator.validate(allNullUser);
            assert (false);
        } catch (ValidatorException vE) {
            assert true;
        }
        try {
            userValidator.validate(firstNameNullUser);
            assert (false);
        } catch (ValidatorException vE) {
            assert true;
        }
        try {
            userValidator.validate(lastNameNullUser);
            assert (false);
        } catch (ValidatorException vE) {
            assert true;
        }
        try {
            userValidator.validate(emailNullUser);
            assert (false);
        } catch (ValidatorException vE) {
            assert true;
        }
        try {
            userValidator.validate(allEmptyUser);
            assert (false);
        } catch (ValidatorException vE) {
            assert true;
        }
        try {
            userValidator.validate(firstNameEmptyUser);
            assert (false);
        } catch (ValidatorException vE) {
            assert true;
        }
        try {
            userValidator.validate(lastNameEmptyUser);
            assert (false);
        } catch (ValidatorException vE) {
            assert true;
        }
        try {
            userValidator.validate(emailEmptyUser);
            assert (false);
        } catch (ValidatorException vE) {
            assert true;
        }

        User validUser = new User("Marian", "Vasile", "mv@mail.com");
        try {
            userValidator.validate(validUser);
            assert true;
        } catch (ValidatorException vE) {
            assert false;
        }

        Friendship nullFriendship = new Friendship(null, null);
        Friendship nullLeftFriendship = new Friendship(null, validUser.getId());
        Friendship nullRightFriendship = new Friendship(validUser.getId(), null);
        Friendship sameFriendship = new Friendship(validUser.getId(), validUser.getId());

        try {
            friendshipValidator.validate(nullFriendship);
            assert false;
        } catch (ValidatorException vE) {
            assert true;
        }

        try {
            friendshipValidator.validate(nullLeftFriendship);
            assert false;
        } catch (ValidatorException vE) {
            assert true;
        }

        try {
            friendshipValidator.validate(nullRightFriendship);
            assert false;
        } catch (ValidatorException vE) {
            assert true;
        }

        try {
            friendshipValidator.validate(sameFriendship);
            assert false;
        } catch (ValidatorException vE) {
            assert true;
        }

        System.out.println("Validator tests completed at: " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now()));

    }
}
