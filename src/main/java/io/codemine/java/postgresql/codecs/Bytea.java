package io.codemine.java.postgresql.codecs;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HexFormat;

/** A wrapper around a byte array representing a PostgreSQL {@code bytea} value. */
public record Bytea(byte[] bytes) {

  private static final HexFormat HEX = HexFormat.of();

  /**
   * Creates a {@code Bytea} from a {@code byte[]}. The array is defensively copied.
   *
   * @param bytes the byte array to wrap
   */
  public static Bytea of(byte[] bytes) {
    return new Bytea(Arrays.copyOf(bytes, bytes.length));
  }

  /**
   * Creates a {@code Bytea} from a {@link ByteBuffer}. The remaining bytes of the buffer are copied
   * into a new array; the buffer's position is advanced past them.
   *
   * @param buf the source buffer
   */
  public static Bytea of(ByteBuffer buf) {
    byte[] copy = new byte[buf.remaining()];
    buf.get(copy);
    return new Bytea(copy);
  }

  /**
   * Returns a read-only {@link ByteBuffer} backed by the internal byte array.
   *
   * @return a read-only view of the bytes
   */
  public ByteBuffer toByteBuffer() {
    return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Bytea b && Arrays.equals(bytes, b.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendInTextTo(sb);
    return sb.toString();
  }

  void appendInTextTo(StringBuilder sb) {
    sb.append("\\x");
    sb.append(HEX.formatHex(bytes));
  }
}
