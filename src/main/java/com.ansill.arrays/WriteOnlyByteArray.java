package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.ansill.arrays.IndexingUtility.checkWrite;

/**
 * Write-only interface of ByteArray
 */
public interface WriteOnlyByteArray extends ByteArray{

  /**
   * Writes a byte at a specified byte index on this WriteOnlyByteArray
   *
   * @param byteIndex non-negative index on this WriteOnlyByteArray
   * @param value     byte value to be written to this WriteOnlyByteArray
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   */
  void writeByte(@Nonnegative long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException;

  /**
   * Writes bytes from source ReadOnlyByteArray to this WriteOnlyByteArray starting at specified byte index on WriteOnlyByteArray
   *
   * @param byteIndex non-negative index on this WriteOnlyByteArray
   * @param source    source ReadOnlyByteArray to copy bytes to this WriteOnlyByteArray
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds or if source ReadOnlyByteArray is too large
   * @throws ByteArrayLengthOverBoundsException thrown if length goes over the bounds
   */
  default void write(@Nonnegative long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkWrite(byteIndex, source, this.size());

    // Manual copy
    for(long index = 0; index < source.size(); index++){
      this.writeByte(byteIndex + index, source.readByte(index));
    }
  }

  /**
   * Creates a subset of this WriteOnlyByteArray with defined start and length.
   *
   * @param start  the starting point on this WriteOnlyByteArray to start the subset range
   * @param length the length of new subset after the start point
   * @return Subset as WriteOnlyByteArray
   * @throws ByteArrayIndexOutOfBoundsException thrown if start or length is out of the bounds
   * @throws ByteArrayInvalidLengthException    thrown if the length is negative
   * @throws ByteArrayLengthOverBoundsException thrown if length goes over the bounds
   */
  @SuppressWarnings("unchecked")
  @Nonnull
  @Override
  WriteOnlyByteArray subsetOf(@Nonnegative long start, @Nonnegative long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException;
}
