package io.codemine.java.postgresql.codecs;

/**
 * PostgreSQL {@code macaddr} type. MAC (Media Access Control) address.
 *
 * <p>Represents a 6-byte MAC address stored as six individual bytes. The canonical text format is
 * {@code xx:xx:xx:xx:xx:xx} in lower-case hexadecimal.
 */
public record Macaddr(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6) {
  private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

  /**
   * Creates a {@code Macaddr} from a 6-byte array.
   *
   * @param bytes a 6-byte MAC address; the array is not retained
   * @throws IllegalArgumentException if the array length is not 6
   */
  public static Macaddr of(byte[] bytes) {
    if (bytes.length != 6) {
      throw new IllegalArgumentException(
          "MAC address must be exactly 6 bytes, got " + bytes.length);
    }
    return new Macaddr(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]);
  }

  /**
   * Returns the 6 bytes of this MAC address as a new byte array.
   *
   * @return a fresh 6-byte array
   */
  public byte[] toBytes() {
    return new byte[] {b1, b2, b3, b4, b5, b6};
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(17);
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
  }

  private static void appendHexByte(StringBuilder sb, byte value) {
    int unsignedValue = value & 0xff;
    sb.append(HEX_DIGITS[unsignedValue >>> 4]);
    sb.append(HEX_DIGITS[unsignedValue & 0x0f]);
  }
}
