package ro.ubbcluj.map.socialnetworkfx.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class FriendRequest extends Entity<Tuple<Tuple<UUID, UUID>, LocalDateTime>> {
    // Date when the friend request was sent.
    private final LocalDateTime date;
    // The one who requests.
    private final UUID idFrom;
    // The one requested.
    private final UUID idTo;
    // One of: pending, accepted, rejected.
    private final String status;

    public FriendRequest(UUID idFrom, UUID idTo) {
        super(new Tuple<>(new Tuple<>(idFrom, idTo), LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));
        this.idFrom = idFrom;
        this.idTo = idTo;
        this.status = "pending";
        date = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    public FriendRequest(UUID idFrom, UUID idTo, String status, LocalDateTime date) {
        super(new Tuple<>(new Tuple<>(idFrom, idTo), date));
        this.idFrom = idFrom;
        this.idTo = idTo;
        this.status = status;
        this.date = date;
    }

    public UUID getIdFrom() {
        return idFrom;
    }

    public UUID getIdTo() {
        return idTo;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

}
