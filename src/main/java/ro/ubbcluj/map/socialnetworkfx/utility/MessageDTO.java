package ro.ubbcluj.map.socialnetworkfx.utility;

import ro.ubbcluj.map.socialnetworkfx.entity.Message;
import ro.ubbcluj.map.socialnetworkfx.entity.ReplyMessage;
import ro.ubbcluj.map.socialnetworkfx.entity.User;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class MessageDTO {
    private User user;
    private User replyUser;
    private final Message message;
    private final Message repliedMessage;

    public MessageDTO(User user, Message message) {
        this.user = user;
        this.replyUser = null;
        this.message = message;
        this.repliedMessage = null;
    }

    public MessageDTO(User user, User replyUser, Message message, Message repliedMessage) {
        this.user = user;
        this.replyUser = replyUser;
        this.message = message;
        this.repliedMessage = repliedMessage;
    }

    public User getUser() {
        return user;
    }

    public User getReplyUser() {
        return replyUser;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setReplyUser(User replyUser) {
        this.replyUser = replyUser;
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
        // Normal message.
        if (this.message.getClass().equals(Message.class)) {
            return this.user + " [" + this.message.getDate().truncatedTo(ChronoUnit.SECONDS) + "]: " + this.message.getMessageText();
        }

        // Reply Message otherwise.
        ReplyMessage replyMessage = (ReplyMessage) this.message;
        return "Replied to {" + this.replyUser + " [" + replyMessage.getDate().truncatedTo(ChronoUnit.SECONDS) + "] " + this.repliedMessage.getMessageText() + "}\n" + this.user + " [" + this.message.getDate().truncatedTo(ChronoUnit.SECONDS) + "]: " + this.message.getMessageText();

    }
}
