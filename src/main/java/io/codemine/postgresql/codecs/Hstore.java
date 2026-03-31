package io.codemine.postgresql.codecs;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * PostgreSQL {@code hstore} type. A key-value store where both keys and values are text strings.
 * Values may be {@code null} (representing SQL NULL).
 *
 * <p>Keys are unique and non-null. The entries are stored in a sorted {@link TreeMap} to provide
 * deterministic ordering for encoding.
 *
 * @param entries the key-value map; values may be null, keys must not be null
 */
public record Hstore(Map<String, String> entries) {

  /** Compact constructor that makes a sorted, unmodifiable copy of entries. */
  public Hstore {
    Objects.requireNonNull(entries);
    entries = Collections.unmodifiableMap(new TreeMap<>(entries));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendInTextTo(sb);
    return sb.toString();
  }

  void appendInTextTo(StringBuilder sb) {
    boolean first = true;
    for (var entry : entries.entrySet()) {
      if (!first) {
        sb.append(", ");
      }
      first = false;
      sb.append('"');
      sb.append(escapeHstoreText(entry.getKey()));
      sb.append("\"=>");
      if (entry.getValue() == null) {
        sb.append("NULL");
      } else {
        sb.append('"');
        sb.append(escapeHstoreText(entry.getValue()));
        sb.append('"');
      }
    }
  }

  static String escapeHstoreText(String text) {
    StringBuilder sb = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '\\') {
        sb.append("\\\\");
      } else if (c == '"') {
        sb.append("\\\"");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
