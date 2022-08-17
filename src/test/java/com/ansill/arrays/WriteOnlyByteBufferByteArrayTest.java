package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.WriteOnlyByteArrayTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@DisplayName("ByteBufferByteArray - WriteOnly test")
public
class WriteOnlyByteBufferByteArrayTest implements WriteOnlyByteArrayTest, ByteBufferByteArrayTest{

  @Nonnull
  @Override
  public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
    return new ByteBufferByteArray(ByteBuffer.allocate((int) size)).toWriteOnly();
  }

  @Override
  public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

    // Check if writeonly wrapper, unwrap it
    if(testByteArray instanceof WriteOnlyByteArrayWrapper){
      testByteArray = ((WriteOnlyByteArrayWrapper) testByteArray).original;
    }

    // Check if it's ours
    if(!(testByteArray instanceof ByteBufferByteArray))
      throw new IllegalArgumentException("Not primitive byte array");

    // Read
    return ((ByteBufferByteArray) testByteArray).data.get((int) byteIndex);
  }

  @Override
  public boolean isReadableWritableOK(){
    return false;
  }

  @Test
  @Override
  public void testToString(){
    WriteOnlyByteArrayTest.super.testToString();
  }
}
