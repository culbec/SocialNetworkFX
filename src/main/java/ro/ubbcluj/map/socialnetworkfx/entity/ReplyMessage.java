package ro.ubbcluj.map.socialnetworkfx.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReplyMessage extends Message {
    // The message that the reply responds to.
    private final UUID messageId;

    public ReplyMessage(UUID from, List<UUID> to, String messageText, UUID messageId) {
        super(from, to, messageText);
        this.messageId = messageId;
    }

    public ReplyMessage(UUID id, UUID from, List<UUID> to, String messageText, LocalDateTime date, UUID messageId) {
        super(id, from, to, messageText, date);
        this.messageId = messageId;
    }

    public UUID getMessage() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReplyMessage that = (ReplyMessage) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), messageId);
    }

    @Override
    public String toString() {
        return "Reply to " + "{" + this.getMessage() + "}" + super.toString();
    }
}
