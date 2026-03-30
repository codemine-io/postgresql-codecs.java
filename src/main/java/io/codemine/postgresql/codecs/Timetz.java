package io.codemine.postgresql.codecs;

/**
 * PostgreSQL {@code timetz} type. Time of day with time zone.
 *
 * @param time microseconds from midnight (0 to 86400000000)
 * @param zone timezone offset in seconds with <b>inverted sign</b> per PostgreSQL convention: UTC+1
 *     is stored as {@code -3600}
 */
public record Timetz(long time, int zone) {}
