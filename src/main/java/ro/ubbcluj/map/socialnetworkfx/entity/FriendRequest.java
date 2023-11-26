package ro.ubbcluj.map.socialnetworkfx.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public class FriendRequest extends Entity<Tuple<Tuple<UUID, UUID>, LocalDateTime>> {
    // The one who requests.
    private UUID idFrom;
    // The one requested.
    private UUID idTo;

    // One of: pending, accepted, rejected.
    private String status;

    // Date when the friend request was sent.
    LocalDateTime date;

    public FriendRequest(UUID idFrom, UUID idTo, String status) {
        super(new Tuple<>(new Tuple<>(idFrom, idTo), LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));
        this.idFrom = idFrom;
        this.idTo = idTo;
        this.status = status;
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
