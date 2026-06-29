package com.ansill.arrays;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import test.BaseByteArrayTest;
import test.other.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.other.ReadableWritableByteArrayWithOtherByteArrayTest;
import test.other.WriteOnlyByteArrayWithOtherByteArrayTest;
import test.self.SelfByteArrayTest;
import test.self.SelfReadOnlyByteArrayTest;
import test.self.SelfReadableWritableByteArrayTest;
import test.self.SelfWriteOnlyByteArrayTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@DisplayName("PrimitiveByteArray Test Suite")
public class PrimitiveByteArrayNestedTest{

  public abstract static class PrimitiveByteArrayTest implements BaseByteArrayTest{

    @Override
    public @NonNull String getExpectedToStringClassName() {
      return "PrimitiveByteArray";
    }

    @Override
    public void writeTestByteArray(@Nonnull ByteArray testByteArray, long byteIndex, byte value){

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
      if(!(testByteArray instanceof PrimitiveByteArray))
        throw new IllegalArgumentException("Not primitive byte array");

      // Update
      ((PrimitiveByteArray) testByteArray).writeByte(byteIndex, value);
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
      if(!(testByteArray instanceof PrimitiveByteArray))
        throw new IllegalArgumentException("Not primitive byte array");

      // Update
      return ((PrimitiveByteArray) testByteArray).readByte(byteIndex);
    }

    @Nonnull
    public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
      return createTestReadableWritableByteArray(size).toReadOnly();
    }

    @Nonnull
    public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
      return createTestReadableWritableByteArray(size).toWriteOnly();
    }

    @Nonnull
    public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
      return new PrimitiveByteArray(new byte[(int) size]);
    }

    @Override
    public boolean isReadableWritableOK(){
      return false;
    }
  }

  @Nested
  @DisplayName("PrimitiveByteArray ReadOnly tests")
  public class ReadOnlyTests{

    @Nested
    @DisplayName("PrimitiveByteArray ReadOnly test on self")
    public class ReadOnlyPrimitiveByteArrayTest extends PrimitiveByteArrayTest implements SelfReadOnlyByteArrayTest{
      @Override
      @DisplayName("Test toString()")
      @TestFactory
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

    @Nested
    @DisplayName("PrimitiveByteArray ReadOnly test with ByteBufferByteArray implementation")
    public class ReadOnlyPrimitiveByteArrayWithByteBufferByteArrayTest
      extends ReadOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }

    @Nested
    @DisplayName("PrimitiveByteArray ReadOnly test with control ByteArray implementation")
    public class ReadOnlyPrimitiveByteArrayWithControlByteArrayTest extends PrimitiveByteArrayTest
      implements ReadOnlyByteArrayWithOtherByteArrayTest{
    }

    @Nested
    @DisplayName("PrimitiveByteArray ReadOnly test with MultipleByteArray implementation")
    public
    class ReadOnlyPrimitiveByteArrayWithMultipleByteArrayTest
      extends ReadOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return MultipleByteArrayNestedTest.createReadableWritableByteArray(
          size,
          (int) (size + 232)
        );
      }
    }

    @Nested
    @DisplayName("PrimitiveByteArray ReadOnly test with PrimitiveByteArray implementation")
    public class ReadOnlyPrimitiveByteArrayWithSelfTest extends ReadOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }
  }

  @Nested
  @DisplayName("PrimitiveByteArray WriteOnly tests")
  public class WriteOnlyTests{

    @Nested
    @DisplayName("PrimitiveByteArray WriteOnly test on self")
    public class WriteOnlyPrimitiveByteArrayTest extends PrimitiveByteArrayTest implements SelfWriteOnlyByteArrayTest, SelfByteArrayTest{

      @Override
      @DisplayName("Test toString()")
      @TestFactory
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

    @Nested
    @DisplayName("PrimitiveByteArray WriteOnly test with ByteBufferByteArray implementation")
    public class WriteOnlyPrimitiveByteArrayWithByteBufferByteArrayTest
      extends WriteOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }

    @Nested
    @DisplayName("PrimitiveByteArray WriteOnly test with control ByteArray implementation")
    public class WriteOnlyPrimitiveByteArrayWithControlByteArrayTest extends PrimitiveByteArrayTest implements
      WriteOnlyByteArrayWithOtherByteArrayTest{

      @Override
      public boolean isReadableWritableOK(){
        return false;
      }
    }

    @Nested
    @DisplayName("PrimitiveByteArray WriteOnly test with MultipleByteArray implementation")
    public class WriteOnlyPrimitiveByteArrayWithMultipleByteArrayTest
      extends WriteOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return MultipleByteArrayNestedTest.createReadableWritableByteArray(
          size,
          (int) (size + 232)
        );
      }
    }

    @Nested
    @DisplayName("PrimitiveByteArray WriteOnly test with PrimitiveByteArray implementation")
    public class WriteOnlyPrimitiveByteArrayWithSelfTest extends WriteOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }
  }

  @Nested
  @DisplayName("PrimitiveByteArray ReadableWritable tests")
  public class ReadableWritableTests{

    @Nested
    @DisplayName("PrimitiveByteArray ReadableWritable test on self")
    public class ReadableWritablePrimitiveByteArrayTest extends PrimitiveByteArrayTest
      implements SelfReadableWritableByteArrayTest{

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

      @Override
      @DisplayName("Test toString()")
      @TestFactory
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
    }

    @Nested
    @DisplayName("PrimitiveByteArray ReadableWritable test with ByteBufferByteArray implementation")
    public class ReadableWritablePrimitiveByteArrayWithByteBufferByteArrayTest
      extends ReadableWritableTests.ReadableWritablePrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }

    @Nested
    @DisplayName("PrimitiveByteArray ReadableWritable test with control ByteArray implementation")
    public class ReadableWritablePrimitiveByteArrayWithControlByteArrayTest extends PrimitiveByteArrayTest implements
      ReadableWritableByteArrayWithOtherByteArrayTest{

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
    @DisplayName("PrimitiveByteArray ReadableWritable test with MultipleByteArray implementation")
    public
    class ReadableWritablePrimitiveByteArrayWithMultipleByteArrayTest
      extends ReadableWritableTests.ReadableWritablePrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return MultipleByteArrayNestedTest.createReadableWritableByteArray(
          size,
          (int) (size + 232)
        );
      }
    }

    @Nested
    @DisplayName("PrimitiveByteArray ReadableWritable test with PrimitiveByteArray implementation")
    public class ReadableWritablePrimitiveByteArrayWithSelfTest
      extends ReadableWritableTests.ReadableWritablePrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }
  }
}
