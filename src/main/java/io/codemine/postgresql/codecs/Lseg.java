package io.codemine.postgresql.codecs;

/** PostgreSQL {@code lseg} type. A line segment defined by two endpoints. */
public record Lseg(double x1, double y1, double x2, double y2) {}
