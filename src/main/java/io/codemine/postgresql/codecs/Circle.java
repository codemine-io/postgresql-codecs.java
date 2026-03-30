package io.codemine.postgresql.codecs;

/**
 * PostgreSQL {@code circle} type. A circle defined by center point (x, y) and radius r.
 *
 * @param x the x-coordinate of the center
 * @param y the y-coordinate of the center
 * @param r the radius (non-negative)
 */
public record Circle(double x, double y, double r) {}
