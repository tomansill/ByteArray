package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Utility to provide convenient indexing checks functions */
public final class IndexingUtility{

  /** Private constructor that will throw AssertionError */
  private IndexingUtility(){
    throw new AssertionError("Utility class");
  }

  /**
   * Combines variadic arguments into a list. Requires at least one argument.
   *
   * @param first first element
   * @param rest  rest of variadic elements
   * @param <T>   type of return list
   * @return a list containing the elements present in variadic arguments
   * @throws IllegalArgumentException thrown if any of the elements are null
   */
  @SafeVarargs
  @Nonnull
  static <T> List<T> combineVariadic(@Nonnull T first, @Nonnull T... rest) throws IllegalArgumentException{

    // Check first
    //noinspection ConstantConditions
    if(first == null) throw new IllegalArgumentException("first element is null");

    // Check rest
    //noinspection ConstantConditions
    if(rest == null) throw new IllegalArgumentException("rest array is null");
    for(T t : rest) if(t == null) throw new IllegalArgumentException("null elements in rest array");

    // Create list
    ArrayList<T> list = new ArrayList<>(1 + rest.length);
    list.add(first);
    Collections.addAll(list, rest);

    // Return list
    return list;
  }


  /**
   * Combines variadic arguments into a list. Requires at least two arguments.
   *
   * @param first  first element
   * @param second second element
   * @param rest   rest of variadic elements
   * @param <T>    type of return list
   * @return a list containing the elements present in variadic arguments
   * @throws IllegalArgumentException thrown if any of the elements are null
   */
  @SafeVarargs
  @Nonnull
  static <T> List<T> combineVariadic(@Nonnull T first, @Nonnull T second, @Nonnull T... rest)
  throws IllegalArgumentException{

    // Check first
    //noinspection ConstantConditions
    if(first == null) throw new IllegalArgumentException("first element is null");

    // Check second
    //noinspection ConstantConditions
    if(second == null) throw new IllegalArgumentException("second element is null");

    // Check rest
    //noinspection ConstantConditions
    if(rest == null) throw new IllegalArgumentException("rest array is null");
    for(T t : rest) if(t == null) throw new IllegalArgumentException("null elements in rest array");

    // Create list
    ArrayList<T> list = new ArrayList<>(2 + rest.length);
    list.add(first);
    list.add(second);
    Collections.addAll(list, rest);

    // Return list
    return list;

  }

  /**
   * Checks read/write byte call and throw any exception if anything is wrong with arguments or exit if all arguments are valid
   *
   * @param byteIndex       byte index
   * @param sizeOfByteArray size of byte array. <i>Note that this is a trusted parameter. The function will not check if this is valid or not. This parameter should not come directly from user's input.</i>
   * @throws ByteArrayIndexOutOfBoundsException thrown if the index is out of bounds
   */
  public static void checkReadWriteByte(long byteIndex, @Nonnegative long sizeOfByteArray)
  throws ByteArrayIndexOutOfBoundsException{
    if(byteIndex < 0){
      throw new ByteArrayIndexOutOfBoundsException("Byte index is negative. Byte index is " + byteIndex + ".");
    }
    if(byteIndex >= sizeOfByteArray){
      throw new ByteArrayIndexOutOfBoundsException(
        "The byte index goes over the size limit of ByteArray. The byte index is " +
        byteIndex +
        ". The size of ByteArray is " +
        sizeOfByteArray +
        ".");
    }
  }

  /**
   * Checks read/write call and throw any exception if anything is wrong with arguments or exit if all arguments are valid
   *
   * @param byteIndex       byte index
   * @param sizeOfValue     size of the value being read/written in bytes
   * @param sizeOfByteArray size of byte array. <i>Note that this is a trusted parameter. The function will not check if this is valid or not. This parameter should not come directly from user's input.</i>
   * @throws ByteArrayIndexOutOfBoundsException thrown if the index is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the value cannot be fully extracted as it goes over the length of the byte array
   */
  public static void checkReadWrite(long byteIndex, @Nonnegative int sizeOfValue, @Nonnegative long sizeOfByteArray)
  throws ByteArrayIndexOutOfBoundsException{
    if(byteIndex < 0){
      throw new ByteArrayIndexOutOfBoundsException("Byte index is negative. Byte index is " + byteIndex + ".");
    }
    if(byteIndex >= sizeOfByteArray){
      throw new ByteArrayIndexOutOfBoundsException(
        "The byte index goes over the size limit of ByteArray. The byte index is " +
        byteIndex +
        ". The size of ByteArray is " +
        sizeOfByteArray +
        ".");
    }
    if(byteIndex + sizeOfValue > sizeOfByteArray){
      throw new ByteArrayLengthOverBoundsException(
        "The size of value exceeds the size limit of ByteArray. The byte index is " +
        byteIndex +
        ". The size of value is " +
        sizeOfValue +
        ". The size of ByteArray is " +
        sizeOfByteArray +
        ".");
    }
  }

  /**
   * Checks read call and throw any exception if anything is wrong with arguments. Or exit if all arguments are valid
   *
   * @param byteIndex       byte index
   * @param destination     destination byte array
   * @param sizeOfByteArray size of current byte array <i>Note that this is a trusted parameter. The function will not check if this is valid or not. This parameter should not come directly from user's input.</i>
   * @throws ByteArrayIndexOutOfBoundsException thrown if index out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if size of byte array exceeds
   */
  public static void checkRead(
    long byteIndex,
    @Nullable WriteOnlyByteArray destination,
    @Nonnegative long sizeOfByteArray
  ) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    if(byteIndex < 0){
      throw new ByteArrayIndexOutOfBoundsException("Byte index is negative. Byte index is " + byteIndex + ".");
    }
    if(byteIndex >= sizeOfByteArray){
      throw new ByteArrayIndexOutOfBoundsException(
        "The byte index goes over the size limit of ByteArray. The byte index is " +
        byteIndex +
        ". The size of ByteArray is " +
        sizeOfByteArray +
        ".");
    }
    if(destination == null) throw new IllegalArgumentException("Destination ByteArray is null");
    if(destination.size() + byteIndex > sizeOfByteArray){
      throw new ByteArrayLengthOverBoundsException(
        "The combination of byte index and length overlaps ByteArray's size. Byte Index: "
        + byteIndex + " Length: "
        + destination.size() + " ByteArray size: "
        + sizeOfByteArray + " Amount of bytes over: "
        + ((byteIndex + destination.size()) - sizeOfByteArray) + "."
      );
    }
  }

  /**
   * Checks subsetof call. Throws exceptions if there's any errors with arguments. Exits if all is correct.
   *
   * @param start      starting index
   * @param length     length of new subset
   * @param actualSize actual size of byte array <i>Note that this is a trusted parameter. The function will not check if this is valid or not. This parameter should not come directly from user's input.</i>
   * @throws ByteArrayIndexOutOfBoundsException thrown if index is invalid
   * @throws ByteArrayInvalidLengthException    thrown if length is invalid
   * @throws ByteArrayLengthOverBoundsException thrown if length is over the bounds
   */
  public static void checkSubsetOf(long start, long length, @Nonnegative long actualSize)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayInvalidLengthException, ByteArrayLengthOverBoundsException{
    if(start < 0){
      throw new ByteArrayIndexOutOfBoundsException("Start index is negative. Start index is " + start + ".");
    }
    if(start >= actualSize){
      throw new ByteArrayIndexOutOfBoundsException(
        "The byte index goes over the size limit of ByteArray. The byte index is " +
        start +
        ". The size of ByteArray is " +
        actualSize +
        ".");
    }
    if(length < 0) throw new ByteArrayInvalidLengthException("Length is negative. Length is " + length + ".");
    if(length == 0) throw new ByteArrayInvalidLengthException("Length is zero. Length is " + length + ".");
    if(length + start > actualSize){
      throw new ByteArrayLengthOverBoundsException(
        "The combination of byte index and length overlaps ByteArray's size. Byte Index: "
        + start + " Length: "
        + length + " ByteArray size: "
        + actualSize + " Amount of bytes over: "
        + ((start + length) - actualSize) + "."
      );
    }
  }

  /**
   * Checks write call. Throws exception on any errors. Exits if all arguments are correct.
   *
   * @param byteIndex       index
   * @param source          source array
   * @param sizeOfByteArray size of actual byte array <i>Note that this is a trusted parameter. The function will not check if this is valid or not. This parameter should not come directly from user's input.</i>
   * @throws ByteArrayIndexOutOfBoundsException thrown if index is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the length is overlapping.
   */
  public static void checkWrite(
    long byteIndex,
    @Nullable ReadOnlyByteArray source,
    @Nonnegative long sizeOfByteArray
  ) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    if(byteIndex < 0){
      throw new ByteArrayIndexOutOfBoundsException("Byte index is negative. Byte index is " + byteIndex + ".");
    }
    if(byteIndex >= sizeOfByteArray){
      throw new ByteArrayIndexOutOfBoundsException(
        "The byte index goes over the size limit of ByteArray. The byte index is " +
        byteIndex +
        ". The size of ByteArray is " +
        sizeOfByteArray +
        ".");
    }
    if(source == null) throw new IllegalArgumentException("Source ByteArray is null");
    if(source.size() + byteIndex > sizeOfByteArray){
      throw new ByteArrayLengthOverBoundsException(
        "The combination of byte index and length overlaps ByteArray's size. Byte Index: "
        + byteIndex + " Length: "
        + source.size() + " ByteArray size: "
        + sizeOfByteArray + " Amount of bytes over: "
        + ((byteIndex + source.size()) - sizeOfByteArray) + "."
      );
    }
  }

}
