package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

import static com.ansill.arrays.IndexingUtility.*;

/** ReadableWritableByteArray implementation using primitive byte array as backing data */
final class PrimitiveByteArray implements ReadableWritableByteArray, ReadOnlyByteArray{

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
   * Constructor to use input byte array as full ByteArray
   *
   * @param data byte array data
   */
  PrimitiveByteArray(@Nonnull byte[] data){
    this.data = data;
    this.start = 0;
    this.size = data.length;
  }

  /**
   * Constructor to use input byte array as subset ByteArray
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
   * Directly copies byte arrays inside of byte arrays
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

  @Override
  public long size(){
    return size;
  }

  @Override
  public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, size);
    return data[(int) (start + byteIndex)];
  }

  @Override
  public void read(
    @Nonnegative long byteIndex,
    @Nonnull WriteOnlyByteArray destination
  ) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, this.size());

    // Check if destination is a wrapper, if it is a wrapper, unwrap it
    if(destination instanceof WriteOnlyByteArrayWrapper){
      destination = ((WriteOnlyByteArrayWrapper) destination).original;
    }

    // Check if destination is indeed PrimitiveByteArray, then we can access directly for faster copying
    if(destination instanceof PrimitiveByteArray){

      // Cast
      PrimitiveByteArray direct = (PrimitiveByteArray) destination;

      // Copy
      copy(this, byteIndex, direct, 0, destination.size());

      // Exit
      return;
    }

    // Check if destination is indeed ByteBufferByteArray, then we can access directly for faster copying
    if(destination instanceof ByteBufferByteArray){

      // Cast
      ByteBufferByteArray bbbaDestination = (ByteBufferByteArray) destination;

      // Get bytebuffer
      ByteBuffer bbDestination = bbbaDestination.data.duplicate();

      // Read
      bbDestination.put(this.data, (int) (this.start + byteIndex), (int) bbbaDestination.size());

      // Exit
      return;
    }

    // Otherwise, use manual Copy
    for(long index = 0; index < destination.size(); index++){
      destination.writeByte(index, this.readByte(byteIndex + index));
    }
  }

  @Override
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, size);
    data[(int) (start + byteIndex)] = value;
  }

  @Override
  public void write(long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkWrite(byteIndex, source, size);

    // Check if source is a wrapper, if it is, unwrap it
    if(source instanceof ReadOnlyByteArrayWrapper) source = ((ReadOnlyByteArrayWrapper) source).original;

    // Check if source is indeed PrimitiveByteArray, then we can access directly for faster copying
    if(source instanceof PrimitiveByteArray){
      PrimitiveByteArray direct = (PrimitiveByteArray) source;
      copy(direct, 0, this, byteIndex, source.size());
      return;
    }

    // Check if source is indeed ByteBufferByteArray, then we can access directly for faster copying
    if(source instanceof ByteBufferByteArray){

      // Cast
      ByteBufferByteArray bbbaSource = (ByteBufferByteArray) source;

      // Get bytebuffer
      ByteBuffer bbSource = bbbaSource.data.duplicate();

      // Write
      bbSource.get(this.data, (int) byteIndex, (int) bbbaSource.size());

      // Exit
      return;
    }

    // Manual copy as default
    for(long index = 0; index < source.size(); index++){
      this.writeByte(byteIndex + index, source.readByte(index));
    }
  }

  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{
    if(start == 0 && length == this.size) return this;
    checkSubsetOf(start, length, this.size());
    return new PrimitiveByteArray(data, (int) (this.start + start), (int) length);
  }

  @Override
  public String toString(){

    // Size (Limit to 128 elements for performance reasons)
    int size = (int) Long.min(128, this.size());

    // Stringbuilder
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getSimpleName())
      .append("(size=")
      .append(this.size())
      .append(", content=[");

    // Go over the bytes
    for(int index = 0; index < size; index++){

      // Read
      byte value = data[start + index];

      // Convert to hex
      String hexValue = Long.toHexString(value & 0xffL);

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
