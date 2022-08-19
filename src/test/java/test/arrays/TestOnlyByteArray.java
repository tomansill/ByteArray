package test.arrays;

import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ByteArrayInvalidLengthException;
import com.ansill.arrays.ByteArrayLengthOverBoundsException;
import com.ansill.arrays.IndexingUtility;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.WriteOnlyByteArray;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.ansill.arrays.IndexingUtility.checkReadWriteByte;

public class TestOnlyByteArray implements ReadableWritableByteArray{

  public final long start;

  public final long size;

  public final ByteBuffer[] data;

  public TestOnlyByteArray(long size){
    if(size == 0) throw new IllegalArgumentException();
    start = 0;
    this.size = size;
    int amountOfByteAs = (int) Math.ceil((size * 1.0) / Integer.MAX_VALUE);
    this.data = new ByteBuffer[amountOfByteAs];
    int index = 0;
    while(size > 0){
      int amount = (int) Long.min(size, Integer.MAX_VALUE);
      data[index++] = ByteBuffer.allocateDirect(amount);
      size -= amount;
    }
  }

  protected TestOnlyByteArray(long start, long size, ByteBuffer[] data){
    this.start = start;
    this.size = size;
    this.data = data;
  }

  @Nonnull
  @Override
  public ReadOnlyByteArray toReadOnly(){
    return new ReadOnly(this.start, size, data);
  }

  @Nonnull
  @Override
  public WriteOnlyByteArray toWriteOnly(){
    return new WriteOnly(this.start, size, data);
  }

  @Override
  public long size(){
    return size;
  }

  @Override
  public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, size);
    byteIndex += start;
    for(ByteBuffer bb : data){
      int len = bb.limit() - bb.position();
      if(byteIndex >= len) byteIndex -= len;
      else return bb.get((int) byteIndex);
    }
    throw new RuntimeException();
  }

  @Override
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, size);
    byteIndex += start;
    for(ByteBuffer bb : data){
      int len = bb.limit() - bb.position();
      if(byteIndex >= len) byteIndex -= len;
      else{
        bb.put((int) byteIndex, value);
        return;
      }
    }
    throw new RuntimeException();
  }

  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{
    if(start == 0 && length == size) return this;
    IndexingUtility.checkSubsetOf(start, length, this.size);
    return new TestOnlyByteArray(this.start + start, length, data);
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
      byte value = this.readByte(index);

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
    return ReadableWritableByteArray.class.getSimpleName() +
           "(size=" +
           this.size() +
           ", content=[" +
           String.join("_", bytes) +
           "])";
  }

  public static class ReadOnly implements ReadOnlyByteArray{

    @Nonnull
    public final TestOnlyByteArray original;

    protected ReadOnly(long start, long size, ByteBuffer[] data){
      this.original = new TestOnlyByteArray(start, size, data);
    }

    @Override
    public long size(){
      return original.size();
    }

    @Override
    public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
      return original.readByte(byteIndex);
    }

    @Nonnull
    @Override
    public ReadOnlyByteArray subsetOf(long start, long length)
    throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

      // Return self
      if(start == 0 && length == this.size()) return this;

      // Check
      IndexingUtility.checkSubsetOf(start, length, original.size());

      // Modify and return
      return new ReadOnly(original.start + start, length, original.data);
    }
  }

  public static class WriteOnly extends TestOnlyByteArray implements WriteOnlyByteArray{

    protected WriteOnly(long start, long size, ByteBuffer[] data){
      super(start, size, data);
    }
  }
}
