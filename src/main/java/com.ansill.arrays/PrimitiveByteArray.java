package com.ansill.arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.ansill.arrays.IndexingUtility.checkRead;
import static com.ansill.arrays.IndexingUtility.checkReadWriteByte;
import static com.ansill.arrays.IndexingUtility.checkSubsetOf;
import static com.ansill.arrays.IndexingUtility.checkWrite;

/** {@link ReadableWritableByteArray} implementation using primitive byte array as backing data */
final class PrimitiveByteArray implements ReadableWritableByteArray, ReadOnlyByteArray{

  /** Logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(PrimitiveByteArray.class);

  /** Starting index of byte array */
  @Nonnegative
  final int start;

  /** Byte array as the data */
  @Nonnull
  final byte[] data;

  /** Size of the data on byte array */
  @Nonnegative
  final int size;

  /**
   * Constructor to use input byte array as full {@link ReadableWritableByteArray}
   *
   * @param data byte array data
   * @throws IllegalArgumentException thrown if the length of data array is zero
   */
  PrimitiveByteArray(@Nonnull byte[] data) throws IllegalArgumentException{
    if(data.length == 0) throw new IllegalArgumentException("The length of data is zero");
    this.data = data;
    this.start = 0;
    this.size = data.length;
  }

  /**
   * Constructor to use input byte array as subset {@link ReadableWritableByteArray}
   * <p>
   * <i>Note: All of the input parameters are trusted. The constructor will not perform any checks on the parameter values.</i>
   *
   * @param data   byte array data
   * @param start  starting index of byte array
   * @param length length of byte array
   */
  private PrimitiveByteArray(@Nonnull byte[] data, @Nonnegative int start, @Nonnegative int length){
    this.data = data;
    this.start = start;
    this.size = length;
  }

  /**
   * Directly copies byte arrays inside of {@link PrimitiveByteArray}s for maximum performance.
   *
   * @param source           source ByteArray to copy from
   * @param sourceStart      starting index on source byte array to start copying from
   * @param destination      destination ByteArray to copy to
   * @param destinationStart starting index on destination byte array to start copying to
   * @param size             amount of bytes to copy
   */
  private static void copy(
    @Nonnull PrimitiveByteArray source,
    @Nonnegative long sourceStart,
    @Nonnull PrimitiveByteArray destination,
    long destinationStart,
    long size
  ){
    System.arraycopy(
      source.data,
      (int) (source.start + sourceStart),
      destination.data,
      (int) (destination.start + destinationStart),
      (int) size
    );
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
    checkReadWriteByte(byteIndex, size);
    return data[(int) (start + byteIndex)];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void read(
    @Nonnegative long byteIndex,
    @Nonnull WriteOnlyByteArray destination
  ) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, this.size());

    // Check if destination is a wrapper, if it is a wrapper, unwrap it
    while(destination instanceof WriteOnlyByteArrayWrapper){
      destination = ((WriteOnlyByteArrayWrapper) destination).original;
    }

    // Check if destination is indeed a PrimitiveByteArray, then we can access directly for faster copying
    if(destination instanceof PrimitiveByteArray){

      // Cast
      var direct = (PrimitiveByteArray) destination;

      // Copy
      copy(this, byteIndex, direct, 0, destination.size());
    }

    // Check if destination is indeed a ByteBufferByteArray, then we can access directly for faster copying
    else if(destination instanceof ByteBufferByteArray){

      // Cast
      var bbbaDestination = (ByteBufferByteArray) destination;

      // Get bytebuffer
      var bbDestination = bbbaDestination.data.duplicate();

      // Read
      bbDestination.put(this.data, (int) (this.start + byteIndex), (int) bbbaDestination.size());
    }

    // Check if destination is indeed a ReadableWritableMultipleByteArray, then we can access directly for faster copying
    else if(destination instanceof ReadableWritableMultipleByteArray){

      // Subset this bytearray
      var subsetted = this.subsetOf(byteIndex, destination.size());

      // use MBA's write function
      destination.write(0, subsetted);
    }else{

      // Otherwise, use manual copy. Warn about it through logger
      LOGGER.warn(
        "No implementation found to handle efficient bulk copy for {}. Using manual per-byte copy.",
        destination.getClass().getName()
      );
      for(long index = 0; index < destination.size(); index++){
        destination.writeByte(index, data[(int) (start + byteIndex + index)]);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, size);
    data[(int) (start + byteIndex)] = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(@Nonnegative long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkWrite(byteIndex, source, size);

    // Check if source is a wrapper, if it is, unwrap it
    while(source instanceof ReadOnlyByteArrayWrapper) source = ((ReadOnlyByteArrayWrapper) source).original;

    // Check if source is indeed a PrimitiveByteArray, then we can access directly for faster copying
    if(source instanceof PrimitiveByteArray){
      var direct = (PrimitiveByteArray) source;
      copy(direct, 0, this, byteIndex, source.size());
    }

    // Check if source is indeed a ByteBufferByteArray, then we can access directly for faster copying
    else if(source instanceof ByteBufferByteArray){

      // Cast
      var bbbaSource = (ByteBufferByteArray) source;

      // Get bytebuffer
      var bbSource = bbbaSource.data.duplicate();

      // Write
      bbSource.get(this.data, (int) byteIndex, (int) bbbaSource.size());
    }

    // Check if source is indeed a ReadableWritableMultipleByteArray or ReadOnlyMultipleByteArray, then we can access directly for faster copying
    else if(source instanceof ReadableWritableMultipleByteArray || source instanceof ReadOnlyMultipleByteArray){

      // Subset this bytearray
      var subsetted = this.subsetOf(byteIndex, source.size());

      // use MBA's read function
      source.read(0, subsetted);
    }else{

      // Otherwise, use manual copy. Warn about it through logger
      LOGGER.warn(
        "No implementation found to handle efficient bulk copy for {}. Using manual per-byte copy.",
        source.getClass().getName()
      );
      for(long index = 0; index < source.size(); index++){
        data[(int) (start + byteIndex + index)] = source.readByte(index);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{
    if(start == 0 && length == this.size) return this;
    checkSubsetOf(start, length, this.size());
    return new PrimitiveByteArray(data, (int) (this.start + start), (int) length);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString(){

    // Size (Limit to 128 elements for performance reasons)
    int size = (int) Long.min(128, this.size());

    // Stringbuilder
    var sb = new StringBuilder();
    sb.append(this.getClass().getSimpleName())
      .append("(size=")
      .append(this.size())
      .append(", content=[");

    // Go over the bytes
    for(int index = 0; index < size; index++){

      // Read
      byte value = data[start + index];

      // Convert to hex
      var hexValue = Long.toHexString(value & 0xffL);

      // Prefix if one char
      if(hexValue.length() == 1) hexValue = "0" + hexValue;

      // Append hex
      sb.append(hexValue);

      // Append underscore
      if(index != size - 1) sb.append("_");
    }

    // If truncated, then add ellipsis
    if(size != this.size()) sb.append("...");

    // Close it
    sb.append("])");

    // Build string and return
    return sb.toString();
  }
}
