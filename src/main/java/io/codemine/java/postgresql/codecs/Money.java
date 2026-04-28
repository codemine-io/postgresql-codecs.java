package io.codemine.java.postgresql.codecs;

import java.math.BigDecimal;

/**
 * PostgreSQL {@code money} type. A monetary amount stored as a scaled integer.
 *
 * <p>PostgreSQL stores {@code money} internally as a 64-bit integer whose scale (i.e. the number of
 * fractional decimal digits) is determined by the database's {@code lc_monetary} locale setting.
 * The most common scale is {@code 2} (cents), but locales with no fractional unit (e.g. Japanese
 * yen) use {@code 0}, and some use other values.
 *
 * <p>The raw {@code amount} field holds the unscaled integer exactly as PostgreSQL stores it
 * internally (e.g. {@code 123456} for $1,234.56 at scale 2). Use {@link #toBigDecimal(int)} to
 * obtain a human-readable decimal value, and {@link #of(BigDecimal, int)} to construct a {@code
 * Money} from one.
 *
 * <p>The scale is not stored in the value itself. It is a property of the codec ({@link
 * Codec#money(int)}) and ultimately of the database configuration. Both {@link #of(BigDecimal,
 * int)} and {@link #toBigDecimal(int)} therefore accept the {@code decimals} parameter explicitly.
 *
 * @param amount the raw PostgreSQL-internal scaled integer
 */
public record Money(long amount) {

  /**
   * Creates a {@code Money} from a {@link BigDecimal} value.
   *
   * <p>The {@code decimals} parameter must match the scale configured in the target database
   * (derived from {@code lc_monetary}). For example, to represent $12.34 pass {@code
   * BigDecimal.valueOf(12, 34)} (or {@code new BigDecimal("12.34")}) with {@code decimals = 2}.
   *
   * <p>The conversion multiplies {@code value} by {@code 10^decimals} and takes the exact {@code
   * long} result. {@link ArithmeticException} is thrown if {@code value} has more fractional digits
   * than {@code decimals} allows, or if the scaled result overflows a {@code long}.
   *
   * @param value the monetary amount as a decimal
   * @param decimals the number of fractional digits (must be ≥ 0)
   * @throws ArithmeticException if the value cannot be represented exactly
   */
  public static Money of(BigDecimal value, int decimals) {
    return new Money(value.scaleByPowerOfTen(decimals).longValueExact());
  }

  /**
   * Converts this {@code Money} to a {@link BigDecimal}.
   *
   * <p>The {@code decimals} parameter must match the scale that was used when this value was
   * created (typically the {@code lc_monetary}-derived scale of the database).
   *
   * @param decimals the number of fractional digits (must be ≥ 0)
   * @return the monetary amount as a decimal, e.g. {@code 12.34} for amount {@code 1234} at scale 2
   */
  public BigDecimal toBigDecimal(int decimals) {
    return BigDecimal.valueOf(amount, decimals);
  }
}
