package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static com.ansill.arrays.IndexingUtility.combineVariadic;

/**
 * {@link ByteArray} interface with utility functions for creating {@link ByteArray}s
 * <p>
 * This interface provides the base implementation for handling {@link ByteArray}s and static methods for creating {@link ByteArray}s
 */
public interface ByteArray{

  /**
   * Combines multiple {@link ReadOnlyByteArray}s into a single {@link ReadOnlyByteArray}
   * <p>
   * If a list containing a single {@link ReadOnlyByteArray} is passed in, then that same {@link ReadOnlyByteArray} will be returned.
   * <p>
   * If there's any {@link ReadableWritableByteArray}s in the input list, it will be converted to {@link ReadOnlyByteArray} using {@link ReadableWritableByteArray}::toReadOnly method.
   * <p>
   *
   * @param byteArrays a list of {@link ReadOnlyByteArray} to be combined
   * @return a single {@link ReadOnlyByteArray}
   */
  @Nonnull
  static ReadOnlyByteArray combineReadOnly(@Nonnull List<ReadOnlyByteArray> byteArrays){

    // Assert non-empty
    //noinspection ConstantConditions
    if(byteArrays == null) throw new IllegalArgumentException("ByteArrays list is null");
    if(byteArrays.isEmpty()) throw new IllegalArgumentException("ByteArrays list is empty");
    for(var byteArray : byteArrays){
      if(byteArray == null) throw new IllegalArgumentException("There is a null element in the ByteArray list");
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
   * Combines multiple {@link ReadOnlyByteArray}s into a single {@link ReadOnlyByteArray}
   * <p>
   * If there's any {@link ReadableWritableByteArray}s in the input arguments, it will be converted to {@link ReadOnlyByteArray} using {@link ReadableWritableByteArray}::toReadOnly method.
   * <p>
   * Example:
   * <pre>{@code
   * var twoCombo = ByteArray.combine(ByteArray.wrap(new byte[25]).toReadOnly(), ByteArray.wrap(ByteBuffer.allocate(25)).toReadOnly());
   * var moreCombo = ByteArray.combine(ByteArray.wrap(new byte[25]).toReadOnly(), ByteArray.wrap(ByteBuffer.allocate(25)).toReadOnly(), twoCombo);
   * }</pre>
   *
   * @param firstByteArray  first byte array to be combined
   * @param secondByteArray second byte array to be combined
   * @param restByteArrays  rest of byte arrays to be combined. <i>(Optional. Can be blank.)</i>
   * @return a single {@link ReadOnlyByteArray}
   */
  @Nonnull
  static ReadOnlyByteArray combine(
    @Nonnull ReadOnlyByteArray firstByteArray,
    @Nonnull ReadOnlyByteArray secondByteArray,
    @Nonnull ReadOnlyByteArray... restByteArrays
  ){

    // Combine parameters
    var byteArrays = combineVariadic(firstByteArray, secondByteArray, restByteArrays);

    // Make sure all are readonly. Convert if any of them is not
    for(int index = 0; index < byteArrays.size(); index++){
      var byteArray = byteArrays.get(index);
      if(byteArray instanceof ReadableWritableByteArray){
        byteArray = ((ReadableWritableByteArray) byteArray).toReadOnly();
        byteArrays.set(index, byteArray);
      }
    }

    // Return
    return new ReadOnlyMultipleByteArray(byteArrays);
  }

  /**
   * Combines multiple {@link ReadableWritableByteArray}s into a single {@link ReadableWritableByteArray}
   * <p>
   *   If a list containing a single {@link ReadableWritableByteArray} is passed in, then that same {@link ReadableWritableByteArray} will be returned.
   * <p>
   * @param byteArrays a list of {@link ReadableWritableByteArray}s to be combined
   * @return a single {@link ReadableWritableByteArray}
   */
  @Nonnull
  static ReadableWritableByteArray combineReadableWritable(@Nonnull List<ReadableWritableByteArray> byteArrays){

    // Assert non-empty
    //noinspection ConstantConditions
    if(byteArrays == null) throw new IllegalArgumentException("ByteArrays list is null");
    if(byteArrays.isEmpty()) throw new IllegalArgumentException("ByteArrays list is empty");
    for(var byteArray : byteArrays){
      if(byteArray == null) throw new IllegalArgumentException("There is a null element in the ByteArray list");
    }

    // If only one, return that one
    if(byteArrays.size() == 1) return byteArrays.get(0);

    // Return
    return new ReadableWritableMultipleByteArray(byteArrays);
  }

  /**
   * Combines multiple {@link ReadableWritableByteArray}s into a single {@link ReadableWritableByteArray}
   * <p>
   *   Example:
   * <pre>{@code
   * var twoCombo = ByteArray.combine(ByteArray.wrap(new byte[25]), ByteArray.wrap(ByteBuffer.allocate(25)));
   * var moreCombo = ByteArray.combine(ByteArray.wrap(new byte[25]), ByteArray.wrap(ByteBuffer.allocate(25)), twoCombo);
   * }</pre>
   *
   * @param firstByteArray  first byte array to be combined
   * @param secondByteArray second byte array to be combined
   * @param restByteArrays  rest of byte arrays to be combined. <i>(Optional. Can be blank.)</i>
   * @return a single {@link ReadableWritableByteArray}
   */
  @Nonnull
  static ReadableWritableByteArray combine(
    @Nonnull ReadableWritableByteArray firstByteArray,
    @Nonnull ReadableWritableByteArray secondByteArray,
    @Nonnull ReadableWritableByteArray... restByteArrays
  ){

    // Combine parameters
    var byteArrays = combineVariadic(firstByteArray, secondByteArray, restByteArrays);

    // Return
    return new ReadableWritableMultipleByteArray(byteArrays);
  }

  /**
   * Wraps one or more primitive byte arrays into {@link ReadableWritableByteArray}
   * <p>
   *   Example:
   * <pre>{@code
   * var byteArrayOfOneByteArray = ByteArray.wrap(new byte[100]);
   * var byteArrayOfTwoByteArrays = ByteArray.wrap(new byte[25], new byte[75]);
   * var byteArrayOfMoreByteArrays = ByteArray.wrap(new byte[25], new byte[23], new byte[33], new byte[10]);
   * }</pre>
   *
   * @param firstByteArray first primitive byte array to be wrapped
   * @param restByteArrays more primitive byte arrays to be wrapped. <i>(Optional. Can be blank.)</i>
   * @return a single {@link ReadableWritableByteArray} containing all byte arrays that has been passed in.
   */
  @Nonnull
  static ReadableWritableByteArray wrap(@Nonnull byte[] firstByteArray, @Nonnull byte[]... restByteArrays){
    var bytesList = combineVariadic(firstByteArray, restByteArrays);
    if(bytesList.size() == 1) return new PrimitiveByteArray(bytesList.get(0));
    return new ReadableWritableMultipleByteArray(bytesList.stream()
                                                          .map(PrimitiveByteArray::new)
                                                          .collect(Collectors.toList()));
  }

  /**
   * Wraps one or more {@link ByteBuffer}s into {@link ReadableWritableByteArray}
   * <p>
   *   Note that {@link ByteBuffer}'s position() and limit() markers will be respected. The {@link ByteBuffer} will
   *   be duplicated using duplicate() method so any changes to position() and limit() markers on the original
   *   {@link ByteBuffer} will not affect the wrapped {@link ByteBuffer}.
   * <p>
   *   Example:
   * <pre>{@code
   * var byteArrayOfOneByteArray = ByteArray.wrap(ByteBuffer.allocate(100));
   * var byteArrayOfTwoByteArrays = ByteArray.wrap(ByteBuffer.allocate(25), ByteBuffer.allocate(75));
   * var byteArrayOfMoreByteArrays = ByteArray.wrap(ByteBuffer.allocate(25), ByteBuffer.allocate(23), ByteBuffer.allocate(33), ByteBuffer.allocate(10));
   * }</pre>
   *
   * @param firstByteBuffer first {@link ByteBuffer} to be wrapped
   * @param restByteBuffers more {@link ByteBuffer}s to be wrapped. <i>(Optional. Can be blank.)</i>
   * @return a single {@link ReadableWritableByteArray} containing all {@link ByteBuffer}s that has been passed in.
   */
  @Nonnull
  static ReadableWritableByteArray wrap(@Nonnull ByteBuffer firstByteBuffer, @Nonnull ByteBuffer... restByteBuffers){

    // Combine
    var byteBuffers = combineVariadic(firstByteBuffer, restByteBuffers);

    // Make sure all buffers are NOT readonly
    for(var byteBuffer : byteBuffers){
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
