package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Utility to provide convenient Indexing checks */
public final class IndexingUtility{

  /** Private constructor that will throw AssertionError */
  private IndexingUtility(){
    throw new AssertionError("Utility class");
  }

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
