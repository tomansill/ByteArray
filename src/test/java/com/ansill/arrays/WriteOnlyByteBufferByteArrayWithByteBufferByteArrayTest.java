package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;

import javax.annotation.Nonnull;

@DisplayName("ByteBufferByteArray - WriteOnly test with PrimitiveByteArray implementation")
public
class WriteOnlyByteBufferByteArrayWithByteBufferByteArrayTest
  extends WriteOnlyByteBufferByteArrayWithControlByteArrayTest{

  @Nonnull
  @Override
  public ReadableWritableByteArray createControlReadableWritable(long size){
    return new PrimitiveByteArray(new byte[(int) size]);
  }
}
