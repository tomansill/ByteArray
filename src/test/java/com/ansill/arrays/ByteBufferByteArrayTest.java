package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.ByteArrayTest;
import test.ReadOnlyByteArrayTest;
import test.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.ReadableWritableByteArrayTest;
import test.WriteOnlyByteArrayTest;
import test.WriteOnlyByteArrayWithOtherByteArrayTest;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@DisplayName("ByteBufferByteArray tests")
public interface ByteBufferByteArrayTest extends ByteArrayTest{

  @Override
  default boolean is64BitAddressingSupported(){
    return false;
  }

  @DisplayName("ByteBufferByteArray - ReadableWritable test")
  class ReadableWritableTest implements ReadableWritableByteArrayTest, ByteBufferByteArrayTest{

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

  @DisplayName("ByteBufferByteArray - ReadOnly test")
  class ReadOnlyTest implements ReadOnlyByteArrayTest, ByteBufferByteArrayTest{

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

  @DisplayName("ByteBufferByteArray - WriteOnly test")
  class WriteOnlyTest implements WriteOnlyByteArrayTest, ByteBufferByteArrayTest{

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

  @DisplayName("ByteBufferByteArray - WriteOnly test with control ByteArray implementation")
  class WriteOnlyWithControlByteArrayTest extends WriteOnlyTest implements WriteOnlyByteArrayWithOtherByteArrayTest{

  }

  @DisplayName("ByteBufferByteArray - WriteOnly test with ByteBufferByteArray implementation")
  class WriteOnlyWithSelfTest extends WriteOnlyWithControlByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
    }
  }

  @DisplayName("ByteBufferByteArray - WriteOnly test with PrimitiveByteArray implementation")
  class WriteOnlyWithByteBufferByteArrayTest extends WriteOnlyWithControlByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new PrimitiveByteArray(new byte[(int) size]);
    }
  }

  @DisplayName("ByteBufferByteArray - ReadOnly test with control ByteArray implementation")
  class ReadOnlyWithControlByteArrayTest extends ReadOnlyTest implements ReadOnlyByteArrayWithOtherByteArrayTest{

  }

  @DisplayName("ByteBufferByteArray - ReadOnly test with PrimitiveByteArray implementation")
  class ReadOnlyWithSelfTest extends ReadOnlyWithControlByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new PrimitiveByteArray(new byte[(int) size]);
    }
  }

  @DisplayName("ByteBufferByteArray - ReadOnly test with ByteBufferByteArray implementation")
  class ReadOnlyWithByteBufferByteArrayTest extends ReadOnlyWithControlByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
    }
  }
}
