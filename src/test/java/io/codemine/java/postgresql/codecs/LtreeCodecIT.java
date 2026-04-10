package io.codemine.java.postgresql.codecs;

public class LtreeCodecIT extends CodecITBase<Ltree> {

  static {
    try (var connection = jdbcPool.getConnection();
        var statement = connection.createStatement()) {
      statement.execute("CREATE EXTENSION IF NOT EXISTS ltree");
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public LtreeCodecIT() {
    super(Codec.LTREE, Ltree.class);
  }
}
