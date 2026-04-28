package io.codemine.java.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.codemine.java.postgresql.CodecTestBase;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

public class TimetzCodecTest extends CodecTestBase<Timetz> {
  public TimetzCodecTest() {
    super(Codec.TIMETZ);
  }

  @Test
  void createsTimetzFromOffsetTime() {
    OffsetTime value = OffsetTime.of(12, 34, 56, 123_456_789, ZoneOffset.ofHoursMinutes(5, 45));

    assertEquals(new Timetz(45_296_123_456L, -20_700), Timetz.of(value));
  }

  @Test
  void convertsTimetzToOffsetTime() {
    Timetz value = new Timetz(45_296_123_456L, 12_600);

    assertEquals(
        OffsetTime.of(12, 34, 56, 123_456_000, ZoneOffset.ofHoursMinutes(-3, -30)),
        value.toOffsetTime());
  }

  @Test
  void truncatesOffsetTimeToMicroseconds() {
    OffsetTime value = OffsetTime.of(1, 2, 3, 987_654_321, ZoneOffset.UTC);

    assertEquals(
        OffsetTime.of(1, 2, 3, 987_654_000, ZoneOffset.UTC), Timetz.of(value).toOffsetTime());
  }
}
