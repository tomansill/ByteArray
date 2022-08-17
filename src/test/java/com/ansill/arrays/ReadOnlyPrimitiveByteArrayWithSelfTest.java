package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;

import javax.annotation.Nonnull;

@DisplayName("PrimitiveByteArray - ReadOnly test with PrimitiveByteArray implementation")
public
class ReadOnlyPrimitiveByteArrayWithSelfTest extends ReadOnlyPrimitiveByteArrayWithControlByteArrayTest{

  @Nonnull
  @Override
  public ReadableWritableByteArray createControlReadableWritable(long size){
    return new PrimitiveByteArray(new byte[(int) size]);
  }
}
