package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;

import javax.annotation.Nonnull;

@DisplayName("PrimitiveByteArray - ReadOnly test with MultipleByteArray implementation")
public
class ReadOnlyPrimitiveByteArrayWithMultipleByteArrayTest extends ReadOnlyPrimitiveByteArrayWithControlByteArrayTest{

  @Nonnull
  @Override
  public ReadableWritableByteArray createControlReadableWritable(long size){
    return MultipleByteArrayTest.createReadableWritableByteArray(size, (int) (size + 232));
  }
}
