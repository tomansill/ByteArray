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

import static com.ansill.arrays.IndexingUtility.*;
import static com.ansill.arrays.MultipleByteArray.ReadableWritable.innerSubsetOf;

public interface MultipleByteArray{

  /** ReadableWritableByteArray implementation that supports multiple byte arrays */
  final class ReadableWritable implements ReadableWritableByteArray, MultipleByteArray{

    /** Index map containing byte arrays */
    @Nonnull
    final TreeMap<Long,ReadableWritableByteArray> indexMap = new TreeMap<>();

    /** Size of this ByteArray */
    @Nonnegative
    private final long size;

    /** List of readonly byte arrays used for getData() method */
    @Nonnull
    private final List<ReadOnlyByteArray> readOnlyByteArrays;

    /**
     * Constructor
     *
     * @param byteArrays byte arrays used to create this multiple byte arrays
     */
    ReadableWritable(@Nonnull List<ReadableWritableByteArray> byteArrays){

      // Build index map
      long size = 0;

      // Iterate over byte arrays
      List<ReadOnlyByteArray> ro = new ArrayList<>();
      for(ReadableWritableByteArray byteArray : byteArrays){
        if(byteArray instanceof ReadableWritable){
          Collection<ReadableWritableByteArray> innerByteArrays = ((ReadableWritable) byteArray).indexMap.values();
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
      NavigableMap<Long,T> submap = indexMap.subMap(floorKey, true, start + length, false);

      // Adjust the start to be relative to submap
      long relativeStart = start - floorKey;

      // Process all
      long remainingLength = length;
      for(T byteArray : submap.values()){

        // Figure out the length needed to subset
        long len = Long.min(byteArray.size() - relativeStart, remainingLength);

        // Subset and add
        list.add(byteArray.subsetOf(relativeStart, len));

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
      return ReadOnly.innerReadByte(indexMap, byteIndex);
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
    throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

      // Check parameters
      checkRead(byteIndex, destination, size);

      // Pass it over
      ReadOnly.innerRead(indexMap, byteIndex, destination);
    }

    @Override
    public void write(long byteIndex, @Nonnull ReadOnlyByteArray source)
    throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

      // Check parameters
      checkWrite(byteIndex, source, size);

      // Pass it over
      try{
        innerWrite(byteIndex, source);
      }catch(ByteArrayInvalidLengthException e){
        throw new RuntimeException(e); // TODO remove me when it becomes runtime exception
      }
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
      return new ReadableWritable(resultingArray);
    }

    @Nonnull
    @Override
    public ReadOnlyByteArray toReadOnly(){
      return new ReadOnly(readOnlyByteArrays);
    }
  }


  /** ReadOnlyByteArray implementation that supports multiple byte arrays */
  final class ReadOnly implements ReadOnlyByteArray, MultipleByteArray{

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
    ReadOnly(@Nonnull List<ReadOnlyByteArray> byteArrays){

      // Build index map
      long size = 0;

      // Iterate over byte arrays
      for(ReadOnlyByteArray byteArray : byteArrays){
        if(byteArray instanceof ReadableWritable){
          Collection<ReadableWritableByteArray> innerByteArrays = ((ReadableWritable) byteArray).indexMap.values();
          for(ReadableWritableByteArray innerByteArray : innerByteArrays){
            indexMap.put(size, innerByteArray.toReadOnly());
            size += innerByteArray.size();
          }
        }else if(byteArray instanceof ReadOnly){
          Collection<ReadOnlyByteArray> innerByteArrays = ((ReadOnly) byteArray).indexMap.values();
          for(ReadOnlyByteArray innerByteArray : innerByteArrays){
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

    private static <T extends ReadOnlyByteArray> byte innerReadByte(@Nonnull TreeMap<Long,T> indexMap, long byteIndex)
    throws ByteArrayIndexOutOfBoundsException{

      // Get ByteArray
      long index = byteIndex;
      T byteArray = indexMap.get(index);
      if(byteArray == null){
        Map.Entry<Long,T> entry = indexMap.floorEntry(index);
        index = entry.getKey();
        byteArray = entry.getValue();
      }

      // Normalize the byteIndex to local index
      long localIndex = byteIndex - index;

      // Get it
      return byteArray.readByte(localIndex);
    }

    private static <T extends ReadOnlyByteArray> void innerRead(
      @Nonnull TreeMap<Long,T> indexMap,
      long byteIndex,
      @Nonnull WriteOnlyByteArray destination
    ) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

      // Get floor index
      long floorIndex = indexMap.floorKey(byteIndex);

      // Get submap
      NavigableMap<Long,T> submap = indexMap.subMap(floorIndex, true, byteIndex + destination.size(), false);

      // Adjust the byteIndex to match submap
      long relativeByteIndex = byteIndex - floorIndex;

      // Loop through the submap
      long remainingLength = destination.size();
      for(T byteArray : submap.values()){

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
    throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

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
      List<ReadOnlyByteArray> resultingArray = innerSubsetOf(this.indexMap, start, length);

      // If only one element, just use that element
      if(resultingArray.size() == 1) return resultingArray.get(0);

      // Return new subset
      return new ReadOnly(resultingArray);
    }

    @Override
    public String toString(){

      // Determine the length needed to render (limit to 128 for performance reasons)
      final int length = (int) Long.min(128, this.size);

      // Set up stringbuilder
      StringBuilder sb = new StringBuilder();
      sb.append(this.getClass().getSimpleName()).append("(size=").append(size).append(", content=[");

      // Count down
      long byteIndex = 0;
      while(byteIndex < length){

        // Get bytearray
        Map.Entry<Long,ReadOnlyByteArray> entry = indexMap.floorEntry(byteIndex);

        // Get bytearray
        ReadOnlyByteArray byteArray = entry.getValue();

        // Determine the limits
        int limit = (int) Long.min(byteArray.size(), length - byteIndex);

        // Write to sb
        for(int i = 0; i < limit; i++){

          // Append slash
          if(byteIndex != 0) sb.append("_");

          // Get data
          byte value;
          try{
            value = byteArray.readByte(i);
          }catch(ByteArrayIndexOutOfBoundsException e){
            throw new RuntimeException(e); // TODO change me later when no longer checked exception
          }

          // Convert to hex
          String hexValue = Long.toHexString(value & 0xffL);

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
}
