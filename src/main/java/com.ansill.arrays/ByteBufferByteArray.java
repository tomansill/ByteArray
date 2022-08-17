package com.ansill.arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.ansill.arrays.IndexingUtility.checkRead;
import static com.ansill.arrays.IndexingUtility.checkReadWriteByte;
import static com.ansill.arrays.IndexingUtility.checkSubsetOf;
import static com.ansill.arrays.IndexingUtility.checkWrite;

/** {@link ReadableWritableByteArray} implementation using {@link ByteBuffer} as the backing data */
class ByteBufferByteArray implements ReadableWritableByteArray{

  /** Logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufferByteArray.class);

  /** {@link ByteBuffer} data that backs this {@link ReadableWritableByteArray} */
  @Nonnull
  final ByteBuffer data;

  /**
   * Constructor
   *
   * @param data {@link ByteBuffer} data for this {@link ReadableWritableByteArray}
   * @throws IllegalArgumentException thrown if the size of data {@link ByteBuffer} is zero
   */
  ByteBufferByteArray(@Nonnull ByteBuffer data) throws IllegalArgumentException{
    if(data.limit() - data.position() == 0) throw new IllegalArgumentException(
      "Buffer's position and limit markers amounts to zero length. This is not allowed");
    this.data = data.duplicate();
  }

  /**
   * Directly copies {@link ByteBuffer}s inside of {@link ByteBufferByteArray}s for maximum performance
   *
   * @param source      source {@link ByteBufferByteArray} to copy from
   * @param byteIndex   starting index on source {@link ByteBufferByteArray} to start reading
   * @param destination destination {@link ByteBufferByteArray} to copy to
   * @param offset      offset on destination {@link ByteBufferByteArray} to start writing
   * @param length      amount of bytes to copy between source to destination {@link ByteBufferByteArray}
   */
  private static void copy(
    @Nonnull ByteBufferByteArray source,
    @Nonnegative long byteIndex,
    @Nonnull ByteBufferByteArray destination,
    @Nonnegative long offset,
    @Nonnegative long length
  ){
    ByteBuffer view = source.data.asReadOnlyBuffer();
    view.position((int) (view.position() + byteIndex));
    view.limit((int) (view.position() + length));
    ByteBuffer destCopy = destination.data.duplicate();
    destCopy.position((int) (destCopy.position() + offset));
    destCopy.put(view);
  }

  @Nonnull
  private static ByteBufferByteArray convert(@Nonnull PrimitiveByteArray primitiveByteArray){

    // Create ByteBuffer
    var ba = ByteBuffer.wrap(primitiveByteArray.data);

    // Adjust the limits
    ba.position(primitiveByteArray.start);
    ba.limit(primitiveByteArray.start + primitiveByteArray.size);

    // Wrap in ByteBufferByteArray
    return new ByteBufferByteArray(ba);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long size(){
    return this.data.limit() - this.data.position();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, this.size());
    return this.data.get((int) (this.data.position() + byteIndex));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void read(long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, this.size());

    // If wrapper, unwrap it
    while(destination instanceof WriteOnlyByteArrayWrapper){
      destination = ((WriteOnlyByteArrayWrapper) destination).original;
    }

    // Check if destination is indeed a ByteBufferByteArray, then we can access directly for faster copying
    if(destination instanceof ByteBufferByteArray){
      var direct = (ByteBufferByteArray) destination;
      copy(this, byteIndex, direct, 0, destination.size());
      return;
    }

    // Check if destination is indeed a PrimitiveByteArray, then we can access directly for faster copying
    if(destination instanceof PrimitiveByteArray){

      // Copy it
      copy(this, byteIndex, convert((PrimitiveByteArray) destination), 0, destination.size());

      // Exit
      return;
    }

    // Check if destination is indeed a ReadableWritableMultipleByteArray, then we can access directly for faster copying
    if(destination instanceof ReadableWritableMultipleByteArray){

      // TODO
    }

    // Manual Copy
    if(LOGGER.isWarnEnabled() && !destination.getClass().getName().contains("TestOnly")){
      LOGGER.warn(
        "No implementation found to handle efficient bulk copy for {}. Using manual per-byte copy.",
        destination.getClass().getName()
      );
    }
    for(long index = 0; index < destination.size(); index++){
      destination.writeByte(index, this.readByte(byteIndex + index));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, this.size());
    this.data.put((int) (this.data.position() + byteIndex), value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkWrite(byteIndex, source, this.size());

    // Check if source is a wrapper, unwrap it if it is a wrapper
    while(source instanceof ReadOnlyByteArrayWrapper){
      source = ((ReadOnlyByteArrayWrapper) source).original;
    }

    // Check if source is indeed a ByteBufferByteArray, then we can access directly for faster copying
    if(source instanceof ByteBufferByteArray){
      var direct = (ByteBufferByteArray) source;
      copy(direct, 0, this, byteIndex, source.size());
      return;
    }

    // Check if source is indeed a PrimitiveByteArray, then we can access directly for faster copying
    if(source instanceof PrimitiveByteArray){

      // Copy it
      copy(convert((PrimitiveByteArray) source), 0, this, byteIndex, source.size());

      // Exit
      return;
    }

    // Check if source is indeed a ReadOnlyMultipleByteArray, then we can access directly for faster copying
    if(source instanceof ReadOnlyMultipleByteArray){

      // TODO
    }

    // Check if source is indeed a ReadableWritableMultipleByteArray, then we can access directly for faster copying
    if(source instanceof ReadableWritableMultipleByteArray){

      // TODO
    }

    // Manual copy
    if(LOGGER.isWarnEnabled() && !source.getClass().getName().contains("TestOnly")){
      LOGGER.warn(
        "No implementation found to handle efficient bulk copy for {}. Using manual per-byte copy.",
        source.getClass().getName()
      );
    }
    for(long index = 0; index < source.size(); index++){
      this.writeByte(byteIndex + index, source.readByte(index));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{
    if(this.data.position() == start && this.size() == length) return this;
    checkSubsetOf(start, length, this.size());
    ByteBuffer newByteBuffer = this.data.duplicate();
    int localPosition = (int) (this.data.position() + start);
    newByteBuffer.position(localPosition);
    newByteBuffer.limit((int) (localPosition + length));
    return new ByteBufferByteArray(newByteBuffer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString(){

    // Size
    int size = (int) Long.min(128, this.size());

    // List of bytes as hex
    List<String> bytes = new ArrayList<>(size);

    // Go over the bytes
    for(int index = 0; index < size; index++){

      // Read
      byte value = this.data.get(this.data.position() + index);

      // Convert to hex
      String hexValue = Long.toHexString(value & 0xffL);

      // Prefix if one char
      if(hexValue.length() == 1) hexValue = "0" + hexValue;

      // Add to list
      bytes.add(hexValue);
    }

    // If truncated, then add ellipsis
    if(size != this.size()) bytes.add("...");

    // Build string and return
    return this.getClass().getSimpleName() + "(size=" + this.size() + ", content=[" + String.join("_", bytes) + "])";
  }
}
