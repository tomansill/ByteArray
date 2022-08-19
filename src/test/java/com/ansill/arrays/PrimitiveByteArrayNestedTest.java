package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import test.ByteArrayTest;
import test.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.ReadableWritableByteArrayTest;
import test.SimpleReadOnlyByteArrayTest;
import test.WriteOnlyByteArrayTest;
import test.WriteOnlyByteArrayWithOtherByteArrayTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@DisplayName("PrimitiveByteArray Test Suite")
public class PrimitiveByteArrayNestedTest{

  public interface PrimitiveByteArrayTest extends ByteArrayTest{

  }

  @Nested
  @DisplayName("ReadOnly tests")
  public class ReadOnlyTests{

    @Nested
    @DisplayName("ReadOnly test")
    public class ReadOnlyPrimitiveByteArrayTest implements SimpleReadOnlyByteArrayTest, PrimitiveByteArrayTest{

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
        if(!(testByteArray instanceof PrimitiveByteArray))
          throw new IllegalArgumentException("Not primitive byte array");

        // Update
        ((PrimitiveByteArray) testByteArray).data[(int) byteIndex] = value;
      }

      @Override
      public boolean isReadableWritableOK(){
        return false;
      }
    }

    @Nested
    @DisplayName("ReadOnly test with ByteBufferByteArray implementation")
    public class ReadOnlyPrimitiveByteArrayWithByteBufferByteArrayTest
      extends ReadOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }

    @Nested
    @DisplayName("ReadOnly test with control ByteArray implementation")
    public class ReadOnlyPrimitiveByteArrayWithControlByteArrayTest implements ReadOnlyByteArrayWithOtherByteArrayTest{

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
        if(!(testByteArray instanceof PrimitiveByteArray))
          throw new IllegalArgumentException("Not primitive byte array");

        // Update
        ((PrimitiveByteArray) testByteArray).data[(int) byteIndex] = value;
      }

      @Override
      public boolean isReadableWritableOK(){
        return false;
      }
    }

    @Nested
    @DisplayName("ReadOnly test with MultipleByteArray implementation")
    public
    class ReadOnlyPrimitiveByteArrayWithMultipleByteArrayTest
      extends ReadOnlyPrimitiveByteArrayWithControlByteArrayTest{

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
    @DisplayName("ReadOnly test with PrimitiveByteArray implementation")
    public class ReadOnlyPrimitiveByteArrayWithSelfTest extends ReadOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }
  }

  @Nested
  @DisplayName("WriteOnly tests")
  public class WriteOnlyTests{

    @Nested
    @DisplayName("WriteOnly test")
    public class WriteOnlyPrimitiveByteArrayTest implements WriteOnlyByteArrayTest, PrimitiveByteArrayTest{

      @Nonnull
      @Override
      public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
        return new PrimitiveByteArray(new byte[(int) size]).toWriteOnly();
      }

      @Override
      public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

        // Check if writeonly wrapper, unwrap it
        if(testByteArray instanceof WriteOnlyByteArrayWrapper){
          testByteArray = ((WriteOnlyByteArrayWrapper) testByteArray).original;
        }

        // Check if it's ours
        if(!(testByteArray instanceof PrimitiveByteArray))
          throw new IllegalArgumentException("Not primitive byte array");

        // Read
        return ((PrimitiveByteArray) testByteArray).data[(int) byteIndex];
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

    @Nested
    @DisplayName("WriteOnly test with ByteBufferByteArray implementation")
    public class WriteOnlyPrimitiveByteArrayWithByteBufferByteArrayTest
      extends WriteOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
      }
    }

    @Nested
    @DisplayName("WriteOnly test with control ByteArray implementation")
    public class WriteOnlyPrimitiveByteArrayWithControlByteArrayTest extends WriteOnlyPrimitiveByteArrayTest
      implements
      WriteOnlyByteArrayWithOtherByteArrayTest{

    }

    @Nested
    @DisplayName("WriteOnly test with MultipleByteArray implementation")
    public class WriteOnlyPrimitiveByteArrayWithMultipleByteArrayTest
      extends WriteOnlyPrimitiveByteArrayWithControlByteArrayTest{

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
    @DisplayName("WriteOnly test with PrimitiveByteArray implementation")
    public class WriteOnlyPrimitiveByteArrayWithSelfTest extends WriteOnlyPrimitiveByteArrayWithControlByteArrayTest{

      @Nonnull
      @Override
      public ReadableWritableByteArray createControlReadableWritable(long size){
        return new PrimitiveByteArray(new byte[(int) size]);
      }
    }
  }

  @Nested
  @DisplayName("ReadableWritable test")
  public class ReadableWritableTests{

    @Nested
    @DisplayName("ReadableWritable test")
    public class ReadableWritablePrimitiveByteArrayTest
      implements ReadableWritableByteArrayTest, PrimitiveByteArrayTest{

      @Override
      public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

        // Check if wrapper, unwrap it
        if(testByteArray instanceof ReadOnlyByteArrayWrapper){
          testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
        }

        // Check if it's ours
        if(!(testByteArray instanceof PrimitiveByteArray))
          throw new IllegalArgumentException("Not primitive byte array");

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
        if(!(testByteArray instanceof PrimitiveByteArray))
          throw new IllegalArgumentException("Not primitive byte array");

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
  }
}
