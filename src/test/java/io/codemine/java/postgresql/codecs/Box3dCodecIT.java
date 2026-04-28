package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

class Box3dCodecIT extends CodecITBase<Box3d> {
  Box3dCodecIT() {
    super(Codec.BOX3D, Box3d.class);
  }
}
