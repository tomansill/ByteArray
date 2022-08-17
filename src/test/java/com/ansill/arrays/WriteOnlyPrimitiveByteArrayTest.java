package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.WriteOnlyByteArrayTest;

import javax.annotation.Nonnull;

@DisplayName("PrimitiveByteArray - WriteOnly test")
public
class WriteOnlyPrimitiveByteArrayTest implements WriteOnlyByteArrayTest, PrimitiveByteArrayTest{

  @Nonnull
  @Override
  public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
    return new PrimitiveByteArray(new byte[(int) size]).toWriteOnly();
  }

  @Override
  public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

    // Check if writeonly wrapper, unwrap it
    if(testByteArray instanceof WriteOnlyByteArrayWrapper){
      testByteArray = ((WriteOnlyByteArrayWrapper) testByteArray).original;
    }

    // Check if it's ours
    if(!(testByteArray instanceof PrimitiveByteArray)) throw new IllegalArgumentException("Not primitive byte array");

    // Read
    return ((PrimitiveByteArray) testByteArray).data[(int) byteIndex];
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
