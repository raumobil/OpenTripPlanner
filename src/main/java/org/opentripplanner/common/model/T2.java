package org.opentripplanner.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * An ordered pair of objects of potentially different types
 */
public class T2<E1, E2> implements Serializable {
    private static final long serialVersionUID = 1L;

    public final E1 first;

    public final E2 second;

    public T2(E1 first, E2 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        return (first != null ? first.hashCode() : 0) + (second != null ? second.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof T2)) { return false; }
        var other = (T2<?,?>) object;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public String toString() {
        return "T2(" + first + ", " + second + ")";
    }
}
