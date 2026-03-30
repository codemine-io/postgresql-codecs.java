package io.codemine.postgresql.codecs;

/** PostgreSQL {@code line} type. Represents the line Ax + By + C = 0. */
public record Line(double a, double b, double c) {}
