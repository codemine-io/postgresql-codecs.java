package io.codemine.java.postgresql.codecs;

import java.util.Objects;

/**
 * PostgreSQL range type. Represents a range of values of type {@code A} in the canonical {@code
 * [lower, upper)} form (inclusive lower, exclusive upper).
 *
 * <p>A range can be:
 *
 * <ul>
 *   <li><b>Empty</b> — no values are in the range
 *   <li><b>Bounded</b> — with an optional lower and/or upper bound. A {@code null} lower bound
 *       means negative infinity; a {@code null} upper bound means positive infinity.
 * </ul>
 *
 * <p>The standard PostgreSQL range types are:
 *
 * <ul>
 *   <li>{@code int4range} — {@code Range<Integer>}
 *   <li>{@code int8range} — {@code Range<Long>}
 *   <li>{@code numrange} — {@code Range<java.math.BigDecimal>}
 *   <li>{@code tsrange} — {@code Range<java.time.LocalDateTime>}
 *   <li>{@code tstzrange} — {@code Range<java.time.Instant>}
 *   <li>{@code daterange} — {@code Range<java.time.LocalDate>}
 * </ul>
 *
 * @param <A> the element type of the range
 */
public sealed interface Range<A> {

  /** An empty range containing no values. */
  record Empty<A>() implements Range<A> {

    @Override
    public String toString() {
      return "empty";
    }
  }

  /**
   * A bounded range. After PostgreSQL normalization, the lower bound is inclusive and the upper
   * bound is exclusive.
   *
   * @param lower the inclusive lower bound, or {@code null} for negative infinity
   * @param upper the exclusive upper bound, or {@code null} for positive infinity
   */
  record Bounded<A>(A lower, A upper) implements Range<A> {

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Bounded<?> other)) {
        return false;
      }
      return Objects.equals(lower, other.lower) && Objects.equals(upper, other.upper);
    }

    @Override
    public int hashCode() {
      return Objects.hash(lower, upper);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (lower == null) {
        sb.append('(');
      } else {
        sb.append('[').append(lower);
      }
      sb.append(',');
      if (upper == null) {
        sb.append(')');
      } else {
        sb.append(upper).append(')');
      }
      return sb.toString();
    }
  }

  /** Creates an empty range. */
  static <A> Range<A> empty() {
    return new Empty<>();
  }

  /** Creates a bounded range with the given lower and upper bounds. */
  static <A> Range<A> bounded(A lower, A upper) {
    return new Bounded<>(lower, upper);
  }

  /** Creates an unbounded range (from negative infinity to positive infinity). */
  static <A> Range<A> unbounded() {
    return new Bounded<>(null, null);
  }
}
