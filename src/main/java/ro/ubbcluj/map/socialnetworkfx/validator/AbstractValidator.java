package ro.ubbcluj.map.socialnetworkfx.validator;

import ro.ubbcluj.map.socialnetworkfx.entity.Entity;
import ro.ubbcluj.map.socialnetworkfx.exception.ValidatorException;

/**
 * Validator interface for implementing the rest of the validators.
 */
public abstract class AbstractValidator<ID, E extends Entity<ID>> {

    /**
     * Validates an entity of a specific type.
     */
    public abstract void validate(E e) throws ValidatorException;
}

