package ro.ubbcluj.map.socialnetworkfx.utility;

import ro.ubbcluj.map.socialnetworkfx.entity.Message;
import ro.ubbcluj.map.socialnetworkfx.entity.User;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class MessageDTO {
    private User user;
    private final Message message;

    public MessageDTO(User user, Message message) {
        this.user = user;
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageDTO that = (MessageDTO) o;
        return Objects.equals(user, that.user) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, message);
    }

    @Override
    public String toString() {
        return this.user + " [" + this.message.getDate().truncatedTo(ChronoUnit.SECONDS) + "]: " + this.message.getMessageText();
    }
}
