package io.codemine.java.postgresql.codecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** PostgreSQL {@code ltree} type. A hierarchical path of labels separated by dots. */
public record Ltree(List<String> labels) {

  /** Constructs a new {@code Ltree} instance with the given labels. */
  public Ltree {
    Objects.requireNonNull(labels);
    List<String> copy = new ArrayList<>(labels.size());
    for (String label : labels) {
      validateLabel(label);
      copy.add(label);
    }
    labels = List.copyOf(copy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendInTextTo(sb);
    return sb.toString();
  }

  void appendInTextTo(StringBuilder sb) {
    for (int i = 0; i < labels.size(); i++) {
      if (i > 0) {
        sb.append('.');
      }
      sb.append(labels.get(i));
    }
  }

  private static void validateLabel(String label) {
    Objects.requireNonNull(label);
    if (label.isEmpty()) {
      throw new IllegalArgumentException("ltree labels must not be empty");
    }

    for (int i = 0; i < label.length(); ) {
      int codePoint = label.codePointAt(i);
      if (!Character.isLetterOrDigit(codePoint) && codePoint != '_' && codePoint != '-') {
        throw new IllegalArgumentException("invalid ltree label: " + label);
      }
      i += Character.charCount(codePoint);
    }
  }
}
