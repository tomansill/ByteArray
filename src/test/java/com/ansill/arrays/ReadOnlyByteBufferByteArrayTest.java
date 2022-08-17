package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import test.ReadOnlyByteArrayTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@DisplayName("ByteBufferByteArray - ReadOnly test")
public
class ReadOnlyByteBufferByteArrayTest implements ReadOnlyByteArrayTest, ByteBufferByteArrayTest{

  @Nonnull
  @Override
  public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
    return new ByteBufferByteArray(ByteBuffer.allocate((int) size)).toReadOnly();
  }

  @Override
  public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

    // Check if wrapper, unwrap it
    if(testByteArray instanceof ReadOnlyByteArrayWrapper){
      testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
    }

    // Check if it's ours
    if(!(testByteArray instanceof ByteBufferByteArray))
      throw new IllegalArgumentException("Not primitive byte array");

    // Update
    ((ByteBufferByteArray) testByteArray).data.put((int) byteIndex, value);
  }

  @Override
  public boolean isReadableWritableOK(){
    return false;
  }
}
