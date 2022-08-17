package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.WriteOnlyByteArray64BitTest;
import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@DisplayName("MultipleByteArray - WriteOnly test")
public
class WriteOnlyMultipleByteArrayTest implements WriteOnlyByteArray64BitTest, MultipleByteArrayTest{

  @Nonnull
  @Override
  public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
    return createTestWriteOnlyByteArray(size, (int) (3232 + size));
  }

  @Nonnull
  private WriteOnlyByteArray createTestWriteOnlyByteArray(long size, int seed){

    // Seed RNG using size
    Random random = new Random(seed);

    // We want around 5 chunks if possible
    int chunkSize = (int) (size / 5);
    if(chunkSize < 2) chunkSize = (int) size;

    // Build the list
    List<ReadableWritableByteArray> bytearrays = new ArrayList<>();
    long runningSize = size;
    while(runningSize > 0){

      // Set up size
      long innerSize = Long.min(random.nextInt(chunkSize) + 1, runningSize);

      // 25% chance of creating multiplebytearray
      if(random.nextFloat() <= 0.25f){

        // Add to the list
        bytearrays.add(createTestReadableWritableByteArray(innerSize, seed));

      }else{

        // Add to the list
        bytearrays.add(new TestOnlyByteArray(innerSize));

      }

      // Update running size
      runningSize -= innerSize;
    }

    return new ReadableWritableMultipleByteArray(bytearrays).toWriteOnly();
  }

  @Override
  public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

    // Check if wrapper, unwrap it
    if(testByteArray instanceof WriteOnlyByteArrayWrapper){
      testByteArray = ((WriteOnlyByteArrayWrapper) testByteArray).original;
    }

    // Check if it's ours
    if(!(testByteArray instanceof ReadableWritableMultipleByteArray)){
      throw new IllegalArgumentException("Not multiplebytearray");
    }

    // Read
    try{
      return ((ReadableWritableMultipleByteArray) testByteArray).readByte(byteIndex);
    }catch(ByteArrayIndexOutOfBoundsException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isReadableWritableOK(){
    return false;
  }

  @Test
  @Override
  public void testToString(){
    WriteOnlyByteArray64BitTest.super.testToString();
  }
}
