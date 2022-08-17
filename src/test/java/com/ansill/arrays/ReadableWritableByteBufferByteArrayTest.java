package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import test.ReadableWritableByteArrayTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@DisplayName("ByteBufferByteArray - ReadableWritable test")
public
class ReadableWritableByteBufferByteArrayTest implements ReadableWritableByteArrayTest, ByteBufferByteArrayTest{

  @Override
  public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

    // Check if wrapper, unwrap it
    if(testByteArray instanceof ReadOnlyByteArrayWrapper){
      testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
    }

    // Check if it's ours
    if(!(testByteArray instanceof ByteBufferByteArray))
      throw new IllegalArgumentException("Not primitive byte array");

    // Read
    return ((ByteBufferByteArray) testByteArray).data.get((int) byteIndex);
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

  @Nonnull
  @Override
  public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
    return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
  }

  @Override
  public boolean isReadableWritableOK(){
    return true;
  }
}
