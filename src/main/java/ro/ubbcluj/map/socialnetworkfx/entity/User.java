package ro.ubbcluj.map.socialnetworkfx.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * Class that encapsulates a User.
 */
public class User extends Entity<UUID> {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public User(String firstName, String lastName, String email) {
        super(UUID.randomUUID());
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = null;
    }

    public User(String firstName, String lastName, String email, String password) {
        super(UUID.randomUUID());
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public User(UUID uuid, String firstName, String lastName, String email) {
        super(uuid);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = null;
    }

    public User(UUID uuid, String firstName, String lastName, String email, String password) {
        super(uuid);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    /**
     * Getter for the first name of the User
     *
     * @return First name of the User
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Setter for the first name of the User
     *
     * @param firstName New first name of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Getter for the last name of the User
     *
     * @return Last name of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Setter for the last name of the User
     *
     * @param lastName New last name of the user
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Getter for the email of a user.
     *
     * @return Email of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter for the email of the user.
     *
     * @param email New email of the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter for the password of the user.
     *
     * @return Password of the user.
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName)
                && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), firstName, lastName, email);
    }

    @Override
    public String toString() {
        return this.firstName + " " + this.lastName + " " + this.email;
    }
}
