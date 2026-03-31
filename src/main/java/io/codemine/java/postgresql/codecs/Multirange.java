package io.codemine.java.postgresql.codecs;

import java.util.List;
import java.util.Objects;

/**
 * PostgreSQL multirange type. A collection of non-overlapping, non-adjacent ranges stored as a
 * single value.
 *
 * <p>PostgreSQL automatically normalizes multiranges by combining overlapping and adjacent ranges.
 * The ranges are stored in sorted order.
 *
 * <p>The standard PostgreSQL multirange types are:
 *
 * <ul>
 *   <li>{@code int4multirange} — {@code Multirange<Integer>}
 *   <li>{@code int8multirange} — {@code Multirange<Long>}
 *   <li>{@code nummultirange} — {@code Multirange<java.math.BigDecimal>}
 *   <li>{@code tsmultirange} — {@code Multirange<java.time.LocalDateTime>}
 *   <li>{@code tstzmultirange} — {@code Multirange<java.time.Instant>}
 *   <li>{@code datemultirange} — {@code Multirange<java.time.LocalDate>}
 * </ul>
 *
 * @param ranges the list of ranges in this multirange, in sorted order
 * @param <A> the element type of the ranges
 */
public record Multirange<A>(List<Range<A>> ranges) {

  /** Compact constructor that makes an immutable copy of ranges. */
  public Multirange {
    Objects.requireNonNull(ranges);
    ranges = List.copyOf(ranges);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (int i = 0; i < ranges.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(ranges.get(i));
    }
    sb.append('}');
    return sb.toString();
  }
}
