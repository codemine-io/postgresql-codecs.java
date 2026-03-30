package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Codec for PostgreSQL {@code cidr} values. Reuses the {@link Inet} type but with different name,
 * OIDs, and binary is_cidr flag.
 */
final class CidrCodec implements Codec<Inet> {

  @Override
  public String name() {
    return "cidr";
  }

  @Override
  public int scalarOid() {
    return 650;
  }

  @Override
  public int arrayOid() {
    return 651;
  }

  @Override
  public void write(StringBuilder sb, Inet value) {
    // CIDR always shows the netmask, unlike inet which omits /32 and /128
    switch (value) {
      case Inet.V4 v4 -> {
        int addr = v4.address();
        sb.append((addr >>> 24) & 0xFF)
            .append('.')
            .append((addr >>> 16) & 0xFF)
            .append('.')
            .append((addr >>> 8) & 0xFF)
            .append('.')
            .append(addr & 0xFF)
            .append('/')
            .append(v4.netmask() & 0xff);
      }
      case Inet.V6 v6 -> {
        // Reuse the Inet.V6 write method but ensure netmask is always shown
        v6.write(sb);
        // If write didn't include the mask (because it's 128), append it
        if ((v6.netmask() & 0xff) == 128) {
          sb.append("/128");
        }
      }
    }
  }

  @Override
  public Codec.ParsingResult<Inet> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      return new Codec.ParsingResult<>(InetCodec.parseInet(s), input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid cidr: " + s);
    }
  }

  @Override
  public void encodeInBinary(Inet value, ByteArrayOutputStream out) {
    switch (value) {
      case Inet.V4 v4 -> {
        out.write(2); // IPv4 address family
        out.write(v4.netmask());
        out.write(1); // is_cidr = 1
        out.write(4); // address length
        int addr = v4.address();
        out.write((addr >>> 24) & 0xFF);
        out.write((addr >>> 16) & 0xFF);
        out.write((addr >>> 8) & 0xFF);
        out.write(addr & 0xFF);
      }
      case Inet.V6 v6 -> {
        out.write(3); // IPv6 address family
        out.write(v6.netmask());
        out.write(1); // is_cidr = 1
        out.write(16); // address length
        for (int w : new int[] {v6.w1(), v6.w2(), v6.w3(), v6.w4()}) {
          out.write((w >>> 24) & 0xFF);
          out.write((w >>> 16) & 0xFF);
          out.write((w >>> 8) & 0xFF);
          out.write(w & 0xFF);
        }
      }
    }
  }

  @Override
  public Inet decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    if (length < 4) {
      throw new Codec.DecodingException("Binary cidr too short: " + length);
    }
    byte af = buf.get();
    byte netmask = buf.get();
    buf.get(); // is_cidr flag, ignored
    int addrLen = Byte.toUnsignedInt(buf.get());
    return switch (af) {
      case 2 -> {
        if (addrLen != 4 || length != 8) {
          throw new Codec.DecodingException("Binary IPv4 cidr length mismatch");
        }
        yield new Inet.V4(buf.getInt(), netmask);
      }
      case 3 -> {
        if (addrLen != 16 || length != 20) {
          throw new Codec.DecodingException("Binary IPv6 cidr length mismatch");
        }
        yield new Inet.V6(buf.getInt(), buf.getInt(), buf.getInt(), buf.getInt(), netmask);
      }
      default -> throw new Codec.DecodingException("Unknown cidr address family: " + af);
    };
  }

  @Override
  public Inet random(Random r, int size) {
    if (r.nextBoolean()) {
      byte mask = (byte) r.nextInt(0, 33);
      int addr = r.nextInt();
      // Zero out host bits for valid CIDR
      int maskBits = mask & 0xff;
      if (maskBits == 0) {
        addr = 0;
      } else if (maskBits < 32) {
        addr &= (-1 << (32 - maskBits));
      }
      return new Inet.V4(addr, mask);
    } else {
      byte mask = (byte) r.nextInt(0, 129);
      int[] words = {r.nextInt(), r.nextInt(), r.nextInt(), r.nextInt()};
      // Zero out host bits
      int maskBits = mask & 0xff;
      for (int i = 0; i < 4; i++) {
        int wordStart = i * 32;
        if (maskBits >= wordStart + 32) {
          // Entire word is network bits, keep as-is
        } else if (maskBits <= wordStart) {
          // Entire word is host bits, zero it
          words[i] = 0;
        } else {
          // Partial: zero out the host bits in this word
          int bitsToKeep = maskBits - wordStart;
          words[i] = (bitsToKeep == 0) ? 0 : (words[i] & (-1 << (32 - bitsToKeep)));
        }
      }
      return new Inet.V6(words[0], words[1], words[2], words[3], mask);
    }
  }
}
