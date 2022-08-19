package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import test.ByteArrayTest;
import test.ReadOnlyByteArray64BitTest;
import test.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.ReadableWritableByteArray64BitTest;
import test.WriteOnlyByteArray64BitTest;
import test.WriteOnlyByteArrayWithOtherByteArrayTest;
import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@DisplayName("MultipleByteArray Test Suite")
public class MultipleByteArrayNestedTest{

  @DisplayName("MultipleByteArray tests")
  public interface MultipleByteArrayTest extends ByteArrayTest{

    @Nonnull
    static ReadableWritableByteArray createReadableWritableByteArray(long size, int seed){

      // Seed RNG
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

        // 25% chance that it'll be inner multiplebytearray
        if(random.nextFloat() <= 0.25){

          // Add to the list
          bytearrays.add(createReadableWritableByteArray(innerSize, random.nextInt()));

        }else{

          // Add to the list
          bytearrays.add(new TestOnlyByteArray(innerSize));

        }

        // Update running size
        runningSize -= innerSize;
      }

      return new ReadableWritableMultipleByteArray(bytearrays);
    }

    @Override
    default void cleanTestByteArray(@Nonnull ByteArray byteArray){

      // Save size
      long size = byteArray.size();

      // Do a recursive clean
      TestUtility.clean(TestUtility.UNSAFE, byteArray);

      // Trigger a GC to blast away any unused stuff
      System.gc();

      // Log it if actually big
      if(size >= (Integer.MAX_VALUE * 0.5)) System.out.println("Cleared away " + size + "B");
    }

    @Nonnull
    default ReadableWritableByteArray createTestReadableWritableByteArray(long size, int seed){
      return createReadableWritableByteArray(size, seed);
    }

  }

  @Nested
  @DisplayName("WriteOnly test with control ByteArray implementation")
  public class WriteOnlyMultipleByteArrayWithControlByteArrayTest extends WriteOnlyMultipleByteArrayTest implements
    WriteOnlyByteArrayWithOtherByteArrayTest{

  }

  @Nested
  @DisplayName("WriteOnly test with ByteBufferByteArray implementation")
  public class WriteOnlyMultipleByteArrayWithByteBufferByteArrayTest extends WriteOnlyMultipleByteArrayTest implements
    WriteOnlyByteArrayWithOtherByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
    }
  }

  @Nested
  @DisplayName("WriteOnly test with PrimitiveByteArray implementation")
  public class WriteOnlyMultipleByteArrayWithPrimitiveByteArrayTest extends WriteOnlyMultipleByteArrayTest implements
    WriteOnlyByteArrayWithOtherByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new PrimitiveByteArray(new byte[(int) size]);
    }
  }

  @Nested
  @DisplayName("WriteOnly test with self implementation")
  public class WriteOnlyMultipleByteArrayWithSelfTest extends WriteOnlyMultipleByteArrayTest implements
    WriteOnlyByteArrayWithOtherByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return MultipleByteArrayNestedTest.MultipleByteArrayTest.createReadableWritableByteArray(
        size,
        (int) (size + 232)
      );
    }
  }

  @Nested
  @DisplayName("WriteOnly test")
  public class WriteOnlyMultipleByteArrayTest implements WriteOnlyByteArray64BitTest, MultipleByteArrayTest{

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

  @Nested
  @DisplayName("ReadOnly test with control ByteArray implementation")
  public class ReadOnlyMultipleByteArrayWithControlByteArrayTest extends ReadOnlyMultipleByteArrayTest implements
    ReadOnlyByteArrayWithOtherByteArrayTest{

  }

  @Nested
  @DisplayName("ReadOnly test with ByteBufferByteArray implementation")
  public class ReadOnlyMultipleByteArrayWithByteBufferByteArrayTest extends ReadOnlyMultipleByteArrayTest implements
    ReadOnlyByteArrayWithOtherByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
    }

  }

  @Nested
  @DisplayName("ReadOnly test with PrimitiveByteArray implementation")
  public class ReadOnlyMultipleByteArrayWithPrimitiveByteArrayTest extends ReadOnlyMultipleByteArrayTest implements
    ReadOnlyByteArrayWithOtherByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new PrimitiveByteArray(new byte[(int) size]);
    }
  }

  @Nested
  @DisplayName("ReadOnly test with self implementation")
  public class ReadOnlyMultipleByteArrayWithSelfTest extends ReadOnlyMultipleByteArrayTest implements
    ReadOnlyByteArrayWithOtherByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return MultipleByteArrayNestedTest.MultipleByteArrayTest.createReadableWritableByteArray(
        size,
        (int) (size + 232)
      );
    }
  }

  @Nested
  @DisplayName("ReadOnly test")
  public class ReadOnlyMultipleByteArrayTest implements ReadOnlyByteArray64BitTest, MultipleByteArrayTest{

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

  @Nested
  @DisplayName("ReadableWritable test")
  public class ReadableWritableMultipleByteArrayTest
    implements ReadableWritableByteArray64BitTest, MultipleByteArrayTest{

    @Override
    public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof ReadableWritableMultipleByteArray)) throw new IllegalArgumentException(
        "Not multiplebytearray");

      // Read
      try{
        return ((ReadableWritableMultipleByteArray) testByteArray).readByte(byteIndex);
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
      if(testByteArray instanceof ReadOnlyMultipleByteArray){

        // Get the array
        Map.Entry<Long,ReadOnlyByteArray> entry = ((ReadOnlyMultipleByteArray) testByteArray).indexMap.floorEntry(
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

    @Nonnull
    @Override
    public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
      return createTestReadableWritableByteArray(size, 34343);
    }

    @Override
    public boolean isReadableWritableOK(){
      return true;
    }
  }
}
