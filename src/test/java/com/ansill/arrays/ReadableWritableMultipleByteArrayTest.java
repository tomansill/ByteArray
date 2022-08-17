package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import test.ReadableWritableByteArray64BitTest;
import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnull;
import java.util.Map;

@DisplayName("MultipleByteArray - ReadableWritable test")
public
class ReadableWritableMultipleByteArrayTest implements ReadableWritableByteArray64BitTest, MultipleByteArrayTest{

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
