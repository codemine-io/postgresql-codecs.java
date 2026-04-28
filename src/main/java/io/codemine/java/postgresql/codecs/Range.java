package io.codemine.java.postgresql.codecs;

/**
 * PostgreSQL range type. Represents a range of values of type {@code A} with full bound inclusivity
 * information.
 *
 * <p>A range can be:
 *
 * <ul>
 *   <li><b>Empty</b> — no values are in the range
 *   <li><b>Bounded</b> — with an optional lower and/or upper bound. A {@code null} lower bound
 *       means negative infinity; a {@code null} upper bound means positive infinity. Infinite
 *       bounds are always exclusive regardless of the inclusivity flag.
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
   * A bounded range with explicit bound inclusivity.
   *
   * <p>Infinite bounds ({@code null}) are always exclusive: if {@code lower} is {@code null} then
   * {@code lowerInclusive} is normalised to {@code false}, and likewise for {@code upper}.
   *
   * @param lower the lower bound value, or {@code null} for negative infinity
   * @param lowerInclusive {@code true} if the lower bound is inclusive ({@code [}); ignored when
   *     {@code lower} is {@code null}
   * @param upper the upper bound value, or {@code null} for positive infinity
   * @param upperInclusive {@code true} if the upper bound is inclusive ({@code ]}); ignored when
   *     {@code upper} is {@code null}
   */
  record Bounded<A>(A lower, boolean lowerInclusive, A upper, boolean upperInclusive)
      implements Range<A> {

    public Bounded {
      // Infinite bounds are always exclusive — normalise to keep the record canonical.
      if (lower == null) {
        lowerInclusive = false;
      }
      if (upper == null) {
        upperInclusive = false;
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (lower == null) {
        sb.append('(');
      } else {
        sb.append(lowerInclusive ? '[' : '(').append(lower);
      }
      sb.append(',');
      if (upper == null) {
        sb.append(')');
      } else {
        sb.append(upper).append(upperInclusive ? ']' : ')');
      }
      return sb.toString();
    }
  }

  /** Creates an empty range. */
  static <A> Range<A> empty() {
    return new Empty<>();
  }

  /**
   * Creates a range with the given bounds and their inclusivity.
   *
   * @param lower lower bound, or {@code null} for negative infinity (always exclusive)
   * @param lowerInclusive whether the lower bound is inclusive
   * @param upper upper bound, or {@code null} for positive infinity (always exclusive)
   * @param upperInclusive whether the upper bound is inclusive
   */
  static <A> Range<A> of(A lower, boolean lowerInclusive, A upper, boolean upperInclusive) {
    return new Bounded<>(lower, lowerInclusive, upper, upperInclusive);
  }

  /**
   * Creates a half-open range {@code [lower, upper)} — inclusive lower, exclusive upper. This is
   * the canonical form for discrete range types ({@code int4range}, {@code int8range}, {@code
   * daterange}).
   */
  static <A> Range<A> bounded(A lower, A upper) {
    return new Bounded<>(lower, true, upper, false);
  }

  /** Creates an unbounded range {@code (,)} — from negative infinity to positive infinity. */
  static <A> Range<A> unbounded() {
    return new Bounded<>(null, false, null, false);
  }
}
