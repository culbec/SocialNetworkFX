package ro.ubbcluj.map.socialnetworkfx.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Class that encapsulates a Friendship.
 */
public class Friendship extends Entity<Tuple<UUID, UUID>> {
    // date and time of the friendship
    private final LocalDateTime friendshipDate;

    /**
     * Initializes a Friendship.
     *
     * @param userId1 First user of the friendship.
     * @param userId2 Second user of the friendship.
     */
    public Friendship(UUID userId1, UUID userId2) {
        super(new Tuple<>(userId1, userId2));
        this.friendshipDate = LocalDateTime.now();
    }

    /**
     * Initializes a Friendship.
     *
     * @param userId1        First user of the friendship.
     * @param userId2        Second user of the friendship.
     * @param friendshipDate Date and time of the friendship.
     */

    public Friendship(UUID userId1, UUID userId2, LocalDateTime friendshipDate) {
        super(new Tuple<>(userId1, userId2));
        this.friendshipDate = friendshipDate;
    }

    /**
     * Getter for the friendship date
     *
     * @return The date when the friendship was created
     */
    public LocalDateTime getFriendshipDate() {
        return this.friendshipDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), friendshipDate);
    }

    @Override
    public String toString() {
        return this.id + " " + DateTimeFormatter.ofPattern("dd-MM-yyyy HH:ss").format(this.friendshipDate);
    }
}
