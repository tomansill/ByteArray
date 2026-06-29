package test;

import com.ansill.arrays.*;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.*;

public interface BaseByteArrayTest{

  @Nonnull
  String SEED = "the holy moly seed";

  @Nonnegative
  int TRIALS = 3;

  /**
   * Special case where implementing classes may need to clean up their huge ByteArrays when done. In most cases, this
   * is not necessary as Garbage Collector will just collect it.
   *
   * @param byteArray byte array that may be wrapped in ReadOnlyByteArrayWrapper or WriteOnlyByteArrayWrapper
   */
  default void cleanTestByteArray(@Nonnull ByteArray byteArray){
    // Do nothing
  }

  @Nonnull
  default Random getRNG(){
    return new Random(SEED.hashCode() + this.getClass().getName().hashCode());
  }

  /**
   * Returns whether if test byte array must be ReadOnly/WriteOnly or it can be ReadableWritable
   *
   * @return true if it can be readable-writable, false if must readonly
   */
  boolean isReadableWritableOK();

  @Nonnull
  ByteArray createTestByteArray(long size);

  default long getToStringPerformanceLimit(){
    return 128L;
  }

  @Nonnull
  String getExpectedToStringClassName();

  void writeTestByteArray(@Nonnull ByteArray testByteArray, @Nonnegative long byteIndex, byte value);

  byte readTestByteArray(@Nonnull ByteArray byteArray, long index);

}
