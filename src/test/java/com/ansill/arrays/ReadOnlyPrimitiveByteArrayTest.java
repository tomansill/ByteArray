package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import test.ReadOnlyByteArrayTest;

import javax.annotation.Nonnull;

@DisplayName("PrimitiveByteArray - ReadOnly test")
public
class ReadOnlyPrimitiveByteArrayTest implements ReadOnlyByteArrayTest, PrimitiveByteArrayTest{

  @Nonnull
  @Override
  public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
    return new PrimitiveByteArray(new byte[(int) size]).toReadOnly();
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

  @Override
  public boolean isReadableWritableOK(){
    return false;
  }
}
