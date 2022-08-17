package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;

import javax.annotation.Nonnull;

@DisplayName("PrimitiveByteArray - WriteOnly test with PrimitiveByteArray implementation")
public
class WriteOnlyPrimitiveByteArrayWithSelfTest extends WriteOnlyPrimitiveByteArrayWithControlByteArrayTest{

  @Nonnull
  @Override
  public ReadableWritableByteArray createControlReadableWritable(long size){
    return new PrimitiveByteArray(new byte[(int) size]);
  }
}
