package io.codemine.java.postgresql.codecs;

/**
 * PostgreSQL {@code macaddr8} type. An 8-byte MAC address (EUI-64).
 *
 * <p>The canonical text format is {@code xx:xx:xx:xx:xx:xx:xx:xx} in lower-case hexadecimal.
 */
public record Macaddr8(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
  private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

  /**
   * Creates a {@code Macaddr8} from an 8-byte array.
   *
   * @param bytes an 8-byte EUI-64 MAC address; the array is not retained
   * @throws IllegalArgumentException if the array length is not 8
   */
  public static Macaddr8 of(byte[] bytes) {
    if (bytes.length != 8) {
      throw new IllegalArgumentException(
          "EUI-64 MAC address must be exactly 8 bytes, got " + bytes.length);
    }
    return new Macaddr8(
        bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
  }

  /**
   * Returns the 8 bytes of this EUI-64 MAC address as a new byte array.
   *
   * @return a fresh 8-byte array
   */
  public byte[] toBytes() {
    return new byte[] {b1, b2, b3, b4, b5, b6, b7, b8};
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(23);
    appendInTextTo(sb);
    return sb.toString();
  }

  void appendInTextTo(StringBuilder sb) {
    appendHexByte(sb, b1);
    sb.append(':');
    appendHexByte(sb, b2);
    sb.append(':');
    appendHexByte(sb, b3);
    sb.append(':');
    appendHexByte(sb, b4);
    sb.append(':');
    appendHexByte(sb, b5);
    sb.append(':');
    appendHexByte(sb, b6);
    sb.append(':');
    appendHexByte(sb, b7);
    sb.append(':');
    appendHexByte(sb, b8);
  }

  private static void appendHexByte(StringBuilder sb, byte value) {
    int unsigned = value & 0xff;
    sb.append(HEX_DIGITS[unsigned >>> 4]);
    sb.append(HEX_DIGITS[unsigned & 0x0f]);
  }
}
