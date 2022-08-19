package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.TreeMap;

import static com.ansill.arrays.IndexingUtility.checkRead;
import static com.ansill.arrays.IndexingUtility.checkReadWriteByte;
import static com.ansill.arrays.IndexingUtility.checkSubsetOf;
import static com.ansill.arrays.ReadableWritableMultipleByteArray.innerSubsetOf;

/** ReadOnlyByteArray implementation that supports multiple byte arrays */
final class ReadOnlyMultipleByteArray implements ReadOnlyByteArray{

  /** Index map containing byte arrays */
  @Nonnull
  final TreeMap<Long,ReadOnlyByteArray> indexMap = new TreeMap<>();

  /** Size of this ByteArray */
  @Nonnegative
  private final long size;

  /**
   * Constructor
   *
   * @param byteArrays byte arrays used to create this multiple byte arrays
   */
  ReadOnlyMultipleByteArray(@Nonnull List<ReadOnlyByteArray> byteArrays){

    // Build index map
    long size = 0;

    // Iterate over byte arrays
    for(var byteArray : byteArrays){
      if(byteArray instanceof ReadableWritableMultipleByteArray){
        var innerByteArrays = ((ReadableWritableMultipleByteArray) byteArray).indexMap.values();
        for(var innerByteArray : innerByteArrays){
          indexMap.put(size, innerByteArray.toReadOnly());
          size += innerByteArray.size();
        }
      }else if(byteArray instanceof com.ansill.arrays.ReadOnlyMultipleByteArray){
        var innerByteArrays = ((com.ansill.arrays.ReadOnlyMultipleByteArray) byteArray).indexMap.values();
        for(var innerByteArray : innerByteArrays){
          indexMap.put(size, innerByteArray);
          size += innerByteArray.size();
        }
      }else{
        if(byteArray instanceof ReadableWritableByteArray){
          byteArray = ((ReadableWritableByteArray) byteArray).toReadOnly();
        }
        indexMap.put(size, byteArray);
        size += byteArray.size();
      }
    }

    // Save byte arrays to field

    // Set size
    this.size = size;
  }

  static <T extends ReadOnlyByteArray> byte innerReadByte(@Nonnull TreeMap<Long,T> indexMap, long byteIndex)
  throws ByteArrayIndexOutOfBoundsException{

    // Get ByteArray
    long index = byteIndex;
    T byteArray = indexMap.get(index);
    if(byteArray == null){
      var entry = indexMap.floorEntry(index);
      index = entry.getKey();
      byteArray = entry.getValue();
    }

    // Normalize the byteIndex to local index
    long localIndex = byteIndex - index;

    // Get it
    return byteArray.readByte(localIndex);
  }

  static <T extends ReadOnlyByteArray> void innerRead(
    @Nonnull TreeMap<Long,T> indexMap,
    long byteIndex,
    @Nonnull WriteOnlyByteArray destination
  ) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Get floor index
    long floorIndex = indexMap.floorKey(byteIndex);

    // Get submap
    var submap = indexMap.subMap(floorIndex, true, byteIndex + destination.size(), false);

    // Adjust the byteIndex to match submap
    long relativeByteIndex = byteIndex - floorIndex;

    // Loop through the submap
    long remainingLength = destination.size();
    for(var byteArray : submap.values()){

      // Determine the amount of bytes to copy
      long lenToCopy = Long.min(byteArray.size() - relativeByteIndex, remainingLength);

      // Subset and read
      byteArray.read(relativeByteIndex, destination.subsetOf(destination.size() - remainingLength, lenToCopy));

      // Adjust relative byte index and remaining length
      relativeByteIndex = Long.max(0, relativeByteIndex - byteArray.size() - lenToCopy);
      remainingLength -= lenToCopy;
    }
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
    return innerReadByte(indexMap, byteIndex);
  }

  @Override
  public void read(long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, size);

    // Pass it over
    innerRead(indexMap, byteIndex, destination);
  }

  @Nonnull
  @Override
  public ReadOnlyByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

    // Immediately return if there's nothing to subset
    if(start == 0 && length == this.size()) return this;

    // Check parameters
    checkSubsetOf(start, length, size);

    // Calculate and subset
    var resultingArray = innerSubsetOf(this.indexMap, start, length);

    // If only one element, just use that element
    if(resultingArray.size() == 1) return resultingArray.get(0);

    // Return new subset
    return new com.ansill.arrays.ReadOnlyMultipleByteArray(resultingArray);
  }

  @Override
  public String toString(){

    // Determine the length needed to render (limit to 128 for performance reasons)
    final int length = (int) Long.min(128, this.size);

    // Set up stringbuilder
    var sb = new StringBuilder();
    sb.append(this.getClass().getSimpleName()).append("(size=").append(size).append(", content=[");

    // Count down
    long byteIndex = 0;
    while(byteIndex < length){

      // Get bytearray
      var entry = indexMap.floorEntry(byteIndex);

      // Get bytearray
      var byteArray = entry.getValue();

      // Determine the limits
      int limit = (int) Long.min(byteArray.size(), length - byteIndex);

      // Write to sb
      for(int i = 0; i < limit; i++){

        // Append slash
        if(byteIndex != 0) sb.append("_");

        // Get data
        byte value = byteArray.readByte(i);

        // Convert to hex
        var hexValue = Long.toHexString(value & 0xffL);

        // Prefix if one char
        if(hexValue.length() == 1) hexValue = "0" + hexValue;

        // Write
        sb.append(hexValue);

        // Increment
        byteIndex++;
      }
    }

    // Add ellipsis if truncated
    if(length != this.size) sb.append("...");

    // Add cap and return
    sb.append("])");
    return sb.toString();
  }
}
