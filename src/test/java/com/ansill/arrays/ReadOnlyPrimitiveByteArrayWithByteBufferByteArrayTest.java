package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@DisplayName("PrimitiveByteArray - ReadOnly test with ByteBufferByteArray implementation")
public
class ReadOnlyPrimitiveByteArrayWithByteBufferByteArrayTest extends ReadOnlyPrimitiveByteArrayWithControlByteArrayTest{

  @Nonnull
  @Override
  public ReadableWritableByteArray createControlReadableWritable(long size){
    return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
  }
}
