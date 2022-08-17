package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.ansill.arrays.IndexingUtility.checkRead;

/**
 * Read-only interface of {@link ByteArray}
 * <p>
 * {@link ReadOnlyByteArray} provides a read-only view of {@link ReadableWritableByteArray}.
 * <p>
 * Immutability is not guaranteed. Any changes done at the original {@link ReadableWritableByteArray} should
 * propagate to the associated {@link ReadOnlyByteArray}. Preventing mutations to the
 * {@link ReadableWritableByteArray} or its backing data can achieve immutability in the associated
 * {@link ReadOnlyByteArray}.
 */
public interface ReadOnlyByteArray extends ByteArray{

  /**
   * Reads a byte at a specified index on this {@link ReadOnlyByteArray}
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return byte value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   */
  byte readByte(@Nonnegative long byteIndex) throws ByteArrayIndexOutOfBoundsException;

  /**
   * Reads bytes from this {@link ReadOnlyByteArray} starting at specified byte index and writes the bytes to destination {@link WriteOnlyByteArray}.
   *
   * @param byteIndex   non-negative index on this {@link ReadOnlyByteArray}
   * @param destination destination {@link WriteOnlyByteArray} to be written to while reading the {@link ReadOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds or if destination {@link WriteOnlyByteArray} is too large
   * @throws ByteArrayLengthOverBoundsException thrown if destination byte array goes over the bounds
   */
  default void read(@Nonnegative long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, this.size());

    // Manual Copy
    for(long index = 0; index < destination.size(); index++){
      destination.writeByte(index, this.readByte(byteIndex + index));
    }
  }

  /**
   * Creates a subset of this {@link ReadOnlyByteArray} with defined start and length.
   * <p>
   * The resulting subset {@link ReadOnlyByteArray} is a view of the original {@link ReadOnlyByteArray}. Any
   * changes to the {@link ReadableWritableByteArray} that is linked to the original {@link ReadOnlyByteArray} that
   * the subset {@link ReadOnlyByteArray} came from, will be updated to reflect the change.
   *
   * @param start  the starting point on this {@link ReadOnlyByteArray} to start the subset range
   * @param length the length of new subset after the start point
   * @return new {@link ReadOnlyByteArray} as a subset or same {@link ReadOnlyByteArray} if start is 0 and length is equal to current {@link ReadOnlyByteArray}'s size
   * @throws ByteArrayIndexOutOfBoundsException thrown if start or length is out of the bounds
   * @throws ByteArrayInvalidLengthException    thrown if the length is negative
   * @throws ByteArrayLengthOverBoundsException thrown if length of new subset {@link ReadOnlyByteArray} goes over the bounds
   */
  @Nonnull
  @Override
  ReadOnlyByteArray subsetOf(@Nonnegative long start, @Nonnegative long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException;
}
