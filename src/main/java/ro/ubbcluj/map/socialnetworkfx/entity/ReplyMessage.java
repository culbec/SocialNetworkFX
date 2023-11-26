package ro.ubbcluj.map.socialnetworkfx.entity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

public class ReplyMessage extends Message {
    private Message message;

    public ReplyMessage(User from, String messageText, LocalDateTime date, Message message) {
        super(from, Collections.singletonList(message.getFrom()), messageText, date);

    }
}
