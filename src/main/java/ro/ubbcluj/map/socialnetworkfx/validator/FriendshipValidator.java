package ro.ubbcluj.map.socialnetworkfx.validator;

import ro.ubbcluj.map.socialnetworkfx.entity.Friendship;
import ro.ubbcluj.map.socialnetworkfx.entity.Tuple;
import ro.ubbcluj.map.socialnetworkfx.exception.ValidatorException;

import java.util.UUID;

public class FriendshipValidator extends AbstractValidator<Tuple<UUID, UUID>, Friendship> {
    @Override
    public void validate(Friendship friendship) throws ValidatorException {
        if (friendship == null) {
            throw new ValidatorException("Friendship cannot be null!");
        } else if (friendship.getId().getLeft() == null || friendship.getId().getRight() == null) {
            throw new ValidatorException("Friendship contents cannot be null!");
        } else if (friendship.getId().getLeft().equals(friendship.getId().getRight())) {
            throw new ValidatorException("Friendship cannot be made between the same user!");
        }
    }

}
