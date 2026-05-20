package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

/** Codec for PostGIS {@code geography} values. */
final class GeographyCodec implements Codec<Geography> {

  @Override
  public String name() {
    return "geography";
  }

  @Override
  public void encodeInText(StringBuilder sb, Geography value) {
    sb.append(GeometrySerde.encodeHex(GeometrySerde.encodeGeometry(value.value())));
  }

  @Override
  public Codec.ParsingResult<Geography> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    byte[] bytes = GeometrySerde.decodeHex(input.subSequence(offset, input.length()));
    return new Codec.ParsingResult<>(
        new Geography(GeometrySerde.decodeGeometry(ByteBuffer.wrap(bytes), bytes.length)),
        input.length());
  }

  @Override
  public void encodeInBinary(Geography value, ByteArrayOutputStream out) {
    out.writeBytes(GeometrySerde.encodeGeometry(value.value()));
  }

  @Override
  public Geography decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    return new Geography(GeometrySerde.decodeGeometry(buf, length));
  }

  @Override
  public Codec<List<Geography>> inDim() {
    return new ArrayCodec<>(this, ':');
  }

  @Override
  public Geography random(Random r, int size) {
    return GeometrySerde.randomGeography(r, size);
  }
}
