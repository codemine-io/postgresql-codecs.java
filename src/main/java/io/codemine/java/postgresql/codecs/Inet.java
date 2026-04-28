package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * PostgreSQL {@code inet} type. IPv4 or IPv6 host address with optional subnet mask.
 *
 * <p>Holds an IPv4 or IPv6 host address, and optionally its subnet, all in one field. The subnet is
 * represented by the number of network address bits present in the host address (the "netmask"). If
 * the netmask is 32 and the address is IPv4 (or 128 for IPv6), the value represents just a single
 * host.
 */
public sealed interface Inet permits Inet.V4, Inet.V6 {

  /** Appends the PostgreSQL text representation of this address to {@code sb}. */
  void appendInTextTo(StringBuilder sb);

  /** Encodes this address in PostgreSQL binary wire format into {@code out}. */
  void encodeInBinary(ByteArrayOutputStream out);

  /**
   * Returns the address part of this {@code Inet} as a {@link InetAddress}. The netmask is not
   * represented in the returned object.
   */
  InetAddress toInetAddress();

  /**
   * IPv4 host address with optional subnet mask.
   *
   * @param address IPv4 address as a 32-bit big-endian word.
   * @param netmask Network mask length in the range 0–32.
   */
  record V4(int address, byte netmask) implements Inet {

    /**
     * Creates an {@code Inet.V4} from a {@link Inet4Address} with a full host netmask ({@code
     * /32}).
     *
     * @param addr the IPv4 address
     */
    public static Inet.V4 of(Inet4Address addr) {
      byte[] raw = addr.getAddress();
      int a =
          ((raw[0] & 0xFF) << 24)
              | ((raw[1] & 0xFF) << 16)
              | ((raw[2] & 0xFF) << 8)
              | (raw[3] & 0xFF);
      return new Inet.V4(a, (byte) 32);
    }

    /**
     * Returns the address part of this value as a {@link Inet4Address}. The netmask is not
     * represented in the returned object.
     */
    @Override
    public Inet4Address toInetAddress() {
      byte[] raw = {
        (byte) ((address >>> 24) & 0xFF),
        (byte) ((address >>> 16) & 0xFF),
        (byte) ((address >>> 8) & 0xFF),
        (byte) (address & 0xFF)
      };
      try {
        return (Inet4Address) InetAddress.getByAddress(raw);
      } catch (UnknownHostException e) {
        throw new AssertionError("unreachable: 4-byte address is always valid", e);
      }
    }

    @Override
    public void appendInTextTo(StringBuilder sb) {
      sb.append((address >>> 24) & 0xFF);
      sb.append('.');
      sb.append((address >>> 16) & 0xFF);
      sb.append('.');
      sb.append((address >>> 8) & 0xFF);
      sb.append('.');
      sb.append(address & 0xFF);
      if ((netmask & 0xff) != 32) {
        sb.append('/').append(netmask & 0xff);
      }
    }

    @Override
    public void encodeInBinary(ByteArrayOutputStream out) {
      out.write(2); // IPv4 address family
      out.write(netmask);
      out.write(0); // is_cidr = 0 for inet
      out.write(4); // address length
      out.write((address >>> 24) & 0xFF);
      out.write((address >>> 16) & 0xFF);
      out.write((address >>> 8) & 0xFF);
      out.write(address & 0xFF);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      appendInTextTo(sb);
      return sb.toString();
    }
  }

  /**
   * IPv6 host address with optional subnet mask.
   *
   * @param w1 First 32 bits of the IPv6 address in big-endian order.
   * @param w2 Second 32 bits of the IPv6 address in big-endian order.
   * @param w3 Third 32 bits of the IPv6 address in big-endian order.
   * @param w4 Fourth 32 bits of the IPv6 address in big-endian order.
   * @param netmask Network mask length in the range 0–128.
   */
  record V6(int w1, int w2, int w3, int w4, byte netmask) implements Inet {

    /**
     * Creates an {@code Inet.V6} from a {@link Inet6Address} with a full host netmask ({@code
     * /128}).
     *
     * @param addr the IPv6 address
     */
    public static Inet.V6 of(Inet6Address addr) {
      byte[] raw = addr.getAddress();
      int a1 =
          ((raw[0] & 0xFF) << 24)
              | ((raw[1] & 0xFF) << 16)
              | ((raw[2] & 0xFF) << 8)
              | (raw[3] & 0xFF);
      int a2 =
          ((raw[4] & 0xFF) << 24)
              | ((raw[5] & 0xFF) << 16)
              | ((raw[6] & 0xFF) << 8)
              | (raw[7] & 0xFF);
      int a3 =
          ((raw[8] & 0xFF) << 24)
              | ((raw[9] & 0xFF) << 16)
              | ((raw[10] & 0xFF) << 8)
              | (raw[11] & 0xFF);
      int a4 =
          ((raw[12] & 0xFF) << 24)
              | ((raw[13] & 0xFF) << 16)
              | ((raw[14] & 0xFF) << 8)
              | (raw[15] & 0xFF);
      return new Inet.V6(a1, a2, a3, a4, (byte) 128);
    }

    /**
     * Returns the address part of this value as a {@link Inet6Address}. The netmask is not
     * represented in the returned object.
     */
    @Override
    public Inet6Address toInetAddress() {
      byte[] raw = {
        (byte) ((w1 >>> 24) & 0xFF), (byte) ((w1 >>> 16) & 0xFF),
        (byte) ((w1 >>> 8) & 0xFF), (byte) (w1 & 0xFF),
        (byte) ((w2 >>> 24) & 0xFF), (byte) ((w2 >>> 16) & 0xFF),
        (byte) ((w2 >>> 8) & 0xFF), (byte) (w2 & 0xFF),
        (byte) ((w3 >>> 24) & 0xFF), (byte) ((w3 >>> 16) & 0xFF),
        (byte) ((w3 >>> 8) & 0xFF), (byte) (w3 & 0xFF),
        (byte) ((w4 >>> 24) & 0xFF), (byte) ((w4 >>> 16) & 0xFF),
        (byte) ((w4 >>> 8) & 0xFF), (byte) (w4 & 0xFF)
      };
      try {
        return (Inet6Address) InetAddress.getByAddress(raw);
      } catch (UnknownHostException e) {
        throw new AssertionError("unreachable: 16-byte address is always valid", e);
      }
    }

    @Override
    public void appendInTextTo(StringBuilder sb) {
      // Formats a 128-bit IPv6 address (stored as four 32-bit words) as compressed text per
      // RFC 5952, e.g. {@code ::1} instead of {@code 0:0:0:0:0:0:0:1}.

      int[] g = {
        (w1 >>> 16) & 0xFFFF, w1 & 0xFFFF,
        (w2 >>> 16) & 0xFFFF, w2 & 0xFFFF,
        (w3 >>> 16) & 0xFFFF, w3 & 0xFFFF,
        (w4 >>> 16) & 0xFFFF, w4 & 0xFFFF
      };
      // Find the longest consecutive run of zero groups (min length 2)
      int elideStart = -1;
      int elideLen = 0;
      int curStart = -1;
      int curLen = 0;
      for (int i = 0; i < 8; i++) {
        if (g[i] == 0) {
          if (curLen == 0) {
            curStart = i;
          }
          curLen++;
        } else {
          if (curLen >= 2 && curLen > elideLen) {
            elideLen = curLen;
            elideStart = curStart;
          }
          curLen = 0;
        }
      }
      if (curLen >= 2 && curLen > elideLen) {
        elideLen = curLen;
        elideStart = curStart;
      }

      boolean first = true;
      int i = 0;
      while (i < 8) {
        if (elideStart >= 0 && i == elideStart) {
          sb.append("::");
          i += elideLen;
          first = false;
        } else {
          if (!first) {
            sb.append(':');
          }
          sb.append(Integer.toHexString(g[i]));
          first = false;
          i++;
        }
      }

      if ((netmask & 0xff) != 128) {
        sb.append('/').append(netmask & 0xff);
      }
    }

    @Override
    public void encodeInBinary(ByteArrayOutputStream out) {
      out.write(3); // IPv6 address family for INET
      out.write(netmask);
      out.write(0); // is_cidr = 0 for inet
      out.write(16); // address length
      out.write((w1 >>> 24) & 0xFF);
      out.write((w1 >>> 16) & 0xFF);
      out.write((w1 >>> 8) & 0xFF);
      out.write(w1 & 0xFF);
      out.write((w2 >>> 24) & 0xFF);
      out.write((w2 >>> 16) & 0xFF);
      out.write((w2 >>> 8) & 0xFF);
      out.write(w2 & 0xFF);
      out.write((w3 >>> 24) & 0xFF);
      out.write((w3 >>> 16) & 0xFF);
      out.write((w3 >>> 8) & 0xFF);
      out.write(w3 & 0xFF);
      out.write((w4 >>> 24) & 0xFF);
      out.write((w4 >>> 16) & 0xFF);
      out.write((w4 >>> 8) & 0xFF);
      out.write(w4 & 0xFF);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      appendInTextTo(sb);
      return sb.toString();
    }
  }
}
