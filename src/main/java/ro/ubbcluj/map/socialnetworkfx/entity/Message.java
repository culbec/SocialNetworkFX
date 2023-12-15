package ro.ubbcluj.map.socialnetworkfx.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Message extends Entity<UUID> {
    // Sender of the message.
    private final UUID from;
    // Receivers of the message
    private final List<UUID> to;
    // Text of the message.
    private final String messageText;
    // Date of sending the message.
    private final LocalDateTime date;

    public Message(UUID from, List<UUID> to, String messageText) {
        super(UUID.randomUUID());
        this.from = from;
        this.to = to;
        this.messageText = messageText;
        this.date = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    public Message(UUID id, UUID from, List<UUID> to, String messageText, LocalDateTime date) {
        super(id);
        this.from = from;
        this.to = to;
        this.messageText = messageText;
        this.date = date.truncatedTo(ChronoUnit.MILLIS);
    }

    public UUID getFrom() {
        return from;
    }

    public List<UUID> getTo() {
        return to;
    }

    public String getMessageText() {
        return messageText;
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
        return Objects.equals(from, message1.from) && Objects.equals(to, message1.to) && Objects.equals(messageText, message1.messageText) && Objects.equals(date, message1.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), from, to, messageText, date);
    }

    @Override
    public String toString() {
        return "Message{" +
                "from=" + from +
                ", to=" + to +
                ", message='" + messageText + '\'' +
                ", date=" + date +
                '}';
    }
}
