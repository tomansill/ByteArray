package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.TreeMap;

import static com.ansill.arrays.IndexingUtility.checkRead;
import static com.ansill.arrays.IndexingUtility.checkReadWrite;
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

  /**
   * Performs a read on this {@link ReadOnlyByteArray} to get a byte value at specified index
   *
   * @param indexMap  index map used to retrieve a byte
   * @param byteIndex index of byte
   * @param <T>       type that extends {@link ReadOnlyByteArray}
   * @return byte
   * @throws ByteArrayIndexOutOfBoundsException thrown if byteIndex is out of the bounds
   */
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

  /**
   * Performs a read on this {@link ReadOnlyByteArray} and copy the resulting bytes to destination
   * {@link WriteOnlyByteArray}.
   *
   * @param indexMap    map of byte arrays with indices
   * @param byteIndex   byte index on where to start reading
   * @param destination destination {@link WriteOnlyByteArray}
   * @param <T>         class that extends {@link ReadOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if index is out of the bounds
   * @throws ByteArrayLengthOverBoundsException thrown if copying the values to destination byte array will go outside
   *                                            the bounds
   */
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

  /**
   * Performs a read on this {@link ReadOnlyByteArray} and copy the resulting bytes to destination
   * {@link WriteOnlyByteArray} by filling WriteOnlyByteArray in reverse order.
   *
   * @param indexMap    map of byte arrays with indices
   * @param byteIndex   byte index on where to start reading
   * @param destination destination {@link WriteOnlyByteArray}
   * @param <T>         class that extends {@link ReadOnlyByteArray}
   * @throws ByteArrayIndexOutOfBoundsException thrown if index is out of the bounds
   * @throws ByteArrayLengthOverBoundsException thrown if copying the values to destination byte array will go outside
   *                                            the bounds
   */
  static <T extends ReadOnlyByteArray> void innerReadReversed(
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
      byteArray.readReversed(
        relativeByteIndex,
        destination.subsetOf((destination.size() - 1) - (destination.size() - remainingLength), lenToCopy)
      );

      // Adjust relative byte index and remaining length
      relativeByteIndex = Long.max(0, relativeByteIndex - byteArray.size() - lenToCopy);
      remainingLength -= lenToCopy;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long size(){
    return size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{

    // Check parameter
    checkReadWriteByte(byteIndex, size);

    // Read it
    return innerReadByte(indexMap, byteIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public short readShortBE(long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 2, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(2);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.getShort();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public short readShortLE(long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 2, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(2);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.order(ByteOrder.LITTLE_ENDIAN).getShort();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int readIntBE(long byteIndex) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 4, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(4);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.getInt();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int readIntLE(long byteIndex) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 4, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(4);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.order(ByteOrder.LITTLE_ENDIAN).getInt();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long readLongBE(long byteIndex) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 8, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(8);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.getLong();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long readLongLE(long byteIndex) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 8, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(8);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.order(ByteOrder.LITTLE_ENDIAN).getLong();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public float readFloatBE(long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 4, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(4);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.getFloat();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public float readFloatLE(long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 4, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(4);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.order(ByteOrder.LITTLE_ENDIAN).getFloat();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double readDoubleBE(long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 8, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(8);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.getDouble();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double readDoubleLE(long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 8, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(8);

    // Read
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, new ByteBufferByteArray(bb));

    // Read the results
    return bb.order(ByteOrder.LITTLE_ENDIAN).getDouble();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void read(long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, size);

    // Pass it over
    innerRead(indexMap, byteIndex, destination);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readReversed(long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, size);

    // Pass it over
    innerReadReversed(indexMap, byteIndex, destination);
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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
