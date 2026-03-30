package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code numeric} values. */
final class NumericCodec implements Codec<BigDecimal> {

  private static final short NUMERIC_POS = 0x0000;
  private static final short NUMERIC_NEG = 0x4000;
  private static final short NUMERIC_NAN = (short) 0xC000;
  private static final int NBASE = 10000;

  @Override
  public String name() {
    return "numeric";
  }

  @Override
  public int scalarOid() {
    return 1700;
  }

  @Override
  public int arrayOid() {
    return 1231;
  }

  @Override
  public void write(StringBuilder sb, BigDecimal value) {
    sb.append(value.toPlainString());
  }

  @Override
  public Codec.ParsingResult<BigDecimal> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      BigDecimal value = new BigDecimal(s);
      return new Codec.ParsingResult<>(value, input.length());
    } catch (NumberFormatException e) {
      throw new Codec.DecodingException(input, offset, "Invalid numeric: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(BigDecimal value, ByteArrayOutputStream out) {
    short sign = value.signum() < 0 ? NUMERIC_NEG : NUMERIC_POS;
    BigDecimal abs = value.abs();
    int scale = abs.scale();
    short dscale = (short) Math.max(scale, 0);

    // Pad scale up to a multiple of 4 for base-10000 alignment.
    int paddedScale = scale <= 0 ? 0 : ((scale + 3) / 4) * 4;

    // Multiply by 10^paddedScale to get an integer whose base-10000 digits
    // directly represent the digit array of the PostgreSQL numeric.
    BigInteger scaledInt = abs.scaleByPowerOfTen(paddedScale).toBigIntegerExact();

    // Compute base-10000 digits.
    short[] digits = toBase10000(scaledInt);
    short ndigits = (short) digits.length;

    // Weight: the first digit sits at position NBASE^weight.
    // scaledInt = value * NBASE^(paddedScale/4), so
    // weight = ndigits - 1 - (paddedScale / 4).
    int fractionalPositions = paddedScale / 4;
    short weight = ndigits == 0 ? (short) 0 : (short) (ndigits - 1 - fractionalPositions);

    // Strip trailing zero digits.
    int effectiveNdigits = ndigits;
    while (effectiveNdigits > 0 && digits[effectiveNdigits - 1] == 0) {
      effectiveNdigits--;
    }

    writeShort(out, (short) effectiveNdigits);
    writeShort(out, weight);
    writeShort(out, sign);
    writeShort(out, dscale);
    for (int i = 0; i < effectiveNdigits; i++) {
      writeShort(out, digits[i]);
    }
  }

  @Override
  public BigDecimal decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    short ndigits = buf.getShort();
    short weight = buf.getShort();
    short sign = buf.getShort();
    short dscale = buf.getShort();

    if (sign == NUMERIC_NAN) {
      throw new Codec.DecodingException("Cannot represent PostgreSQL numeric NaN as BigDecimal");
    }

    if (ndigits == 0) {
      return BigDecimal.ZERO.setScale(dscale);
    }

    // Reconstruct the value from base-10000 digits.
    BigInteger unscaled = BigInteger.ZERO;
    for (int i = 0; i < ndigits; i++) {
      short digit = buf.getShort();
      unscaled = unscaled.multiply(BigInteger.valueOf(NBASE)).add(BigInteger.valueOf(digit));
    }

    if (sign == NUMERIC_NEG) {
      unscaled = unscaled.negate();
    }

    // The total number of base-10000 positions contributing to the fractional part.
    int fractionalPositions = ndigits - (weight + 1);
    int impliedScale = fractionalPositions * 4;

    BigDecimal result = new BigDecimal(unscaled, impliedScale);
    if (dscale >= 0 && dscale != result.scale()) {
      result = result.setScale(dscale);
    }
    return result;
  }

  @Override
  public BigDecimal random(Random r, int size) {
    if (size == 0) {
      return BigDecimal.ZERO;
    }
    long unscaledVal = r.nextLong() % ((long) size * 10000L);
    int scale = r.nextInt(10);
    return BigDecimal.valueOf(unscaledVal, scale);
  }

  private static void writeShort(ByteArrayOutputStream out, short value) {
    out.write((value >>> 8) & 0xFF);
    out.write(value & 0xFF);
  }

  /**
   * Converts a non-negative BigInteger to an array of base-10000 digits (most significant first).
   */
  private static short[] toBase10000(BigInteger value) {
    if (value.signum() == 0) {
      return new short[0];
    }
    BigInteger base = BigInteger.valueOf(NBASE);
    // Count digits first.
    int count = 0;
    BigInteger temp = value;
    while (temp.signum() > 0) {
      count++;
      temp = temp.divide(base);
    }
    short[] digits = new short[count];
    temp = value;
    for (int i = count - 1; i >= 0; i--) {
      BigInteger[] divRem = temp.divideAndRemainder(base);
      digits[i] = divRem[1].shortValue();
      temp = divRem[0];
    }
    return digits;
  }
}
