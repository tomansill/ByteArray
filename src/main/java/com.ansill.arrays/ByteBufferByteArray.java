package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.ansill.arrays.IndexingUtility.checkRead;
import static com.ansill.arrays.IndexingUtility.checkReadWriteByte;
import static com.ansill.arrays.IndexingUtility.checkSubsetOf;
import static com.ansill.arrays.IndexingUtility.checkWrite;

/** ReadableWritableByteArray implementation using ByteBuffer as backing data */
class ByteBufferByteArray implements ReadableWritableByteArray{

  /** ByteBuffer data */
  @Nonnull
  final ByteBuffer data;

  /**
   * Constructor
   *
   * @param data bytebuffer data
   */
  ByteBufferByteArray(@Nonnull ByteBuffer data){
    // TODO check against zero-sized bytebuffers?
    this.data = data.duplicate();
  }

  /**
   * Directly copies byte arrays inside of byte arrays
   *
   * @param source      source ByteArray to copy from
   * @param byteIndex   starting index on source to start reading
   * @param destination destination ByteArray to copy to
   * @param offset      offset on destination ByteArray to start writing
   * @param length      amount of bytes to copy between source to destination ByteArrays
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

  @Override
  public long size(){
    return this.data.limit() - this.data.position();
  }

  @Override
  public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, this.size());
    return this.data.get((int) (this.data.position() + byteIndex));
  }

  @Override
  public void read(long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkRead(byteIndex, destination, this.size());

    // Check if destination is indeed PrimitiveByteArray, then we can access directly for faster copying
    if(destination instanceof ByteBufferByteArray){
      ByteBufferByteArray direct = (ByteBufferByteArray) destination;
      copy(this, byteIndex, direct, 0, destination.size());
      return;
    }

    // If wrapper, then check if inner element is PrimitiveByteArray
    if(destination instanceof WriteOnlyByteArrayWrapper){
      WriteOnlyByteArrayWrapper wrapper = (WriteOnlyByteArrayWrapper) destination;
      if(wrapper.original instanceof ByteBufferByteArray){
        ByteBufferByteArray direct = (ByteBufferByteArray) wrapper.original;
        copy(this, byteIndex, direct, 0, destination.size());
        return;
      }
    }

    // Otherwise, use default
    ReadableWritableByteArray.super.read(byteIndex, destination);
  }

  @Override
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, this.size());
    this.data.put((int) (this.data.position() + byteIndex), value);
  }

  @Override
  public void write(long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    checkWrite(byteIndex, source, this.size());

    // Check if source is indeed PrimitiveByteArray, then we can access directly for faster copying
    if(source instanceof ByteBufferByteArray){
      ByteBufferByteArray direct = (ByteBufferByteArray) source;
      copy(direct, 0, this, byteIndex, source.size());
      return;
    }

    // If wrapper, then check if inner element is PrimitiveByteArray
    if(source instanceof ReadOnlyByteArrayWrapper){
      ReadOnlyByteArrayWrapper wrapper = (ReadOnlyByteArrayWrapper) source;
      if(wrapper.original instanceof ByteBufferByteArray){
        ByteBufferByteArray direct = (ByteBufferByteArray) wrapper.original;
        copy(direct, 0, this, byteIndex, source.size());
        return;
      }
    }

    // Otherwise, use default copying
    ReadableWritableByteArray.super.write(byteIndex, source);
  }

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
