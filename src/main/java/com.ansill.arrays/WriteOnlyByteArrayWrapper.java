package com.ansill.arrays;

import javax.annotation.Nonnull;

/** Wrapper that wraps ReadableWritableByteArray into WriteOnlyByteArray that will lock out any accesses that is not related to writing */
class WriteOnlyByteArrayWrapper implements WriteOnlyByteArray{

  /** Original ByteArray */
  @Nonnull
  final ReadableWritableByteArray original;

  /**
   * Constructor
   *
   * @param original original ByteArray
   */
  WriteOnlyByteArrayWrapper(@Nonnull ReadableWritableByteArray original){
    this.original = original;
  }

  @Override
  public long size(){
    return original.size();
  }

  @Override
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{
    original.writeByte(byteIndex, value);
  }

  @Override
  public void writeShort(long byteIndex, short value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    original.writeShort(byteIndex, value);
  }

  @Override
  public void writeInt(long byteIndex, int value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    original.writeInt(byteIndex, value);
  }

  @Override
  public void writeLong(long byteIndex, long value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    original.writeLong(byteIndex, value);
  }

  @Override
  public void writeFloat(long byteIndex, float value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    original.writeFloat(byteIndex, value);
  }

  @Override
  public void writeDouble(long byteIndex, double value)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    original.writeDouble(byteIndex, value);
  }

  @Override
  public void write(long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    original.write(byteIndex, source);
  }

  @Nonnull
  @Override
  public WriteOnlyByteArray subsetOf(long start, long length)
  throws
    ByteArrayIndexOutOfBoundsException,
    ByteArrayLengthOverBoundsException,
    ByteArrayInvalidLengthException{
    if(start == 0 && length == size()) return this;
    return new WriteOnlyByteArrayWrapper(original.subsetOf(start, length));
  }

  @Override
  public String toString(){
    return original.toString();
  }
}
