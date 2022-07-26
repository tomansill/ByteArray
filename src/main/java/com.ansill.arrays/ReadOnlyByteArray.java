package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.ansill.arrays.IndexingUtility.checkRead;

/**
 * Read-only interface of ByteArray
 * <p>
 * Immutability is not guaranteed and is up to the implementing classes to make it immutable or not.
 */
public interface ReadOnlyByteArray extends ByteArray{

  /**
   * Reads a byte at a specified index on this ReadOnlyByteArray
   *
   * @param byteIndex non-negative index on this ReadOnlyByteArray
   * @return byte value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   */
  byte readByte(@Nonnegative long byteIndex) throws ByteArrayIndexOutOfBoundsException;

  /**
   * Reads bytes from this ReadOnlyByteArray starting at specified byte index and writes the bytes to destination WriteOnlyByteArray.
   *
   * @param byteIndex   non-negative index on this ReadOnlyByteArray
   * @param destination destination WriteOnlyByteArray to be written to while reading the ReadOnlyByteArray
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds or if destination WriteOnlyByteArray is too large
   */
  default void read(@Nonnegative long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

    // Check parameters
    checkRead(byteIndex, destination, this.size());

    // Manual Copy
    for(long index = 0; index < destination.size(); index++){
      destination.writeByte(index, this.readByte(byteIndex + index));
    }
  }

  /**
   * Creates a subset of this ReadOnlyByteArray with defined start and length.
   *
   * @param start  the starting point on this ReadOnlyByteArray to start the subset range
   * @param length the length of new subset after the start point
   * @return Subset as ReadOnlyByteArray
   * @throws ByteArrayIndexOutOfBoundsException thrown if start or length is out of the bounds
   */
  @Nonnull
  @Override
  ReadOnlyByteArray subsetOf(@Nonnegative long start, @Nonnegative long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException;
}
