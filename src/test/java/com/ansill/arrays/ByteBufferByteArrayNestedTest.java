package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import test.ByteArrayTest;
import test.ReadOnlyByteArrayTest;
import test.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.ReadableWritableByteArrayTest;
import test.WriteOnlyByteArrayTest;
import test.WriteOnlyByteArrayWithOtherByteArrayTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@DisplayName("ByteBufferByteArray Test Suite")
public class ByteBufferByteArrayNestedTest{

  @DisplayName("ByteBufferByteArray tests")
  public interface ByteBufferByteArrayTest extends ByteArrayTest{

  }

  @Nested
  @DisplayName("ReadableWritable test")
  public class ReadableWritableByteBufferByteArrayTest
    implements ReadableWritableByteArrayTest, ByteBufferByteArrayTest{

    @Override
    public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof ByteBufferByteArray))
        throw new IllegalArgumentException("Not primitive byte array");

      // Read
      return ((ByteBufferByteArray) testByteArray).data.get((int) byteIndex);
    }

    @Override
    public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof ByteBufferByteArray))
        throw new IllegalArgumentException("Not primitive byte array");

      // Update
      ((ByteBufferByteArray) testByteArray).data.put((int) byteIndex, value);
    }

    @Nonnull
    @Override
    public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
    }

    @Override
    public boolean isReadableWritableOK(){
      return true;
    }
  }

  @Nested
  @DisplayName("ReadOnly test")
  public class ReadOnlyByteBufferByteArrayTest implements ReadOnlyByteArrayTest, ByteBufferByteArrayTest{

    @Nonnull
    @Override
    public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size)).toReadOnly();
    }

    @Override
    public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof ByteBufferByteArray))
        throw new IllegalArgumentException("Not primitive byte array");

      // Update
      ((ByteBufferByteArray) testByteArray).data.put((int) byteIndex, value);
    }

    @Override
    public boolean isReadableWritableOK(){
      return false;
    }
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
      return MultipleByteArrayNestedTest.MultipleByteArrayTest.createReadableWritableByteArray(
        size,
        (int) (size + 232)
      );
    }
  }

  @Nested
  @DisplayName("WriteOnly test")
  public class WriteOnlyByteBufferByteArrayTest implements WriteOnlyByteArrayTest, ByteBufferByteArrayTest{

    @Nonnull
    @Override
    public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size)).toWriteOnly();
    }

    @Override
    public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

      // Check if writeonly wrapper, unwrap it
      if(testByteArray instanceof WriteOnlyByteArrayWrapper){
        testByteArray = ((WriteOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof ByteBufferByteArray))
        throw new IllegalArgumentException("Not primitive byte array");

      // Read
      return ((ByteBufferByteArray) testByteArray).data.get((int) byteIndex);
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
      return MultipleByteArrayNestedTest.MultipleByteArrayTest.createReadableWritableByteArray(
        size,
        (int) (size + 232)
      );
    }
  }

  @Nested
  @DisplayName("WriteOnly test with control ByteArray implementation")
  public class WriteOnlyByteBufferByteArrayWithControlByteArrayTest extends WriteOnlyByteBufferByteArrayTest
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
