package io.codemine.postgresql.codecs;

/**
 * PostgreSQL {@code interval} type. A time span with separate month, day, and microsecond
 * components.
 *
 * @param time time component in microseconds
 * @param day day component
 * @param month month component (may be decomposed into years and months for display)
 */
public record Interval(long time, int day, int month) {}
