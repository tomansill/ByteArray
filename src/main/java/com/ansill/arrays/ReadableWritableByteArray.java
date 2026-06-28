package com.ansill.arrays;

import javax.annotation.Nonnull;

/**
 * Readable and Writable interface of {@link ByteArray}
 * <p>
 * {@link ReadableWritableByteArray} provides a full read and write access of the byte array data.
 */
public interface ReadableWritableByteArray extends ReadOnlyByteArray, WriteOnlyByteArray, ByteArray{

  /**
   * Converts this {@link ReadableWritableByteArray} to {@link ReadOnlyByteArray}
   * <p>
   * The resulting {@link ReadOnlyByteArray} will guarantee read-only operations of this
   * {@link ReadableWritableByteArray}. Any changes to {@link ReadableWritableByteArray} or to its underlying
   * data structure will propagate to the resulting {@link ReadOnlyByteArray}.
   *
   * @return {@link ReadOnlyByteArray}
   */
  @Nonnull
  default ReadOnlyByteArray toReadOnly(){
    return new ReadOnlyByteArrayWrapper(this);
  }

  /**
   * Converts this {@link ReadableWritableByteArray} to {@link WriteOnlyByteArray}
   * <p>
   * The resulting {@link WriteOnlyByteArray} will guarantee write-only operations of this
   * {@link ReadableWritableByteArray}. Any write operations on the resulting {@link WriteOnlyByteArray} will affect
   * this {@link ReadableWritableByteArray} and possibly its underlying data structure.
   *
   * @return {@link WriteOnlyByteArray}
   */
  @Nonnull
  default WriteOnlyByteArray toWriteOnly(){
    return new WriteOnlyByteArrayWrapper(this);
  }

  /**
   * Creates a subset of this {@link ReadableWritableByteArray} with defined start and length.
   * <p>
   * The resulting subset {@link ReadableWritableByteArray} is a view of the original
   * {@link ReadableWritableByteArray}. Any changes to either subset or original {@link ReadableWritableByteArray}
   * will propagate to each other.
   *
   * @param start  the starting point on this {@link ReadableWritableByteArray} to start the subset range
   * @param length the length of new subset after the start point
   * @return new {@link ReadableWritableByteArray} as a subset or same {@link ReadableWritableByteArray} if start is 0 and length is equal to current {@link ReadableWritableByteArray}'s size
   * @throws ByteArrayIndexOutOfBoundsException thrown if start or length is out of the bounds
   * @throws ByteArrayInvalidLengthException    thrown if the length is negative
   * @throws ByteArrayLengthOverBoundsException thrown if length of new subset {@link ReadableWritableByteArray} goes over the bounds
   */
  @Nonnull
  @Override
  ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException;
}
