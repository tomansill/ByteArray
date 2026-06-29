package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@link ByteArray} interface with utility functions for creating {@link ByteArray}s
 * <p>
 * This interface provides the base implementation for handling {@link ByteArray}s and static methods for creating {@link ByteArray}s
 */
public interface ByteArray{

  /**
   * Returns the size of bytes in this {@link ByteArray}
   *
   * @return the size of bytes as non-negative long value
   */
  @Nonnegative
  long size();

  /**
   * Creates a subset of this {@link ByteArray} with defined start and length.
   * <p>
   * The resulting subset {@link ByteArray} is a view of the original {@link ByteArray}. Any changes to either subset or original {@link ByteArray} will propagate to each other.
   *
   * @param start  the starting point on this {@link ByteArray} to start the subset range
   * @param length the length of new subset after the start point
   * @return new {@link ByteArray} as a subset or same {@link ByteArray} if start is 0 and length is equal to current {@link ByteArray}'s size
   * @throws ByteArrayIndexOutOfBoundsException thrown if start or length is out of the bounds
   * @throws ByteArrayInvalidLengthException    thrown if the length is negative
   * @throws ByteArrayLengthOverBoundsException thrown if length of new subset {@link ByteArray} goes over the bounds
   */
  @Nonnull
  ByteArray subsetOf(@Nonnegative long start, @Nonnegative long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException;

}
