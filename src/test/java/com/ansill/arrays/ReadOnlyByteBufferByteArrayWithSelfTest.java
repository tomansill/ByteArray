package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;

import javax.annotation.Nonnull;

@DisplayName("ByteBufferByteArray - ReadOnly test with PrimitiveByteArray implementation")
public
class ReadOnlyByteBufferByteArrayWithSelfTest extends ReadOnlyByteBufferByteArrayWithControlByteArrayTest{

  @Nonnull
  @Override
  public ReadableWritableByteArray createControlReadableWritable(long size){
    return new PrimitiveByteArray(new byte[(int) size]);
  }
}
