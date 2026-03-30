package io.codemine.postgresql.codecs;

import java.util.List;

/**
 * PostgreSQL {@code polygon} type. A closed geometric shape defined by a list of vertices.
 *
 * @param points the vertices of the polygon
 */
public record Polygon(List<Point> points) {}
