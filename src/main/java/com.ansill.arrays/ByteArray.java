package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static com.ansill.arrays.IndexingUtility.combineVariadic;

/**
 * ByteArray interface
 * <p>
 * This interface provides the base implementation for handling ByteArrays and static methods for creating ByteArrays
 */
public interface ByteArray{

  /**
   * Combines multiple ByteArrays into a single ReadOnlyByteArray
   *
   * @param byteArrays byte arrays to be combined
   * @return a single ReadOnlyByteArray
   */
  @Nonnull
  static ReadOnlyByteArray combineReadOnly(@Nonnull List<ReadOnlyByteArray> byteArrays){

    // Assert non-empty
    //noinspection ConstantConditions
    if(byteArrays == null) throw new IllegalArgumentException("byteArrays list is null");
    if(byteArrays.isEmpty()) throw new IllegalArgumentException("byteArrays list is empty");
    for(ReadOnlyByteArray byteArray : byteArrays){
      if(byteArray == null) throw new IllegalArgumentException("There is null element in the byteArray list");
    }

    // Make sure all byte arrays are ReadOnly, not ReadableWritable in disguise
    byteArrays = byteArrays.stream().map(byteArray -> {
      if(byteArray instanceof ReadableWritableByteArray) return ((ReadableWritableByteArray) byteArray).toReadOnly();
      else return byteArray;
    }).collect(Collectors.toList());

    // If only one, return that one
    if(byteArrays.size() == 1) return byteArrays.get(0);

    // Return
    return new ReadOnlyMultipleByteArray(byteArrays);
  }

  /**
   * Combines multiple ByteArrays into a single ReadOnlyByteArray
   *
   * @param firstByteArray  first byte array to be combined
   * @param secondByteArray second byte array to be combined
   * @param restByteArrays  rest of byte arrays to be combined
   * @return a single ReadOnlyByteArray
   */
  @Nonnull
  static ReadOnlyByteArray combineReadOnly(
    @Nonnull ReadOnlyByteArray firstByteArray,
    @Nonnull ReadOnlyByteArray secondByteArray,
    @Nonnull ReadOnlyByteArray... restByteArrays
  ){

    // Combine parameters
    List<ReadOnlyByteArray> byteArrays = combineVariadic(firstByteArray, secondByteArray, restByteArrays);

    // Make sure all are readonly. Convert if any of them is not
    for(int index = 0; index < byteArrays.size(); index++){
      ReadOnlyByteArray byteArray = byteArrays.get(index);
      if(byteArray instanceof ReadableWritableByteArray){
        byteArray = ((ReadableWritableByteArray) byteArray).toReadOnly();
        byteArrays.set(index, byteArray);
      }
    }

    // Return
    return new ReadOnlyMultipleByteArray(byteArrays);
  }

  /**
   * Combines multiple ByteArrays into a single ReadableWritableByteArray
   *
   * @param byteArrays byte arrays to be combined
   * @return a single ReadableWritableByteArray
   */
  @Nonnull
  static ReadableWritableByteArray combine(@Nonnull List<ReadableWritableByteArray> byteArrays){

    // Assert non-empty
    //noinspection ConstantConditions
    if(byteArrays == null) throw new IllegalArgumentException("byteArrays list is null");
    if(byteArrays.isEmpty()) throw new IllegalArgumentException("byteArrays list is empty");
    for(ReadOnlyByteArray byteArray : byteArrays){
      if(byteArray == null) throw new IllegalArgumentException("There is null element in the byteArray list");
    }

    // If only one, return that one
    if(byteArrays.size() == 1) return byteArrays.get(0);

    // Return
    return new ReadableWritableMultipleByteArray(byteArrays);
  }


  /**
   * Combines multiple ByteArrays into a single ReadableWritableByteArray
   *
   * @param firstByteArray  first byte array to be combined
   * @param secondByteArray second byte array to be combined
   * @param restByteArrays  rest of byte arrays to be combined
   * @return a single ReadableWritableByteArray
   */
  @Nonnull
  static ReadableWritableByteArray combine(
    @Nonnull ReadableWritableByteArray firstByteArray,
    @Nonnull ReadableWritableByteArray secondByteArray,
    @Nonnull ReadableWritableByteArray... restByteArrays
  ){

    // Combine parameters
    List<ReadableWritableByteArray> byteArrays = combineVariadic(firstByteArray, secondByteArray, restByteArrays);

    // Return
    return new ReadableWritableMultipleByteArray(byteArrays);
  }

  /**
   * Wraps one or more primitive byte arrays into ReadableWritableByteArray
   *
   * @param firstByteArray first primitive byte array to be wrapped
   * @param restByteArrays more primitive byte arrays to be wrapped
   * @return ReadableWritableByteArray
   */
  @Nonnull
  static ReadableWritableByteArray wrap(@Nonnull byte[] firstByteArray, @Nonnull byte[]... restByteArrays){
    List<byte[]> bytesList = combineVariadic(firstByteArray, restByteArrays);
    if(bytesList.size() == 1) return new PrimitiveByteArray(bytesList.get(0));
    return new ReadableWritableMultipleByteArray(bytesList.stream()
                                                          .map(PrimitiveByteArray::new)
                                                          .collect(Collectors.toList()));
  }

  /**
   * Wraps one or more ByteBuffers into ReadableWritableByteArray
   *
   * @param firstByteBuffer first ByteBuffer to be wrapped
   * @param restByteBuffers more ByteBuffers to be wrapped
   * @return ReadableWritableByteArray
   */
  @Nonnull
  static ReadableWritableByteArray wrap(@Nonnull ByteBuffer firstByteBuffer, @Nonnull ByteBuffer... restByteBuffers){

    // Combine
    List<ByteBuffer> byteBuffers = combineVariadic(firstByteBuffer, restByteBuffers);

    // Make sure all buffers are NOT readonly
    for(ByteBuffer byteBuffer : byteBuffers){
      if(byteBuffer.isReadOnly()) throw new IllegalArgumentException("ReadOnly ByteBuffer was passed in");
    }

    // If just one, return that one instead
    if(byteBuffers.size() == 1) return new ByteBufferByteArray(byteBuffers.get(0));

    // Return as multiple
    return new ReadableWritableMultipleByteArray(byteBuffers.stream()
                                                            .map(ByteBufferByteArray::new)
                                                            .collect(Collectors.toList()));
  }

  /**
   * Returns the size of bytes in this ByteArray
   *
   * @return the size of bytes as non-negative long value
   */
  @Nonnegative
  long size();

  /**
   * Creates a subset of this ByteArray with defined start and length.
   *
   * @param start  the starting point on this ByteArray to start the subset range
   * @param length the length of new subset after the start point
   * @param <T>    Type that extends ByteArray. Such as ReadOnlyByteArray, WriteOnlyByteArray, or ReadableWritableByteArray
   * @return Subset as ByteArray
   * @throws ByteArrayIndexOutOfBoundsException thrown if start or length is out of the bounds
   * @throws ByteArrayInvalidLengthException    thrown if the length is negative
   * @throws ByteArrayLengthOverBoundsException thrown if length goes over the bounds
   */
  @Nonnull
  <T extends ByteArray> T subsetOf(@Nonnegative long start, @Nonnegative long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException;

}
