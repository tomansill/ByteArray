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
   * Reads a short at a specified index on this {@link ReadOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return short value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default short readShortBE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 2, this.size());

    // Use two readByte calls
    int value = (0xff & readByte(byteIndex)) << 8;
    value |= (0xff & readByte(byteIndex + 1));

    // Return value
    return (short) value;
  }

  /**
   * Reads a short at a specified index on this {@link ReadOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return short value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default short readShortLE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 2, this.size());

    // Use two readByte calls
    int value = (0xff & readByte(byteIndex + 1)) << 8;
    value |= (0xff & readByte(byteIndex));

    // Return value
    return (short) value;
  }

  /**
   * Reads an int at a specified index on this {@link ReadOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return int value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default int readIntBE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 4, this.size());

    // Use four readByte calls
    int value = (0xff & readByte(byteIndex)) << 8;
    value = (value | (0xff & readByte(byteIndex + 1))) << 8;
    value = (value | (0xff & readByte(byteIndex + 2))) << 8;
    value = (value | (0xff & readByte(byteIndex + 3)));

    // Return value
    return value;
  }

  /**
   * Reads an int at a specified index on this {@link ReadOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return int value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default int readIntLE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 4, this.size());

    // Use four readByte calls
    int value = (0xff & readByte(byteIndex + 3)) << 8;
    value = (value | (0xff & readByte(byteIndex + 2))) << 8;
    value = (value | (0xff & readByte(byteIndex + 1))) << 8;
    value = (value | (0xff & readByte(byteIndex)));

    // Return value
    return value;
  }

  /**
   * Reads a long at a specified index on this {@link ReadOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return long value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default long readLongBE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 8, this.size());

    // Use eight readByte calls
    long value = (0xff & readByte(byteIndex)) << 8;
    value = (value | (0xff & readByte(byteIndex + 1))) << 8;
    value = (value | (0xff & readByte(byteIndex + 2))) << 8;
    value = (value | (0xff & readByte(byteIndex + 3))) << 8;
    value = (value | (0xff & readByte(byteIndex + 4))) << 8;
    value = (value | (0xff & readByte(byteIndex + 5))) << 8;
    value = (value | (0xff & readByte(byteIndex + 6))) << 8;
    value = (value | (0xff & readByte(byteIndex + 7)));

    // Return value
    return value;
  }

  /**
   * Reads a long at a specified index on this {@link ReadOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return long value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default long readLongLE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 8, this.size());

    // Use eight readByte calls
    long value = (0xff & readByte(byteIndex + 7)) << 8;
    value = (value | (0xff & readByte(byteIndex + 6))) << 8;
    value = (value | (0xff & readByte(byteIndex + 5))) << 8;
    value = (value | (0xff & readByte(byteIndex + 4))) << 8;
    value = (value | (0xff & readByte(byteIndex + 3))) << 8;
    value = (value | (0xff & readByte(byteIndex + 2))) << 8;
    value = (value | (0xff & readByte(byteIndex + 1))) << 8;
    value = (value | (0xff & readByte(byteIndex)));

    // Return value
    return value;
  }

  /**
   * Reads a float at a specified index on this {@link ReadOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return float value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default float readFloatBE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    return Float.intBitsToFloat(readIntBE(byteIndex));
  }

  /**
   * Reads a float at a specified index on this {@link ReadOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return float value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default float readFloatLE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    return Float.intBitsToFloat(readIntLE(byteIndex));
  }

  /**
   * Reads a double at a specified index on this {@link ReadOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return double value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default double readDoubleBE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    return Double.longBitsToDouble(readLongBE(byteIndex));
  }

  /**
   * Reads a double at a specified index on this {@link ReadOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link ReadOnlyByteArray}
   * @return double value
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the
   *                                            length of the byte array
   */
  default double readDoubleLE(@Nonnegative long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    return Double.longBitsToDouble(readLongLE(byteIndex));
  }

  /**
   * Reads bytes from this {@link ReadOnlyByteArray} starting at specified byte index and writes the bytes to
   * destination {@link WriteOnlyByteArray}.
   *
   * @param byteIndex   non-negative index on this {@link ReadOnlyByteArray}
   * @param destination destination {@link WriteOnlyByteArray} to be written to while reading the
   *                    {@link ReadOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds or if destination
   *                                            {@link WriteOnlyByteArray} is too large
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
   * Reads bytes from this {@link ReadOnlyByteArray} starting at specified byte index and writes the bytes to
   * destination {@link WriteOnlyByteArray} in a reverse order meaning first bytes on this {@link ReadOnlyByteArray}
   * will be written to last byte positions of the destination {@link WriteOnlyByteArray} and fill the destination
   * {@link WriteOnlyByteArray} to its first byte positions.
   * <p>
   * This call is equivalent to calling normal @{code read(long,WriteOnlyByteArray)} then reversing the {@link WriteOnlyByteArray}.
   *
   * @param byteIndex   non-negative index on this {@link ReadOnlyByteArray}
   * @param destination destination {@link WriteOnlyByteArray} to be written to while reading the
   *                    {@link ReadOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds or if destination
   *                                            {@link WriteOnlyByteArray} is too large
   * @throws ByteArrayLengthOverBoundsException thrown if destination byte array goes over the bounds
   */
  default void readReversed(@Nonnegative long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, this.size());

    // Manual Copy
    long destinationIndex = destination.size() - 1;
    for(long offset = 0; offset < destination.size(); offset++){
      destination.writeByte(destinationIndex--, this.readByte(byteIndex + offset));
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
   * @return new {@link ReadOnlyByteArray} as a subset or same {@link ReadOnlyByteArray} if start is 0 and length is
   * equal to current {@link ReadOnlyByteArray}'s size
   * @throws ByteArrayIndexOutOfBoundsException thrown if start or length is out of the bounds
   * @throws ByteArrayInvalidLengthException    thrown if the length is negative
   * @throws ByteArrayLengthOverBoundsException thrown if length of new subset {@link ReadOnlyByteArray} goes over the
   * bounds
   */
  @Nonnull
  @Override
  ReadOnlyByteArray subsetOf(@Nonnegative long start, @Nonnegative long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException;
}
