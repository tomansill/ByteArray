package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import test.BaseByteArrayTest;
import test.other.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.other.ReadableWritableByteArrayWithOtherByteArrayTest;
import test.other.WriteOnlyByteArrayWithOtherByteArrayTest;
import test.self.SelfReadOnlyByteArrayTest;
import test.self.SelfReadableWritableByteArrayTest;
import test.self.SelfWriteOnlyByteArrayTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@DisplayName("ByteBufferByteArray Test Suite")
public class ByteBufferByteArrayNestedTest{

  public abstract static class ByteBufferByteArrayTest implements BaseByteArrayTest{

    public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

      // Check if wrapper, unwrap it
      while(testByteArray instanceof WriteOnlyByteArrayWrapper){
        testByteArray = ((WriteOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof ByteBufferByteArray)){
        throw new IllegalArgumentException("Not bytebuffer byte array. Actual: " + testByteArray.getClass().getName());
      }

      // Read
      return ((ByteBufferByteArray) testByteArray).readByte(byteIndex);
    }

    public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

      // Check if wrapper, unwrap it
      while(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof ByteBufferByteArray)){
        throw new IllegalArgumentException("Not bytebuffer byte array. Actual: " + testByteArray.getClass().getName());
      }

      // Update
      ((ByteBufferByteArray) testByteArray).writeByte((int) byteIndex, value);
    }

    @Nonnull
    public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
    }

    @Nonnull
    public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
      return createTestReadableWritableByteArray(size).toWriteOnly();
    }

    @Nonnull
    public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
      return createTestReadableWritableByteArray(size).toReadOnly();
    }

    @Override
    public boolean isReadableWritableOK(){
      return false;
    }
  }

  @Nested
  @DisplayName("ReadOnly Tests")
  public class ReadOnlyTests{

    @Nested
    @DisplayName("ReadOnly test")
    public class ReadOnlyByteBufferByteArrayTest extends ByteBufferByteArrayTest implements SelfReadOnlyByteArrayTest{

    }

    @Nested
    @DisplayName("ReadOnly test with ByteBufferByteArray implementation")
    public class ReadOnlyByteBufferByteArrayWithSelfByteArrayTest
      extends ReadOnlyByteBufferByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }

    @Nested
    @DisplayName("ReadOnly test with control ByteArray implementation")
    public class ReadOnlyByteBufferByteArrayWithControlByteArrayTest extends ReadOnlyByteBufferByteArrayTest
      implements
      ReadOnlyByteArrayWithOtherByteArrayTest{

    }

    @Nested
    @DisplayName("ReadOnly test with PrimitiveByteArray implementation")
    public class ReadOnlyByteBufferByteArrayWithPrimitiveByteArrayTest
      extends ReadOnlyByteBufferByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }

    @Nested
    @DisplayName("ReadOnly test with MultipleByteArray implementation")
    public class ReadOnlyByteBufferByteArrayWithMultipleByteArrayTest
      extends ReadOnlyByteBufferByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return MultipleByteArrayNestedTest.createReadableWritableByteArray(
          size,
          (int) (size + 232)
        );
      }
    }
  }

  @Nested
  @DisplayName("WriteOnly Tests")
  public class WriteOnlyTests{

    @Nested
    @DisplayName("WriteOnly test")
    public class WriteOnlyByteBufferByteArrayTest extends ByteBufferByteArrayTest implements SelfWriteOnlyByteArrayTest{

    }

    @Nested
    @DisplayName("WriteOnly test with PrimitiveByteArray implementation")
    public class WriteOnlyByteBufferByteArrayWithPrimitiveByteArrayTest
      extends WriteOnlyByteBufferByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }

    @Nested
    @DisplayName("WriteOnly test with MultipleByteArray implementation")
    public class WriteOnlyByteBufferByteArrayWithMultipleByteArrayTest
      extends WriteOnlyByteBufferByteArrayWithControlByteArrayTest{

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
    @DisplayName("WriteOnly test with control ByteArray implementation")
    public class WriteOnlyByteBufferByteArrayWithControlByteArrayTest extends ByteBufferByteArrayTest
      implements
      WriteOnlyByteArrayWithOtherByteArrayTest{

    }

    @Nested
    @DisplayName("WriteOnly test with ByteBufferByteArray implementation")
    public class WriteOnlyByteBufferByteArrayWithSelfTest extends WriteOnlyByteBufferByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }
  }

  @Nested
  @DisplayName("ReadableWritable Tests")
  public class ReadableWritableTests{


    @Nested
    @DisplayName("ReadableWritable test")
    public class ReadableWritableByteBufferByteArrayTest extends ByteBufferByteArrayTest
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
    }

    @Nested
    @DisplayName("ReadableWritable test with PrimitiveByteArray implementation")
    public class ReadableWritableByteBufferByteArrayWithPrimitiveByteArrayTest
      extends ReadableWritableByteBufferByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }

    @Nested
    @DisplayName("ReadableWritable test with MultipleByteArray implementation")
    public class ReadableWritableByteBufferByteArrayWithMultipleByteArrayTest
      extends ReadableWritableByteBufferByteArrayWithControlByteArrayTest{

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
    @DisplayName("ReadableWritable test with control ByteArray implementation")
    public class ReadableWritableByteBufferByteArrayWithControlByteArrayTest
      extends ReadableWritableByteBufferByteArrayTest
      implements
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
    @DisplayName("ReadableWritable test with ByteBufferByteArray implementation")
    public class ReadableWritableByteBufferByteArrayWithSelfTest
      extends ReadableWritableByteBufferByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }
  }

}
