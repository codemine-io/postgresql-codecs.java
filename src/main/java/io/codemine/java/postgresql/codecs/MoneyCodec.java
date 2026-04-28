package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code money} values. */
final class MoneyCodec implements Codec<BigDecimal> {

  private final int decimals;

  MoneyCodec(int decimals) {
    if (decimals < 0) {
      throw new IllegalArgumentException("decimals must be non-negative, got " + decimals);
    }
    this.decimals = decimals;
  }

  @Override
  public String name() {
    return "money";
  }

  @Override
  public int scalarOid() {
    return 790;
  }

  @Override
  public int arrayOid() {
    return 791;
  }

  /**
   * Writes the money value as a plain decimal (e.g. {@code 1234.56} or {@code -1234.56}) without
   * any currency symbol or grouping separators, which PostgreSQL accepts on all locales.
   */
  @Override
  public void encodeInText(StringBuilder sb, BigDecimal value) {
    sb.append(normalize(value).toPlainString());
  }

  /**
   * Parses PostgreSQL money text output, which is locale-dependent (e.g. {@code $1,234.56}, {@code
   * €1.234,56}). This implementation:
   *
   * <ul>
   *   <li>Handles both {@code -value} and {@code (value)} negative notations.
   *   <li>Strips all currency symbols and other non-numeric characters.
   *   <li>Auto-detects the decimal separator: whichever of {@code .} or {@code ,} appears last and
   *       is followed by exactly {@code decimals} digits is treated as the decimal separator; the
   *       other is a grouping separator.
   * </ul>
   */
  @Override
  public Codec.ParsingResult<BigDecimal> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      boolean negative = false;

      // Handle parentheses (accounting negative format, e.g. "($1.23)").
      if (s.startsWith("(") && s.endsWith(")")) {
        negative = true;
        s = s.substring(1, s.length() - 1).trim();
      }
      // Handle leading '-'.
      if (s.startsWith("-")) {
        negative = true;
        s = s.substring(1).trim();
      }

      // Strip everything except digits, '.' and ','.
      s = s.replaceAll("[^0-9.,]", "");

      long integerPart;
      long fractionalPart;

      int lastDot = s.lastIndexOf('.');
      int lastComma = s.lastIndexOf(',');
      int lastSep = Math.max(lastDot, lastComma);

      if (lastSep >= 0 && (s.length() - lastSep - 1) == decimals) {
        // The last separator is followed by exactly `decimals` digits → decimal separator.
        char grpSep = (s.charAt(lastSep) == '.') ? ',' : '.';
        String intPart = s.substring(0, lastSep).replace(String.valueOf(grpSep), "");
        String fracPart = s.substring(lastSep + 1);
        integerPart = intPart.isEmpty() ? 0L : Long.parseLong(intPart);
        fractionalPart = Long.parseLong(fracPart);
      } else {
        // No recognisable decimal separator — treat the whole string as integer units.
        String digits = s.replaceAll("[.,]", "");
        integerPart = digits.isEmpty() ? 0L : Long.parseLong(digits);
        fractionalPart = 0L;
      }

      long scale = pow10(decimals);
      long amount = integerPart * scale + fractionalPart;
      if (negative) {
        amount = -amount;
      }
      return new Codec.ParsingResult<>(BigDecimal.valueOf(amount, decimals), input.length());
    } catch (NumberFormatException e) {
      throw new Codec.DecodingException(input, offset, "Invalid money: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(BigDecimal value, ByteArrayOutputStream out) {
    long v = toScaledLong(value);
    out.write((int) (v >>> 56) & 0xFF);
    out.write((int) (v >>> 48) & 0xFF);
    out.write((int) (v >>> 40) & 0xFF);
    out.write((int) (v >>> 32) & 0xFF);
    out.write((int) (v >>> 24) & 0xFF);
    out.write((int) (v >>> 16) & 0xFF);
    out.write((int) (v >>> 8) & 0xFF);
    out.write((int) (v & 0xFF));
  }

  @Override
  public BigDecimal decodeInBinary(ByteBuffer buf, int length) {
    return BigDecimal.valueOf(buf.getLong(), decimals);
  }

  @Override
  public BigDecimal random(Random r, int size) {
    if (size == 0) {
      return BigDecimal.ZERO.setScale(decimals);
    }
    long bound = (long) size * pow10(decimals);
    return BigDecimal.valueOf(r.nextLong(-bound, bound + 1), decimals);
  }

  private BigDecimal normalize(BigDecimal value) {
    return value.setScale(decimals, RoundingMode.UNNECESSARY);
  }

  private long toScaledLong(BigDecimal value) {
    try {
      return normalize(value).unscaledValue().longValueExact();
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException(
          "Money value "
              + value
              + " cannot be represented with "
              + decimals
              + " fractional digits as a signed 64-bit integer",
          e);
    }
  }

  private static long pow10(int n) {
    long result = 1L;
    for (int i = 0; i < n; i++) {
      result *= 10L;
    }
    return result;
  }
}
