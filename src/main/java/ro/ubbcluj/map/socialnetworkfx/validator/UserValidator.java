package ro.ubbcluj.map.socialnetworkfx.validator;

import ro.ubbcluj.map.socialnetworkfx.entity.User;
import ro.ubbcluj.map.socialnetworkfx.exception.ValidatorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UserValidator extends AbstractValidator<UUID, User> {
    /**
     * Validates the first name of a user.
     * The first name should start with a capital letter, have at least to letters, and contain only lower case letters
     * for all the letters except the first.
     *
     * @param firstName The first name.
     * @return String of errors.
     */
    private String validateFirstName(String firstName) {
        ArrayList<String> errors = new ArrayList<>();
        // Checking if the first name contains only letters and the first letter is uppercase.
        if (firstName == null) {
            errors.add("First name cannot be null.");
        } else if (firstName.isEmpty()) {
            errors.add("First name cannot be empty.");
        } else if (firstName.length() < 3) {
            errors.add("First name should at least contain 2 characters.");
        } else if (!Character.isUpperCase(firstName.charAt(0))) {
            errors.add("First name needs to start with uppercase letter.");
        } else {
            String restOfFirstName = firstName.substring(1);
            for (Character character : restOfFirstName.toCharArray()) {
                if (character.compareTo('a') < 0 || character.compareTo('z') > 0) {
                    errors.add("First name should contain only letters.");
                    break;
                }
            }
        }
        return errors
                .stream()
                .reduce("", (str1, str2) -> str1.concat(" ").concat(str2));
    }

    /**
     * Validates the last name of a user.
     * The last name should start with a capital letter, have at least to letters, and contain only lower case letters
     * for all the letters except the first.
     *
     * @param lastName The last name.
     * @return String of errors.
     */
    private String validateLastName(String lastName) {
        ArrayList<String> errors = new ArrayList<>();

        // Checking if the last name starts with an uppercase letter and it contains only letters

        if (lastName == null) {
            errors.add("Last name cannot be null.");
        } else if (lastName.isEmpty()) {
            errors.add("Last name cannot be empty.");
        } else if (lastName.length() < 3) {
            errors.add("Last name should at least contain 2 characters.");
        } else if (!Character.isUpperCase(lastName.charAt(0))) {
            errors.add("Last name needs to start with uppercase letter.");
        } else {
            String restOfLastName = lastName.substring(1);
            for (Character character : restOfLastName.toCharArray()) {
                if (character.compareTo('a') < 0 || character.compareTo('z') > 0) {
                    errors.add("Last name should contain only letters.");
                    break;
                }
            }
        }
        return errors
                .stream()
                .reduce("", (str1, str2) -> str1.concat(" ").concat(str2));
    }

    /**
     * Validates the email of a user.
     *
     * @param email The email.
     * @return String of errors.
     */
    private String validateEmail(String email) {
        ArrayList<String> errors = new ArrayList<>();

        // Checking if the email is valid.
        // Split by '@' and has a domain.
        if (email == null) {
            errors.add("Email cannot be null.");
        } else if (email.isEmpty()) {
            errors.add("Email cannot be empty.");
        } else {
            List<String> emailContents = Arrays.asList(email.split("@"));

            if (emailContents.size() != 2) {
                errors.add("Email format not valid.");
            } else {
                if (emailContents.get(1).split("\\.").length == 1) {
                    errors.add("Email domain format not valid.");
                }
            }
        }

        return errors
                .stream()
                .reduce("", (str1, str2) -> str1.concat(" ").concat(str2));
    }

    /**
     * Validates an entity of type User.
     */
    @Override
    public void validate(User user) throws ValidatorException {
        if (user == null) {
            throw new ValidatorException("User cannot be null!");
        }
        ArrayList<String> errors = new ArrayList<>();
        errors.add(this.validateFirstName(user.getFirstName()));
        errors.add(this.validateLastName(user.getLastName()));
        errors.add(this.validateEmail(user.getEmail()));

        String result = errors
                .stream()
                .reduce("", (str1, str2) -> str1.concat("").concat(str2));

        if (!result.isEmpty()) {
            throw new ValidatorException(result);
        }
    }
}
