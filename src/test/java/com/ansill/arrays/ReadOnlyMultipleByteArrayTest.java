package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import test.ReadOnlyByteArray64BitTest;
import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@DisplayName("MultipleByteArray - ReadOnly test")
public
class ReadOnlyMultipleByteArrayTest implements ReadOnlyByteArray64BitTest, MultipleByteArrayTest{

  @Nonnull
  @Override
  public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
    return createTestReadOnlyByteArray(size, 24243);
  }

  @Nonnull
  private ReadOnlyByteArray createTestReadOnlyByteArray(long size, int seed){

    // Seed RNG using size
    Random random = new Random(seed);

    // We want around 5 chunks if possible
    int chunkSize = (int) (size / 5);
    if(chunkSize < 2) chunkSize = (int) size;

    // Build the list
    List<ReadOnlyByteArray> bytearrays = new ArrayList<>();
    long runningSize = size;
    while(runningSize > 0){

      // Set up size
      long innerSize = Long.min(random.nextInt(chunkSize) + 1, runningSize);

      // 25% chance of inner multiplebytearray
      if(innerSize != 1 && random.nextFloat() <= 0.25){

        // Add to the list
        if(random.nextBoolean()) bytearrays.add(createTestReadOnlyByteArray(innerSize, random.nextInt()));
        else if(random.nextBoolean()){
          bytearrays.add(createTestReadableWritableByteArray(innerSize, random.nextInt()).toReadOnly());
        }else bytearrays.add(createTestReadableWritableByteArray(innerSize, random.nextInt()));

      }else{

        TestOnlyByteArray ba = new TestOnlyByteArray(innerSize);

        // Add to the list
        bytearrays.add(random.nextBoolean() ? ba : ba.toReadOnly());

      }

      // Update running size
      runningSize -= innerSize;
    }

    return new ReadOnlyMultipleByteArray(bytearrays);
  }

  @Override
  public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

    // Check if wrapper, unwrap it
    if(testByteArray instanceof ReadOnlyByteArrayWrapper){
      testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
    }

    // Check type
    if(testByteArray instanceof ReadOnlyMultipleByteArray){

      // Get the array
      Map.Entry<Long,ReadOnlyByteArray> entry = ((ReadOnlyMultipleByteArray) testByteArray).indexMap.floorEntry(
        byteIndex);

      // Extract
      long start = entry.getKey();
      ReadOnlyByteArray byteArray = entry.getValue();
      byteIndex -= start;

      // ByteArray should be TestOnlyByteArray and variants
      if(byteArray instanceof TestOnlyByteArray){
        ByteBuffer[] data = ((TestOnlyByteArray) byteArray).data;
        long startba = ((TestOnlyByteArray) byteArray).start;
        byteIndex += startba;
        for(ByteBuffer bb : data){
          int len = bb.limit() - bb.position();
          if(byteIndex >= len) byteIndex -= len;
          else{
            bb.put((int) byteIndex, value);
            return;
          }
        }
      }else if(byteArray instanceof TestOnlyByteArray.ReadOnly){
        ByteBuffer[] data = ((TestOnlyByteArray.ReadOnly) byteArray).original.data;
        long startba = ((TestOnlyByteArray.ReadOnly) byteArray).original.start;
        byteIndex += startba;
        for(ByteBuffer bb : data){
          int len = bb.limit() - bb.position();
          if(byteIndex >= len) byteIndex -= len;
          else{
            bb.put((int) byteIndex, value);
            return;
          }
        }
      }else throw new IllegalArgumentException("Not testonlybytearray");

    }else if(testByteArray instanceof ReadableWritableMultipleByteArray){

      // Get the array
      Map.Entry<Long,ReadableWritableByteArray> entry = ((ReadableWritableMultipleByteArray) testByteArray).indexMap.floorEntry(
        byteIndex);

      // Extract
      long start = entry.getKey();
      ReadableWritableByteArray byteArray = entry.getValue();

      // Write
      try{
        byteArray.writeByte(byteIndex - start, value);
      }catch(ByteArrayIndexOutOfBoundsException e){
        throw new RuntimeException(e);
      }

    }else throw new IllegalArgumentException("Not multiplebytearray");
  }

  @Override
  public boolean isReadableWritableOK(){
    return false;
  }
}
