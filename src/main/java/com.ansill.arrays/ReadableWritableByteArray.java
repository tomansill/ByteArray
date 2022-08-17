package com.ansill.arrays;

import javax.annotation.Nonnull;

/**
 * Fully Readable and Writable interface of ByteArray
 */
public interface ReadableWritableByteArray extends ReadOnlyByteArray, WriteOnlyByteArray, ByteArray{

  /**
   * Converts this ReadableWritableByteArray to ReadOnlyByteArray
   *
   * @return ReadOnlyByteArray
   */
  @Nonnull
  default ReadOnlyByteArray toReadOnly(){
    return new ReadOnlyByteArrayWrapper(this);
  }

  /**
   * Converts this ReadableWritableByteArray to WriteOnlyByteArray
   *
   * @return WriteOnlyByteArray
   */
  @Nonnull
  default WriteOnlyByteArray toWriteOnly(){
    return new WriteOnlyByteArrayWrapper(this);
  }

  // Shim to get around inheritance woes
  @Nonnull
  @Override
  ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException;
}
