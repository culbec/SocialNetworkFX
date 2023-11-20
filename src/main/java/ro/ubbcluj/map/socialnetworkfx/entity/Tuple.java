package ro.ubbcluj.map.socialnetworkfx.entity;

import java.util.Objects;

/**
 * Pair of elements.
 *
 * @param <E1> First entity of the pair.
 * @param <E2> Second entity of the pair.
 */
public class Tuple<E1, E2> {
    private final E1 left;
    private final E2 right;

    public Tuple(E1 e1, E2 e2) {
        this.left = e1;
        this.right = e2;
    }

    public E1 getLeft() {
        return left;
    }

    public E2 getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(left, tuple.left) && Objects.equals(right, tuple.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return this.left + " " + this.right;
    }
}
