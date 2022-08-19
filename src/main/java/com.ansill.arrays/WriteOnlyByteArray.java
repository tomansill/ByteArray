package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.ansill.arrays.IndexingUtility.checkWrite;

/**
 * Write-only interface of {@link ByteArray}
 * <p>
 * {@link WriteOnlyByteArray} provides a write-only access of {@link ReadableWritableByteArray}.
 */
public interface WriteOnlyByteArray extends ByteArray{

  /**
   * Writes a byte at a specified byte index on this {@link WriteOnlyByteArray}
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     byte value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   */
  void writeByte(@Nonnegative long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException;

  /**
   * Writes bytes from source {@link ReadOnlyByteArray} to this {@link WriteOnlyByteArray} starting at specified byte index on {@link WriteOnlyByteArray}
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param source    source {@link ReadOnlyByteArray} to copy bytes to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds or if source {@link ReadOnlyByteArray} is too large
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
   * Creates a subset of this {@link WriteOnlyByteArray} with defined start and length.
   * <p>
   * The resulting subset {@link WriteOnlyByteArray} is a view of the original {@link WriteOnlyByteArray}. Any
   * changes to either subset or original {@link ByteArray} will propagate to each other.
   *
   * @param start  the starting point on this {@link WriteOnlyByteArray} to start the subset range
   * @param length the length of new subset after the start point
   * @return new {@link WriteOnlyByteArray} as a subset or same {@link WriteOnlyByteArray} if start is 0 and length is equal to current {@link WriteOnlyByteArray}'s size
   * @throws ByteArrayIndexOutOfBoundsException thrown if start or length is out of the bounds
   * @throws ByteArrayInvalidLengthException    thrown if the length is negative
   * @throws ByteArrayLengthOverBoundsException thrown if length of new subset {@link WriteOnlyByteArray} goes over the bounds
   */
  @Nonnull
  @Override
  WriteOnlyByteArray subsetOf(@Nonnegative long start, @Nonnegative long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException;
}
