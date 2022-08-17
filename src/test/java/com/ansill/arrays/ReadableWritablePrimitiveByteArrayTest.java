package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import test.ReadableWritableByteArrayTest;

import javax.annotation.Nonnull;

@DisplayName("PrimitiveByteArray - ReadableWritable test")
public
class ReadableWritablePrimitiveByteArrayTest implements ReadableWritableByteArrayTest, PrimitiveByteArrayTest{

  @Override
  public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

    // Check if wrapper, unwrap it
    if(testByteArray instanceof ReadOnlyByteArrayWrapper){
      testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
    }

    // Check if it's ours
    if(!(testByteArray instanceof PrimitiveByteArray)) throw new IllegalArgumentException("Not primitive byte array");

    // Read
    return ((PrimitiveByteArray) testByteArray).data[(int) byteIndex];
  }

  @Override
  public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

    // Check if wrapper, unwrap it
    if(testByteArray instanceof ReadOnlyByteArrayWrapper){
      testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
    }

    // Check if it's ours
    if(!(testByteArray instanceof PrimitiveByteArray)) throw new IllegalArgumentException("Not primitive byte array");

    // Update
    ((PrimitiveByteArray) testByteArray).data[(int) byteIndex] = value;
  }

  @Nonnull
  @Override
  public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
    return new PrimitiveByteArray(new byte[(int) size]);
  }

  @Override
  public boolean isReadableWritableOK(){
    return true;
  }
}
