package com.ansill.arrays;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.BaseByteArrayTest;
import test.arrays.TestOnlyByteArray;
import test.other.ReadOnlyByteArrayWithOtherByteArray64BitTest;
import test.other.ReadableWritableByteArrayWithOtherByteArray64BitTest;
import test.other.WriteOnlyByteArrayWithOtherByteArray64BitTest;
import test.self.SelfByteArrayTest;
import test.self.SelfReadOnlyByteArray64BitTest;
import test.self.SelfReadableWritableByteArray64BitTest;
import test.self.SelfWriteOnlyByteArray64BitTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

@DisplayName("MultipleByteArray Test Suite")
public class MultipleByteArrayNestedTest{

  private static final Logger LOGGER = LoggerFactory.getLogger(MultipleByteArrayNestedTest.class);

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

    @Override
    public @NonNull String getExpectedToStringClassName() {
      return "ReadableWritableMultipleByteArray";
    }

    @Nonnull
    public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
      return createReadableWritableByteArray(size, 34343);
    }

    @Override
    public void writeTestByteArray(@NonNull ByteArray testByteArray, long byteIndex, byte value) {

      // Check if wrapper, unwrap it
      while(testByteArray instanceof  ReadOnlyByteArrayWrapper || testByteArray instanceof  WriteOnlyByteArrayWrapper){
        while(testByteArray instanceof ReadOnlyByteArrayWrapper){
          testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
        }
        while(testByteArray instanceof WriteOnlyByteArrayWrapper){
          testByteArray = ((WriteOnlyByteArrayWrapper) testByteArray).original;
        }
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
          ((TestOnlyByteArray) byteArray).writeByte(byteIndex, value);
        }else if(byteArray instanceof TestOnlyByteArray.ReadOnly){
          ((TestOnlyByteArray.ReadOnly) byteArray).original.writeByte(byteIndex, value);
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
    public byte readTestByteArray(@NonNull ByteArray testByteArray, long byteIndex) {

      // Check if wrapper, unwrap it
      while(testByteArray instanceof  ReadOnlyByteArrayWrapper || testByteArray instanceof  WriteOnlyByteArrayWrapper){
        while(testByteArray instanceof ReadOnlyByteArrayWrapper){
          testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
        }
        while(testByteArray instanceof WriteOnlyByteArrayWrapper){
          testByteArray = ((WriteOnlyByteArrayWrapper) testByteArray).original;
        }
      }

      // Check if it's ours
      if(testByteArray instanceof ReadableWritableMultipleByteArray){
        try{
          return ((ReadableWritableMultipleByteArray) testByteArray).readByte(byteIndex);
        }catch(ByteArrayIndexOutOfBoundsException e){
          throw new RuntimeException(e);
        }
      }
      if(testByteArray instanceof ReadOnlyMultipleByteArray){
        try{
          return ((ReadOnlyMultipleByteArray) testByteArray).readByte(byteIndex);
        }catch(ByteArrayIndexOutOfBoundsException e){
          throw new RuntimeException(e);
        }
      }

      // TestOnlyByteArray ends up here somehow
      if(testByteArray instanceof TestOnlyByteArray){
        try{
          return ((TestOnlyByteArray) testByteArray).readByte(byteIndex);
        }catch(ByteArrayIndexOutOfBoundsException e){
          throw new RuntimeException(e);
        }
      }

      // Fail
      throw new IllegalArgumentException("Not multiplebytearray: " + testByteArray.getClass().getName());
    }

    @Override
    public void cleanTestByteArray(@Nonnull ByteArray byteArray){

      // Save size
      long size = byteArray.size();

      // Do a recursive clean
      TestUtility.clean(byteArray);

      // Trigger a GC to blast away any unused stuff
      System.gc();

      // Log it if actually big
      if(size >= (Integer.MAX_VALUE * 0.5)) LOGGER.debug("Cleared away {}B", size);
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

    @Override
    public boolean isReadableWritableOK(){
      return false;
    }

    @Nonnull
    public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
      return createTestReadableWritableByteArray(size).toWriteOnly();
    }
  }

  @Nested
  @DisplayName("MultipleByteArray ReadOnly Tests")
  public class ReadOnlyTests{

    @Nested
    @DisplayName("MultipleByteArray ReadOnly test with control ByteArray implementation")
    public class ReadOnlyMultipleByteArrayWithControlByteArrayTest extends ReadOnlyMultipleByteArrayTest implements
      ReadOnlyByteArrayWithOtherByteArray64BitTest{

    }

    @Nested
    @DisplayName("MultipleByteArray ReadOnly test with ByteBufferByteArray implementation")
    public class ReadOnlyMultipleByteArrayWithByteBufferByteArrayTest
      extends ReadOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }

    }

    @Nested
    @DisplayName("MultipleByteArray ReadOnly test with PrimitiveByteArray implementation")
    public class ReadOnlyMultipleByteArrayWithPrimitiveByteArrayTest
      extends ReadOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }

    @Nested
    @DisplayName("MultipleByteArray ReadOnly test with self implementation")
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
    @DisplayName("MultipleByteArray ReadOnly test on self (MultipleByteArray)")
    public class ReadOnlyMultipleByteArrayTest extends MultipleByteArrayTest implements SelfReadOnlyByteArray64BitTest{

      @Override
      public @NonNull String getExpectedToStringClassName() {
        return "ReadOnlyMultipleByteArray";
      }

      @Override
      @TestFactory
      @DisplayName("Test toString()")
      public Iterable<DynamicTest> testToString() {
        return SelfByteArrayTest.generateTestsToString(
                getRNG(),
                "ReadOnlyByteArray",
                getToStringPerformanceLimit(),
                getExpectedToStringClassName(),
                this::createTestReadOnlyByteArray,
                this::writeTestByteArray,
                this::readTestByteArray,
                this::cleanTestByteArray,
                this.isReadableWritableOK()
        );
      }
    }
  }

  @Nested
  @DisplayName("MultipleByteArray WriteOnly Tests")
  public class WriteOnlyTests{

    @Nested
    @DisplayName("MultipleByteArray WriteOnly test with control ByteArray implementation")
    public class WriteOnlyMultipleByteArrayWithControlByteArrayTest extends MultipleByteArrayTest implements
      WriteOnlyByteArrayWithOtherByteArray64BitTest{

    }

    @Nested
    @DisplayName("MultipleByteArray WriteOnly test with ByteBufferByteArray implementation")
    public class WriteOnlyMultipleByteArrayWithByteBufferByteArrayTest
      extends WriteOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }

    @Nested
    @DisplayName("MultipleByteArray WriteOnly test with PrimitiveByteArray implementation")
    public class WriteOnlyMultipleByteArrayWithPrimitiveByteArrayTest
      extends WriteOnlyMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }

    @Nested
    @DisplayName("MultipleByteArray WriteOnly test with self implementation")
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
    @DisplayName("MultipleByteArray WriteOnly test on self (MultipleByteArray)")
    public class WriteOnlyMultipleByteArrayTest extends MultipleByteArrayTest
      implements SelfWriteOnlyByteArray64BitTest{

      @Override
      public @NonNull String getExpectedToStringClassName() {
        return "ReadableWritableMultipleByteArray";
      }

      @Override
      @TestFactory
      @DisplayName("Test toString()")
      public Iterable<DynamicTest> testToString() {
        return SelfByteArrayTest.generateTestsToString(
                getRNG(),
                "WriteOnlyByteArray",
                getToStringPerformanceLimit(),
                getExpectedToStringClassName(),
                this::createTestWriteOnlyByteArray,
                this::writeTestByteArray,
                this::readTestByteArray,
                this::cleanTestByteArray,
                this.isReadableWritableOK()
        );
      }
    }
  }

  @Nested
  @DisplayName("MultipleByteArray ReadableWritable Tests")
  public class ReadableWritableTests{

    @Nested
    @DisplayName("MultipleByteArray ReadableWritable test with control ByteArray implementation")
    public class ReadableWritableMultipleByteArrayWithControlByteArrayTest
      extends ReadableWritableTests.ReadableWritableMultipleByteArrayTest
      implements ReadableWritableByteArrayWithOtherByteArray64BitTest{

	    @Override
      public boolean isReadableWritableOK(){
        return true;
      }

    }

    @Nested
    @DisplayName("MultipleByteArray ReadableWritable test with ByteBufferByteArray implementation")
    public class ReadableWritableMultipleByteArrayWithByteBufferByteArrayTest
      extends ReadableWritableTests.ReadableWritableMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }

    }

    @Nested
    @DisplayName("MultipleByteArray ReadableWritable test with PrimitiveByteArray implementation")
    public class ReadableWritableMultipleByteArrayWithPrimitiveByteArrayTest
      extends ReadableWritableTests.ReadableWritableMultipleByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }

    @Nested
    @DisplayName("MultipleByteArray ReadableWritable test with self implementation")
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
    @DisplayName("MultipleByteArray ReadableWritable test on self (MultipleByteArray)")
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
      @TestFactory
      @DisplayName("Test toString()")
      public Iterable<DynamicTest> testToString() {
        return SelfByteArrayTest.generateTestsToString(
                getRNG(),
                "ReadableWritableByteArray",
                getToStringPerformanceLimit(),
                getExpectedToStringClassName(),
                this::createTestReadableWritableByteArray,
                this::writeTestByteArray,
                this::readTestByteArray,
                this::cleanTestByteArray,
                this.isReadableWritableOK()
        );
      }

      @Override
      public boolean isReadableWritableOK(){
        return true;
      }
    }
  }
}
