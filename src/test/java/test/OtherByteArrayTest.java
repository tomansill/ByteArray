package test;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface OtherByteArrayTest{

  @Nonnull
  default ReadableWritableByteArray createControlReadableWritable(@Nonnegative long size){
    return new TestOnlyByteArray(size);
  }

  /**
   * Special case where implementing classes may need to clean up their huge ByteArrays when done. In most cases, this
   * is not necessary as Garbage Collector will just collect it.
   *
   * @param byteArray byte array that may be wrapped in ReadOnlyByteArrayWrapper or WriteOnlyByteArrayWrapper
   */
  default void cleanControlByteArray(@Nonnull ByteArray byteArray){
    // Do nothing
  }
}
