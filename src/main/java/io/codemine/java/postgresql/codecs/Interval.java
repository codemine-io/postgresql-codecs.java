package io.codemine.java.postgresql.codecs;

/**
 * PostgreSQL {@code interval} type. A time span with separate month, day, and microsecond
 * components.
 *
 * @param micros time component in microseconds
 * @param days day component
 * @param months month component (may be decomposed into years and months for display)
 */
public record Interval(long micros, int days, int months) {

  /**
   * Creates an {@code Interval} from a {@link java.time.Duration}. The duration is converted to an
   * interval by decomposing it into days and microseconds, and then further decomposing the days
   * into months (assuming 30 days per month). Note that this conversion is lossy and may not
   * accurately represent the original duration, especially for durations that span multiple months
   * or years, due to the fixed assumption of 30 days per month.
   *
   * <p>It also loses the nanosecond precision because the interval type only supports microseconds.
   */
  public static Interval of(java.time.Duration duration) {
    long seconds = duration.getSeconds();
    int nanos = duration.getNano();
    long days = seconds / 86400L;
    seconds %= 86400L;
    long months = days / 30L;
    days %= 30L;
    long micros = seconds * 1_000_000L + nanos / 1000;
    return new Interval(micros, (int) days, (int) months);
  }

  /**
   * Converts this {@code Interval} to a {@link java.time.Duration}. The conversion is done by
   * calculating the total number of seconds represented by the interval (including the time, day,
   * and month components) and then creating a Duration from that total. Note that this conversion
   * is lossy and may not accurately represent the original interval, especially for intervals that
   * include month or day components, due to the fixed assumption of 30 days per month and the loss
   * of nanosecond precision.
   */
  public java.time.Duration toDuration() {
    long seconds = micros / 1_000_000L + (days + months * 30L) * 86400L;
    int nanoAdjustment = (int) (micros % 1_000_000L) * 1000;

    return java.time.Duration.ofSeconds(seconds, nanoAdjustment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendInTextTo(sb);
    return sb.toString();
  }

  void appendInTextTo(StringBuilder sb) {
    int years = months / 12;
    int mons = months % 12;

    boolean hasParts = false;

    if (years != 0) {
      sb.append(years).append(years == 1 ? " year" : " years");
      hasParts = true;
    }
    if (mons != 0) {
      if (hasParts) {
        sb.append(' ');
      }
      sb.append(mons).append(mons == 1 ? " mon" : " mons");
      hasParts = true;
    }
    if (days != 0) {
      if (hasParts) {
        sb.append(' ');
      }
      sb.append(days).append(days == 1 ? " day" : " days");
      hasParts = true;
    }
    if (!hasParts) {
      appendTime(sb);
    } else if (micros != 0) {
      sb.append(' ');
      appendTime(sb);
    }
  }

  private void appendTime(StringBuilder sb) {
    long microsState = micros;
    if (microsState < 0) {
      sb.append('-');
      microsState = -microsState;
    }
    long hours = microsState / 3_600_000_000L;
    microsState %= 3_600_000_000L;

    pad2(sb, hours);
    sb.append(':');

    long minutes = microsState / 60_000_000L;
    microsState %= 60_000_000L;
    pad2(sb, minutes);
    sb.append(':');

    long seconds = microsState / 1_000_000L;
    long frac = microsState % 1_000_000L;
    pad2(sb, seconds);
    if (frac > 0) {
      sb.append('.');
      int val = (int) frac;
      sb.append((char) ('0' + val / 100000));
      sb.append((char) ('0' + val / 10000 % 10));
      sb.append((char) ('0' + val / 1000 % 10));
      sb.append((char) ('0' + val / 100 % 10));
      sb.append((char) ('0' + val / 10 % 10));
      sb.append((char) ('0' + val % 10));
      int len = sb.length();
      while (sb.charAt(len - 1) == '0') {
        len--;
      }
      sb.setLength(len);
    }
  }

  private static void pad2(StringBuilder sb, long v) {
    if (v < 10) {
      sb.append('0');
    }
    sb.append(v);
  }
}
