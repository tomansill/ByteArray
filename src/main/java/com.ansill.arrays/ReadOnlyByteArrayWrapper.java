package com.ansill.arrays;

import javax.annotation.Nonnull;

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
  public short readShortBE(long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    return original.readShortBE(byteIndex);
  }

  @Override
  public int readIntBE(long byteIndex) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    return original.readIntBE(byteIndex);
  }

  @Override
  public long readLongBE(long byteIndex) throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    return original.readLongBE(byteIndex);
  }

  @Override
  public float readFloatBE(long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    return original.readFloatBE(byteIndex);
  }

  @Override
  public double readDoubleBE(long byteIndex)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{
    return original.readDoubleBE(byteIndex);
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
    return original.toString();
  }
}
