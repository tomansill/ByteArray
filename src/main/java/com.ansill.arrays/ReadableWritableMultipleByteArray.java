package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import static com.ansill.arrays.IndexingUtility.checkRead;
import static com.ansill.arrays.IndexingUtility.checkReadWrite;
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
    var ro = new ArrayList<ReadOnlyByteArray>();
    for(var byteArray : byteArrays){
      if(byteArray instanceof com.ansill.arrays.ReadableWritableMultipleByteArray){
        var innerByteArrays = ((com.ansill.arrays.ReadableWritableMultipleByteArray) byteArray).indexMap.values();
        for(var innerByteArray : innerByteArrays){
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
    return ReadOnlyMultipleByteArray.innerReadByte(indexMap, byteIndex);
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
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{

    // Check parameter
    checkReadWriteByte(byteIndex, size);

    // Get ByteArray
    long index = byteIndex;
    var byteArray = indexMap.get(index);
    if(byteArray == null){
      var entry = indexMap.floorEntry(index);
      index = entry.getKey();
      byteArray = entry.getValue();
    }

    // Normalize the byteIndex to local index
    long localIndex = byteIndex - index;

    // Get it
    byteArray.writeByte(localIndex, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeShortBE(long byteIndex, short value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 2, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(2);

    // Write value on it
    bb.putShort(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeShortLE(long byteIndex, short value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 2, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(2);

    // Write value on it
    bb.order(ByteOrder.LITTLE_ENDIAN).putShort(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeIntBE(long byteIndex, int value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 4, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(4);

    // Write value on it
    bb.putInt(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeIntLE(long byteIndex, int value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 4, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(4);

    // Write value on it
    bb.order(ByteOrder.LITTLE_ENDIAN).putInt(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeLongBE(long byteIndex, long value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 8, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(8);

    // Write value on it
    bb.putLong(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeLongLE(long byteIndex, long value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 8, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(8);

    // Write value on it
    bb.order(ByteOrder.LITTLE_ENDIAN).putLong(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeFloatBE(long byteIndex, float value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 4, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(4);

    // Write value on it
    bb.putFloat(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeFloatLE(long byteIndex, float value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 4, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(4);

    // Write value on it
    bb.order(ByteOrder.LITTLE_ENDIAN).putFloat(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeDoubleBE(long byteIndex, double value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 8, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(8);

    // Write value on it
    bb.putDouble(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeDoubleLE(long byteIndex, double value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check
    checkReadWrite(byteIndex, 8, size);

    // Use ByteBuffer
    var bb = ByteBuffer.allocate(8);

    // Write value on it
    bb.order(ByteOrder.LITTLE_ENDIAN).putDouble(value).flip();

    // Write
    innerWrite(byteIndex, new ByteBufferByteArray(bb));
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
    ReadOnlyMultipleByteArray.innerRead(indexMap, byteIndex, destination);
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
    ReadOnlyMultipleByteArray.innerReadReversed(indexMap, byteIndex, destination);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkWrite(byteIndex, source, size);

    // Pass it over
    innerWrite(byteIndex, source);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeReversed(long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkWrite(byteIndex, source, size);

    // Pass it over
    innerWriteReversed(byteIndex, source);
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
    var submap = indexMap.subMap(floorIndex, true, byteIndex + source.size(), false);

    // Adjust the byteIndex to match submap
    long relativeByteIndex = byteIndex - floorIndex;

    // Loop through the submap
    long remainingLength = source.size();
    for(var byteArray : submap.values()){

      // Determine the amount of bytes to copy
      long lenToCopy = Long.min(byteArray.size() - relativeByteIndex, remainingLength);

      // Subset and write
      byteArray.write(relativeByteIndex, source.subsetOf(source.size() - remainingLength, lenToCopy));

      // Adjust relative byte index and remaining length
      relativeByteIndex = Long.max(0, relativeByteIndex - byteArray.size() - lenToCopy);
      remainingLength -= lenToCopy;
    }
  }

  /**
   * Inner write in reverse order
   *
   * @param byteIndex byte index to start the write
   * @param source    source byte array to copy the data from
   * @throws ByteArrayIndexOutOfBoundsException thrown if the index is out of bounds
   * @throws ByteArrayLengthOverBoundsException thrown if the length is over the bounds
   */
  private void innerWriteReversed(final long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

    // Get floor index
    long floorIndex = indexMap.floorKey(byteIndex);

    // Get submap
    var submap = indexMap.subMap(floorIndex, true, byteIndex + source.size(), false);

    // Adjust the byteIndex to match submap
    long relativeByteIndex = byteIndex - floorIndex;

    // Loop through the submap
    long remainingLength = source.size();
    for(var byteArray : submap.values()){

      // Determine the amount of bytes to copy
      long lenToCopy = Long.min(byteArray.size() - relativeByteIndex, remainingLength);

      // Subset and write
      byteArray.writeReversed(
        relativeByteIndex,
        source.subsetOf((source.size() - 1) - (source.size() - remainingLength), lenToCopy)
      );

      // Adjust relative byte index and remaining length
      relativeByteIndex = Long.max(0, relativeByteIndex - byteArray.size() - lenToCopy);
      remainingLength -= lenToCopy;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
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
    return new com.ansill.arrays.ReadableWritableMultipleByteArray(resultingArray);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public ReadOnlyByteArray toReadOnly(){
    return new ReadOnlyMultipleByteArray(readOnlyByteArrays);
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
