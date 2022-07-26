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

@DisplayName("PrimitiveByteArray tests")
public interface PrimitiveByteArrayTest extends ByteArrayTest{

  @Override
  default boolean is64BitAddressingSupported(){
    return false;
  }

  @DisplayName("PrimitiveByteArray - ReadableWritable test")
  class ReadableWritableTest implements ReadableWritableByteArrayTest, PrimitiveByteArrayTest{

    @Override
    public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

      // Check if wrapper, unwrap it
      if(testByteArray instanceof ReadOnlyByteArrayWrapper){
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;
      }

      // Check if it's ours
      if(!(testByteArray instanceof PrimitiveByteArray)) throw new IllegalArgumentException("Not primitive byte array");

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
      if(!(testByteArray instanceof PrimitiveByteArray)) throw new IllegalArgumentException("Not primitive byte array");

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

  @DisplayName("PrimitiveByteArray - ReadOnly test")
  class ReadOnlyTest implements ReadOnlyByteArrayTest, PrimitiveByteArrayTest{

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
      if(!(testByteArray instanceof PrimitiveByteArray)) throw new IllegalArgumentException("Not primitive byte array");

      // Update
      ((PrimitiveByteArray) testByteArray).data[(int) byteIndex] = value;
    }

    @Override
    public boolean isReadableWritableOK(){
      return false;
    }
  }

  @DisplayName("PrimitiveByteArray - WriteOnly test")
  class WriteOnlyTest implements WriteOnlyByteArrayTest, PrimitiveByteArrayTest{

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
      if(!(testByteArray instanceof PrimitiveByteArray)) throw new IllegalArgumentException("Not primitive byte array");

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

  @DisplayName("PrimitiveByteArray - WriteOnly test with control ByteArray implementation")
  class WriteOnlyWithControlByteArrayTest extends WriteOnlyTest implements WriteOnlyByteArrayWithOtherByteArrayTest{

  }

  @DisplayName("PrimitiveByteArray - WriteOnly test with PrimitiveByteArray implementation")
  class WriteOnlyWithSelfTest extends WriteOnlyWithControlByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new PrimitiveByteArray(new byte[(int) size]);
    }
  }

  @DisplayName("PrimitiveByteArray - WriteOnly test with ByteBufferByteArray implementation")
  class WriteOnlyWithByteBufferByteArrayTest extends WriteOnlyWithControlByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
    }
  }

  @DisplayName("PrimitiveByteArray - ReadOnly test with control ByteArray implementation")
  class ReadOnlyWithControlByteArrayTest extends ReadOnlyTest implements ReadOnlyByteArrayWithOtherByteArrayTest{

  }

  @DisplayName("PrimitiveByteArray - ReadOnly test with PrimitiveByteArray implementation")
  class ReadOnlyWithSelfTest extends ReadOnlyWithControlByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new PrimitiveByteArray(new byte[(int) size]);
    }
  }

  @DisplayName("PrimitiveByteArray - ReadOnly test with ByteBufferByteArray implementation")
  class ReadOnlyWithByteBufferByteArrayTest extends ReadOnlyWithControlByteArrayTest{

    @Nonnull
    @Override
    public ReadableWritableByteArray createControlReadableWritable(long size){
      return new ByteBufferByteArray(ByteBuffer.allocate((int) size));
    }
  }
}
