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
   * Writes a short at a specified byte index on this {@link WriteOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     short value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeShortBE(@Nonnegative long byteIndex, short value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 2, this.size());

    // Two write calls
    writeByte(byteIndex, (byte) ((0xff00 & value) >> 8));
    writeByte(byteIndex + 1, (byte) (0xff & value));
  }

  /**
   * Writes a short at a specified byte index on this {@link WriteOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     short value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeShortLE(@Nonnegative long byteIndex, short value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 2, this.size());

    // Two write calls
    writeByte(byteIndex + 1, (byte) ((0xff00 & value) >> 8));
    writeByte(byteIndex, (byte) (0xff & value));
  }

  /**
   * Writes an int at a specified byte index on this {@link WriteOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     int value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeIntBE(@Nonnegative long byteIndex, int value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 4, this.size());

    // Four write calls
    writeByte(byteIndex, (byte) (value >>> 24));
    writeByte(byteIndex + 1, (byte) (value >>> 16));
    writeByte(byteIndex + 2, (byte) (value >>> 8));
    writeByte(byteIndex + 3, (byte) value);
  }

  /**
   * Writes an int at a specified byte index on this {@link WriteOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     int value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeIntLE(@Nonnegative long byteIndex, int value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 4, this.size());

    // Four write calls
    writeByte(byteIndex + 3, (byte) (value >>> 24));
    writeByte(byteIndex + 2, (byte) (value >>> 16));
    writeByte(byteIndex + 1, (byte) (value >>> 8));
    writeByte(byteIndex, (byte) value);
  }

  /**
   * Writes a long at a specified byte index on this {@link WriteOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     long value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeLongBE(@Nonnegative long byteIndex, long value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 8, this.size());

    // Eight write calls
    writeByte(byteIndex, (byte) (value >>> 56));
    writeByte(byteIndex + 1, (byte) (value >>> 48));
    writeByte(byteIndex + 2, (byte) (value >>> 40));
    writeByte(byteIndex + 3, (byte) (value >>> 32));
    writeByte(byteIndex + 4, (byte) (value >>> 24));
    writeByte(byteIndex + 5, (byte) (value >>> 16));
    writeByte(byteIndex + 6, (byte) (value >>> 8));
    writeByte(byteIndex + 7, (byte) value);
  }

  /**
   * Writes a long at a specified byte index on this {@link WriteOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     long value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeLongLE(@Nonnegative long byteIndex, long value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    IndexingUtility.checkReadWrite(byteIndex, 8, this.size());

    // Eight write calls
    writeByte(byteIndex + 7, (byte) (value >>> 56));
    writeByte(byteIndex + 6, (byte) (value >>> 48));
    writeByte(byteIndex + 5, (byte) (value >>> 40));
    writeByte(byteIndex + 4, (byte) (value >>> 32));
    writeByte(byteIndex + 3, (byte) (value >>> 24));
    writeByte(byteIndex + 2, (byte) (value >>> 16));
    writeByte(byteIndex + 1, (byte) (value >>> 8));
    writeByte(byteIndex, (byte) value);
  }

  /**
   * Writes a float at a specified byte index on this {@link WriteOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     float value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeFloatBE(@Nonnegative long byteIndex, float value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    writeIntBE(byteIndex, Float.floatToIntBits(value));
  }

  /**
   * Writes a float at a specified byte index on this {@link WriteOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     float value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeFloatLE(@Nonnegative long byteIndex, float value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    writeIntLE(byteIndex, Float.floatToIntBits(value));
  }

  /**
   * Writes a double at a specified byte index on this {@link WriteOnlyByteArray} in Big Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     double value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeDoubleBE(@Nonnegative long byteIndex, double value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    writeLongBE(byteIndex, Double.doubleToLongBits(value));
  }

  /**
   * Writes a double at a specified byte index on this {@link WriteOnlyByteArray} in Little Endian byte order
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param value     double value to be written to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully written as it goes over the length
   *                                            of the byte array
   */
  default void writeDoubleLE(@Nonnegative long byteIndex, double value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    writeLongLE(byteIndex, Double.doubleToLongBits(value));
  }

  /**
   * Writes bytes from source {@link ReadOnlyByteArray} to this {@link WriteOnlyByteArray} starting at specified byte
   * index on {@link WriteOnlyByteArray}
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param source    source {@link ReadOnlyByteArray} to copy bytes to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds or if source
   * {@link ReadOnlyByteArray} is too large
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
   * Writes bytes from source {@link ReadOnlyByteArray} to this {@link WriteOnlyByteArray} starting at specified byte
   * index on {@link WriteOnlyByteArray} in a reverse order meaning first bytes on this {@link WriteOnlyByteArray}
   * will be written from last byte positions of the source {@link ReadOnlyByteArray} and continue to be written
   * with bytes from source {@link ReadOnlyByteArray} until its first byte positions.
   *
   * @param byteIndex non-negative index on this {@link WriteOnlyByteArray}
   * @param source    source {@link ReadOnlyByteArray} to copy bytes to this {@link WriteOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of bounds or if source
   *                                            {@link ReadOnlyByteArray} is too large
   * @throws ByteArrayLengthOverBoundsException thrown if length goes over the bounds
   */
  default void writeReversed(@Nonnegative long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkWrite(byteIndex, source, this.size());

    // Manual copy
    long pos = source.size() - 1;
    for(long index = 0; index < source.size(); index++){
      this.writeByte(byteIndex + index, source.readByte(pos--));
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
