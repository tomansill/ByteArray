package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.ansill.arrays.IndexingUtility.checkRead;
import static com.ansill.arrays.IndexingUtility.checkReadWriteByte;
import static com.ansill.arrays.IndexingUtility.checkSubsetOf;
import static com.ansill.arrays.IndexingUtility.checkWrite;

/** ReadableWritableByteArray implementation that supports multiple byte arrays */
final class ReadableWritableMultipleByteArray implements ReadableWritableByteArray{

  /** Index map containing byte arrays */
  @Nonnull
  final TreeMap<Long,ReadableWritableByteArray> indexMap = new TreeMap<>();

  /** Size of this ByteArray */
  @Nonnegative
  private final long size;

  /** List of readonly byte arrays used for toReadOnly() method */
  @Nonnull
  private final List<ReadOnlyByteArray> readOnlyByteArrays;

  /**
   * Constructor
   *
   * @param byteArrays byte arrays used to create this multiple byte arrays
   */
  ReadableWritableMultipleByteArray(@Nonnull List<ReadableWritableByteArray> byteArrays){

    // Build index map
    long size = 0;

    // Iterate over byte arrays
    List<ReadOnlyByteArray> ro = new ArrayList<>();
    for(ReadableWritableByteArray byteArray : byteArrays){
      if(byteArray instanceof com.ansill.arrays.ReadableWritableMultipleByteArray){
        Collection<ReadableWritableByteArray> innerByteArrays = ((com.ansill.arrays.ReadableWritableMultipleByteArray) byteArray).indexMap.values();
        for(ReadableWritableByteArray innerByteArray : innerByteArrays){
          ro.add(innerByteArray.toReadOnly());
          indexMap.put(size, innerByteArray);
          size += innerByteArray.size();
        }
      }else{
        ro.add(byteArray.toReadOnly());
        indexMap.put(size, byteArray);
        size += byteArray.size();
      }
    }

    // Save
    this.readOnlyByteArrays = Collections.unmodifiableList(ro);

    // Set size
    this.size = size;
  }

  /**
   * Performs subset and returns list of bytearrays that made to the cut
   *
   * @param indexMap original index map
   * @param start    starting index
   * @param length   length of new subset
   * @param <T>      type of bytearray
   * @return list of bytearrays
   * @throws ByteArrayIndexOutOfBoundsException thrown if the index is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the length is over the bounds
   * @throws ByteArrayInvalidLengthException    thrown if the length is invalid
   */
  @SuppressWarnings("unchecked")
  @Nonnull
  static <T extends ByteArray> List<T> innerSubsetOf(
    @Nonnull TreeMap<Long,T> indexMap,
    final long start,
    final long length
  ) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

    // Set up the outgoing list
    List<T> list = new ArrayList<>();

    // Get floor key
    long floorKey = indexMap.floorKey(start);

    // Get submap
    var submap = indexMap.subMap(floorKey, true, start + length, false);

    // Adjust the start to be relative to submap
    long relativeStart = start - floorKey;

    // Process all
    long remainingLength = length;
    for(var byteArray : submap.values()){

      // Figure out the length needed to subset
      long len = Long.min(byteArray.size() - relativeStart, remainingLength);

      // Subset and add
      list.add((T) byteArray.subsetOf(relativeStart, len));

      // Adjust start and length
      relativeStart = Long.max(0, relativeStart - byteArray.size() - len);
      remainingLength -= len;
    }

    // Return the list
    return list;
  }

  @Override
  public long size(){
    return size;
  }

  @Override
  public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{

    // Check parameter
    checkReadWriteByte(byteIndex, size);

    // Read it
    return ReadOnlyMultipleByteArray.innerReadByte(indexMap, byteIndex);
  }

  @Override
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{

    // Check parameter
    checkReadWriteByte(byteIndex, size);

    // Get ByteArray
    long index = byteIndex;
    ReadableWritableByteArray byteArray = indexMap.get(index);
    if(byteArray == null){
      Map.Entry<Long,ReadableWritableByteArray> entry = indexMap.floorEntry(index);
      index = entry.getKey();
      byteArray = entry.getValue();
    }

    // Normalize the byteIndex to local index
    long localIndex = byteIndex - index;

    // Get it
    byteArray.writeByte(localIndex, value);
  }

  @Override
  public void read(long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, size);

    // Pass it over
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, destination);
  }

  @Override
  public void write(long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkWrite(byteIndex, source, size);

    // Pass it over
    innerWrite(byteIndex, source);
  }

  /**
   * Inner write
   *
   * @param byteIndex byte index to start the write
   * @param source    source byte array to copy the data from
   * @throws ByteArrayIndexOutOfBoundsException thrown if the index is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the length is over the bounds
   */
  private void innerWrite(final long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

    // Get floor index
    long floorIndex = indexMap.floorKey(byteIndex);

    // Get submap
    NavigableMap<Long,ReadableWritableByteArray> submap = indexMap.subMap(
      floorIndex,
      true,
      byteIndex + source.size(),
      false
    );

    // Adjust the byteIndex to match submap
    long relativeByteIndex = byteIndex - floorIndex;

    // Loop through the submap
    long remainingLength = source.size();
    for(ReadableWritableByteArray byteArray : submap.values()){

      // Determine the amount of bytes to copy
      long lenToCopy = Long.min(byteArray.size() - relativeByteIndex, remainingLength);

      // Subset and write
      byteArray.write(relativeByteIndex, source.subsetOf(source.size() - remainingLength, lenToCopy));

      // Adjust relative byte index and remaining length
      relativeByteIndex = Long.max(0, relativeByteIndex - byteArray.size() - lenToCopy);
      remainingLength -= lenToCopy;
    }
  }

  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

    // Immediately return if there's nothing to subset
    if(start == 0 && length == this.size()) return this;

    // Check parameters
    checkSubsetOf(start, length, size);

    // Calculate and subset
    List<ReadableWritableByteArray> resultingArray = innerSubsetOf(this.indexMap, start, length);

    // If only one element, just use that element
    if(resultingArray.size() == 1) return resultingArray.get(0);

    // Return new subset
    return new com.ansill.arrays.ReadableWritableMultipleByteArray(resultingArray);
  }

  @Nonnull
  @Override
  public ReadOnlyByteArray toReadOnly(){
    return new ReadOnlyMultipleByteArray(readOnlyByteArrays);
  }
}
