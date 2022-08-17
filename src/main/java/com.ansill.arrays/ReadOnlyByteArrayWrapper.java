package com.ansill.arrays;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/** Wrapper that wraps ReadableWritableByteArray into ReadOnlyByteArray that will lock out any accesses that is not related to reading */
class ReadOnlyByteArrayWrapper implements ReadOnlyByteArray{

  /** Original ByteArray */
  @Nonnull
  final ReadableWritableByteArray original;

  /**
   * Constructor
   *
   * @param original original ByteArray
   */
  ReadOnlyByteArrayWrapper(@Nonnull ReadableWritableByteArray original){
    this.original = original;
  }

  @Override
  public long size(){
    return original.size();
  }

  @Override
  public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
    return original.readByte(byteIndex);
  }

  @Override
  public void read(long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    original.read(byteIndex, destination);
  }

  @Nonnull
  @Override
  public ReadOnlyByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{
    if(start == 0 && length == size()) return this;
    return new ReadOnlyByteArrayWrapper(original.subsetOf(start, length));
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
      byte value;
      value = this.readByte(index);

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
    return ReadOnlyByteArray.class.getSimpleName() +
           "(size=" +
           this.size() +
           ", content=[" +
           String.join("_", bytes) +
           "])";
  }
}
