package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.Random;

/** Codec for PostgreSQL {@code date} values (days offset from 2000-01-01). */
final class DateCodec implements Codec<Integer> {

  private static final LocalDate PG_EPOCH = LocalDate.of(2000, 1, 1);
  private static final long PG_EPOCH_DAY = PG_EPOCH.toEpochDay();

  @Override
  public String name() {
    return "date";
  }

  @Override
  public int scalarOid() {
    return 1082;
  }

  @Override
  public int arrayOid() {
    return 1182;
  }

  @Override
  public void write(StringBuilder sb, Integer value) {
    sb.append(PG_EPOCH.plusDays(value));
  }

  @Override
  public Codec.ParsingResult<Integer> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      LocalDate date = LocalDate.parse(s);
      int days = (int) (date.toEpochDay() - PG_EPOCH_DAY);
      return new Codec.ParsingResult<>(days, input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid date: " + s);
    }
  }

  @Override
  public void encodeInBinary(Integer value, ByteArrayOutputStream out) {
    out.write((value >>> 24) & 0xFF);
    out.write((value >>> 16) & 0xFF);
    out.write((value >>> 8) & 0xFF);
    out.write(value & 0xFF);
  }

  @Override
  public Integer decodeInBinary(ByteBuffer buf, int length) {
    return buf.getInt();
  }

  @Override
  public Integer random(Random r, int size) {
    if (size == 0) return 0;
    int bound = Math.min(size * 365, 3_652_425);
    return r.nextInt(2 * bound + 1) - bound;
  }
}
