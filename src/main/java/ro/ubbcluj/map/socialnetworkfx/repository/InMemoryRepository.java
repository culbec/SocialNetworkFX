package ro.ubbcluj.map.socialnetworkfx.repository;

import ro.ubbcluj.map.socialnetworkfx.entity.Entity;
import ro.ubbcluj.map.socialnetworkfx.exception.RepositoryException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Repository that stores its values in memory.
 *
 * @param <ID> ID type of the stored Entity.
 * @param <E>  Entity to be stored.
 */
public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    // Data structure that stores the entities
    private final Map<ID, E> entities;

    public InMemoryRepository() {
        this.entities = new HashMap<>();
    }

    @Override
    public boolean isEmpty() {
        return this.entities.isEmpty();
    }

    @Override
    public int size() {
        return this.entities.values().size();
    }

    @Override
    public Iterable<E> getAll() {
        return this.entities.values();
    }


    @Override
    public Optional<E> getOne(ID id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("The id cannot be null!");
        }
        return Optional.ofNullable(this.entities.get(id)); // is null if the entity doesn't exist
    }

    @Override
    public Optional<E> save(E e) throws RepositoryException, IllegalArgumentException {
        if (e == null) {
            throw new IllegalArgumentException("Entity cannot be null!");
        }
        if (this.entities.containsValue(e)) {
            throw new RepositoryException("The same entity is already stored!");
        }
        return Optional.ofNullable(this.entities.putIfAbsent(e.getId(), e));
    }


    @Override
    public Optional<E> delete(ID id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null!");
        }

        return Optional.ofNullable(this.entities.remove(id));
    }

    @Override
    public Optional<E> update(E e) throws RepositoryException, IllegalArgumentException {
        if (e == null) {
            throw new IllegalArgumentException("Id cannot be null!");
        }
        if (this.entities.get(e.getId()) == null) {
            throw new RepositoryException("Entity with the specified id doesn't exist!");
        }
        return Optional.ofNullable(this.entities.put(e.getId(), e));
    }
}
