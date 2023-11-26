package ro.ubbcluj.map.socialnetworkfx.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Message extends Entity<UUID> {
    private User from;
    private List<User> to;
    private String message;
    private LocalDateTime date;

    public Message(User from, List<User> to, String message, LocalDateTime date) {
        super(UUID.randomUUID());
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = date;
    }

    public User getFrom() {
        return from;
    }



    public List<User> getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDate() {
        return date;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Message message1 = (Message) o;
        return Objects.equals(from, message1.from) && Objects.equals(to, message1.to) && Objects.equals(message, message1.message) && Objects.equals(date, message1.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), from, to, message, date);
    }

    @Override
    public String toString() {
        return "Message{" +
                "from=" + from +
                ", to=" + to +
                ", message='" + message + '\'' +
                ", date=" + date +
                '}';
    }
}
