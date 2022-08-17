package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;

import javax.annotation.Nonnull;

@DisplayName("PrimitiveByteArray - WriteOnly test with MultipleByteArray implementation")
public
class WriteOnlyPrimitiveByteArrayWithMultipleByteArrayTest
  extends WriteOnlyPrimitiveByteArrayWithControlByteArrayTest{

  @Nonnull
  @Override
  public ReadableWritableByteArray createControlReadableWritable(long size){
    return MultipleByteArrayTest.createReadableWritableByteArray(size, (int) (size + 232));
  }
}
