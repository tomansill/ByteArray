package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.ByteArrayTest;
import test.ReadOnlyByteArrayTest;
import test.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.ReadableWritableByteArrayTest;
import test.WriteOnlyByteArrayTest;
import test.WriteOnlyByteArrayWithOtherByteArrayTest;
import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@DisplayName("MultipleByteArray tests")
public interface MultipleByteArrayTest extends ByteArrayTest{

  @Override
  default boolean is64BitAddressingSupported(){
    return true;
  }

  @DisplayName("MultipleByteArray - ReadableWritable test")
  class ReadableWritableTest implements ReadableWritableByteArrayTest, MultipleByteArrayTest{

    @Override
    public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof MultipleByteArray.ReadableWritable)) throw new IllegalArgumentException(
        "Not multiplebytearray");

      // Read
      try{
        return ((MultipleByteArray.ReadableWritable) testByteArray).readByte(byteIndex);
      }catch(ByteArrayIndexOutOfBoundsException e){
        throw new RuntimeException(e);
      }
    }

    @Override
    public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check type
      if(testByteArray instanceof MultipleByteArray.ReadOnly){

        // Get the array
        Map.Entry<Long,ReadOnlyByteArray> entry = ((MultipleByteArray.ReadOnly) testByteArray).indexMap.floorEntry(
          byteIndex);

        // Extract
        long start = entry.getKey();
        ReadOnlyByteArray byteArray = entry.getValue();

        // ByteArray should be TestOnlyByteArray
        if(!(byteArray instanceof TestOnlyByteArray)) throw new IllegalArgumentException(
          "Backing bytearray is not TestOnlyByteArray");

        // Write
        try{
          ((TestOnlyByteArray) byteArray).writeByte(byteIndex - start, value);
        }catch(ByteArrayIndexOutOfBoundsException e){
          throw new RuntimeException(e);
        }

      }else if(testByteArray instanceof MultipleByteArray.ReadableWritable){

        // Get the array
        Map.Entry<Long,ReadableWritableByteArray> entry = ((MultipleByteArray.ReadableWritable) testByteArray).indexMap.floorEntry(
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

    @Nonnull
    @Override
    public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
      return createTestReadableWritableByteArray(size, 34343);
    }

    @Nonnull
    private ReadableWritableByteArray createTestReadableWritableByteArray(long size, int seed){

      // Seed RNG using size
      Random random = new Random(seed);

      // Build the list
      List<ReadableWritableByteArray> bytearrays = new ArrayList<>();
      long runningSize = size;
      while(runningSize > 0){

        // Set up size
        long innerSize = Long.min(random.nextInt(20) + 1, runningSize);

        // 25% chance that it'll be inner multiplebytearray
        if(random.nextFloat() <= 0.25){

          // Add to the list
          bytearrays.add(createTestReadableWritableByteArray(innerSize, random.nextInt()));

        }else{

          // Add to the list
          bytearrays.add(new TestOnlyByteArray(innerSize));

        }

        // Update running size
        runningSize -= innerSize;
      }

      return new MultipleByteArray.ReadableWritable(bytearrays);
    }

    @Override
    public boolean isReadableWritableOK(){
      return true;
    }
  }

  @DisplayName("MultipleByteArray - ReadOnly test")
  class ReadOnlyTest implements ReadOnlyByteArrayTest, MultipleByteArrayTest{

    @Nonnull
    @Override
    public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
      return createTestReadOnlyByteArray(size, 24243);
    }

    @Nonnull
    private ReadOnlyByteArray createTestReadOnlyByteArray(long size, int seed){

      // Seed RNG using size
      Random random = new Random(seed);

      // Build the list
      List<ReadOnlyByteArray> bytearrays = new ArrayList<>();
      long runningSize = size;
      while(runningSize > 0){

        // Set up size
        long innerSize = Long.min(random.nextInt(20) + 1, runningSize);

        // 25% chance of inner multiplebytearray
        if(innerSize != 1 && random.nextFloat() <= 0.25){

          // Add to the list
          bytearrays.add(createTestReadOnlyByteArray(innerSize, random.nextInt()));

        }else{

          // Add to the list
          bytearrays.add(new TestOnlyByteArray(innerSize).toReadOnly());

        }

        // Update running size
        runningSize -= innerSize;
      }

      return new MultipleByteArray.ReadOnly(bytearrays);
    }

    @Override
    public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check type
      if(testByteArray instanceof MultipleByteArray.ReadOnly){

        // Get the array
        Map.Entry<Long,ReadOnlyByteArray> entry = ((MultipleByteArray.ReadOnly) testByteArray).indexMap.floorEntry(
          byteIndex);

        // Extract
        long start = entry.getKey();
        ReadOnlyByteArray byteArray = entry.getValue();
        byteIndex -= start;

        // ByteArray should be TestOnlyByteArray and variants
        if(byteArray instanceof TestOnlyByteArray){
          byte[][] data = ((TestOnlyByteArray) byteArray).data;
          long startba = ((TestOnlyByteArray) byteArray).start;
          byteIndex += startba;
          for(byte[] ba : data){
            if(byteIndex >= ba.length) byteIndex -= ba.length;
            else{
              ba[(int) byteIndex] = value;
              return;
            }
          }
        }else if(byteArray instanceof TestOnlyByteArray.ReadOnly){
          byte[][] data = ((TestOnlyByteArray.ReadOnly) byteArray).original.data;
          long startba = ((TestOnlyByteArray.ReadOnly) byteArray).original.start;
          byteIndex += startba;
          for(byte[] ba : data){
            if(byteIndex >= ba.length) byteIndex -= ba.length;
            else{
              ba[(int) byteIndex] = value;
              return;
            }
          }
        }else throw new IllegalArgumentException("Not testonlybytearray");

      }else if(testByteArray instanceof MultipleByteArray.ReadableWritable){

        // Get the array
        Map.Entry<Long,ReadableWritableByteArray> entry = ((MultipleByteArray.ReadableWritable) testByteArray).indexMap.floorEntry(
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

  @DisplayName("MultipleByteArray - WriteOnly test")
  class WriteOnlyTest implements WriteOnlyByteArrayTest, MultipleByteArrayTest{

    @Nonnull
    @Override
    public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
      return createTestWriteOnlyByteArray(size, 3232);
    }

    @Nonnull
    private WriteOnlyByteArray createTestWriteOnlyByteArray(long size, int seed){

      // Seed RNG using size
      Random random = new Random(seed);

      // Build the list
      List<ReadableWritableByteArray> bytearrays = new ArrayList<>();
      long runningSize = size;
      while(runningSize > 0){

        // Set up size
        long innerSize = Long.min(random.nextInt(20) + 1, runningSize);

        // 25% chance of creating multiplebytearray
        if(random.nextFloat() <= 0.25f){

          // Add to the list
          bytearrays.add((ReadableWritableByteArray) createTestByteArray(innerSize));

        }else{

          // Add to the list
          bytearrays.add(new TestOnlyByteArray(innerSize));

        }

        // Update running size
        runningSize -= innerSize;
      }

      return new MultipleByteArray.ReadableWritable(bytearrays).toWriteOnly();
    }

    @Override
    public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof WriteOnlyByteArrayWrapper){
        testByteArray = ((WriteOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof MultipleByteArray.ReadableWritable)){
        throw new IllegalArgumentException("Not multiplebytearray");
      }

      // Read
      try{
        return ((MultipleByteArray.ReadableWritable) testByteArray).readByte(byteIndex);
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
      WriteOnlyByteArrayTest.super.testToString();
    }
  }

  @DisplayName("MultipleByteArray - WriteOnly test with control ByteArray implementation")
  class WriteOnlyWithControlByteArrayTest extends WriteOnlyTest implements WriteOnlyByteArrayWithOtherByteArrayTest{

  }

  @DisplayName("MultipleByteArray - ReadOnly test with control ByteArray implementation")
  class ReadOnlyWithControlByteArrayTest extends ReadOnlyTest implements ReadOnlyByteArrayWithOtherByteArrayTest{

  }
}
