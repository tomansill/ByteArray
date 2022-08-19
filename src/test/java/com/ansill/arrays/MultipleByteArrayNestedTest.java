package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import test.BaseByteArrayTest;
import test.arrays.TestOnlyByteArray;
import test.other.ReadOnlyByteArrayWithOtherByteArray64BitTest;
import test.other.ReadableWritableByteArrayWithOtherByteArray64BitTest;
import test.other.WriteOnlyByteArrayWithOtherByteArray64BitTest;
import test.self.SelfReadOnlyByteArray64BitTest;
import test.self.SelfReadableWritableByteArray64BitTest;
import test.self.SelfWriteOnlyByteArray64BitTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

@DisplayName("MultipleByteArray Test Suite")
public class MultipleByteArrayNestedTest{

  @Nonnull
  public static ReadableWritableByteArray createReadableWritableByteArray(long size, int seed){

    // Seed RNG
    var random = new Random(seed);

    // We want around 5 chunks if possible
    int chunkSize = (int) (size / 5);
    if(chunkSize < 2) chunkSize = (int) size;

    // Build the list
    var bytearrays = new ArrayList<ReadableWritableByteArray>();
    long runningSize = size;
    while(runningSize > 0){

      // Set up size
      long innerSize = Long.min(random.nextInt(chunkSize) + 1, runningSize);

      // 25% chance that it'll be inner multiplebytearray
      if(random.nextFloat() <= 0.25) bytearrays.add(createReadableWritableByteArray(innerSize, random.nextInt()));
      else bytearrays.add(new TestOnlyByteArray(innerSize));

      // Update running size
      runningSize -= innerSize;
    }

    // Return as RW
    return new ReadableWritableMultipleByteArray(bytearrays);
  }

  public abstract static class MultipleByteArrayTest implements BaseByteArrayTest{

    @Nonnull
    public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
      return createReadableWritableByteArray(size, 34343);
    }

    @Override
    public void cleanTestByteArray(@Nonnull ByteArray byteArray){

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
    public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
      return createTestReadOnlyByteArray(size, (int) ("3242".hashCode() + size));
    }

    @Nonnull
    public ReadOnlyByteArray createTestReadOnlyByteArray(long size, int seed){

      // Seed RNG
      var random = new Random(seed);

      // We want around 5 chunks if possible
      int chunkSize = (int) (size / 5);
      if(chunkSize < 2) chunkSize = (int) size;

      // Build the list
      var bytearrays = new ArrayList<ReadOnlyByteArray>();
      long runningSize = size;
      while(runningSize > 0){

        // Set up size
        long innerSize = Long.min(random.nextInt(chunkSize) + 1, runningSize);

        // 33% chance that it'll be inner multiplebytearray
        if(random.nextFloat() <= 0.33){
          if(random.nextBoolean()) bytearrays.add(createReadableWritableByteArray(innerSize, random.nextInt()));
          else bytearrays.add(createTestReadOnlyByteArray(innerSize, random.nextInt()));
        }else{

          // Create testonly
          var testOnly = new TestOnlyByteArray(innerSize);

          // Chance that it will be actually be converted to readonly
          if(random.nextBoolean()) bytearrays.add(testOnly.toReadOnly());
          else bytearrays.add(new TestOnlyByteArray(innerSize));
        }

        // Update running size
        runningSize -= innerSize;
      }

      // Create and return
      return new ReadOnlyMultipleByteArray(bytearrays);
    }

    public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check type
      if(testByteArray instanceof ReadOnlyMultipleByteArray){

        // Get the array
        var entry = ((ReadOnlyMultipleByteArray) testByteArray).indexMap.floorEntry(byteIndex);

        // Extract
        long start = entry.getKey();
        ReadOnlyByteArray byteArray = entry.getValue();
        byteIndex -= start;

        // ByteArray should be TestOnlyByteArray and variants
        if(byteArray instanceof TestOnlyByteArray){
          var data = ((TestOnlyByteArray) byteArray).data;
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
          var data = ((TestOnlyByteArray.ReadOnly) byteArray).original.data;
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
        var entry = ((ReadableWritableMultipleByteArray) testByteArray).indexMap.floorEntry(byteIndex);

        // Extract
        long start = entry.getKey();
        var byteArray = entry.getValue();

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

    @Nonnull
    public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
      return createTestReadableWritableByteArray(size).toWriteOnly();
    }

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
  }

  @Nested
  @DisplayName("ReadOnly Tests")
  public class ReadOnlyTests{

    @Nested
    @DisplayName("ReadOnly test with control ByteArray implementation")
    public class ReadOnlyMultipleByteArrayWithControlByteArrayTest extends ReadOnlyMultipleByteArrayTest implements
      ReadOnlyByteArrayWithOtherByteArray64BitTest{

    }

    @Nested
    @DisplayName("ReadOnly test with ByteBufferByteArray implementation")
    public class ReadOnlyMultipleByteArrayWithByteBufferByteArrayTest
      extends ReadOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }

    }

    @Nested
    @DisplayName("ReadOnly test with PrimitiveByteArray implementation")
    public class ReadOnlyMultipleByteArrayWithPrimitiveByteArrayTest
      extends ReadOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }

    @Nested
    @DisplayName("ReadOnly test with self implementation")
    public class ReadOnlyMultipleByteArrayWithSelfTest extends ReadOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return createReadableWritableByteArray(
          size,
          (int) (size + 232)
        );
      }
    }

    @Nested
    @DisplayName("ReadOnly test")
    public class ReadOnlyMultipleByteArrayTest extends MultipleByteArrayTest implements SelfReadOnlyByteArray64BitTest{

    }
  }

  @Nested
  @DisplayName("WriteOnly Tests")
  public class WriteOnlyTests{

    @Nested
    @DisplayName("WriteOnly test with control ByteArray implementation")
    public class WriteOnlyMultipleByteArrayWithControlByteArrayTest extends WriteOnlyMultipleByteArrayTest implements
      WriteOnlyByteArrayWithOtherByteArray64BitTest{

    }

    @Nested
    @DisplayName("WriteOnly test with ByteBufferByteArray implementation")
    public class WriteOnlyMultipleByteArrayWithByteBufferByteArrayTest
      extends WriteOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }

    @Nested
    @DisplayName("WriteOnly test with PrimitiveByteArray implementation")
    public class WriteOnlyMultipleByteArrayWithPrimitiveByteArrayTest
      extends WriteOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }

    @Nested
    @DisplayName("WriteOnly test with self implementation")
    public class WriteOnlyMultipleByteArrayWithSelfTest extends WriteOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return createReadableWritableByteArray(
          size,
          (int) (size + 232)
        );
      }
    }

    @Nested
    @DisplayName("WriteOnly test")
    public class WriteOnlyMultipleByteArrayTest extends MultipleByteArrayTest
      implements SelfWriteOnlyByteArray64BitTest{

    }
  }

  @Nested
  @DisplayName("ReadableWritable Tests")
  public class ReadableWritableTests{

    @Nested
    @DisplayName("ReadableWritable test with control ByteArray implementation")
    public class ReadableWritableMultipleByteArrayWithControlByteArrayTest
      extends ReadableWritableTests.ReadableWritableMultipleByteArrayTest
      implements ReadableWritableByteArrayWithOtherByteArray64BitTest{

      @Nonnull
      public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
        return createTestReadableWritableByteArray(size);
      }

      @Nonnull
      public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
        return createTestReadableWritableByteArray(size);
      }

      @Override
      public boolean isReadableWritableOK(){
        return true;
      }
    }

    @Nested
    @DisplayName("ReadableWritable test with ByteBufferByteArray implementation")
    public class ReadableWritableMultipleByteArrayWithByteBufferByteArrayTest
      extends ReadableWritableTests.ReadableWritableMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }

    }

    @Nested
    @DisplayName("ReadableWritable test with PrimitiveByteArray implementation")
    public class ReadableWritableMultipleByteArrayWithPrimitiveByteArrayTest
      extends ReadableWritableTests.ReadableWritableMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }

    @Nested
    @DisplayName("ReadableWritable test with self implementation")
    public class ReadableWritableMultipleByteArrayWithSelfTest
      extends ReadableWritableTests.ReadableWritableMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return createReadableWritableByteArray(
          size,
          (int) (size + 232)
        );
      }
    }

    @Nested
    @DisplayName("ReadableWritable test")
    public class ReadableWritableMultipleByteArrayTest extends MultipleByteArrayTest
      implements SelfReadableWritableByteArray64BitTest{

      @Nonnull
      public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
        return createTestReadableWritableByteArray(size);
      }

      @Nonnull
      public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
        return createTestReadableWritableByteArray(size);
      }

      @Override
      public boolean isReadableWritableOK(){
        return true;
      }
    }
  }
}
