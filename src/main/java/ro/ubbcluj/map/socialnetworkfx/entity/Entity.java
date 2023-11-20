package ro.ubbcluj.map.socialnetworkfx.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Generic class identified by an ID
 *
 * @param <ID> Unique identification token for the Entity
 */
public class Entity<ID> implements Serializable {
    protected ID id;

    /**
     * Initializes an Entity
     *
     * @param id The ID of the Entity
     */
    protected Entity(ID id) {
        this.id = id;
    }

    /**
     * Getter for the ID of the Entity
     *
     * @return Entity's ID
     */
    public ID getId() {
        return this.id;
    }

    /**
     * Setter for the Entity's ID
     *
     * @param id New ID of the Entity
     */
    public void setId(ID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
