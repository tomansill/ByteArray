package test.arrays;

import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ByteArrayInvalidLengthException;
import com.ansill.arrays.ByteArrayLengthOverBoundsException;
import com.ansill.arrays.ReadableWritableByteArray;
import org.opentest4j.AssertionFailedError;

import javax.annotation.Nonnull;

public class DoNotTouchMeByteArray implements ReadableWritableByteArray{

  private final long size;

  public DoNotTouchMeByteArray(long size){
    this.size = size;
  }

  @Override
  public long size(){
    return size;
  }

  @Override
  public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
    throw new AssertionFailedError("No modification allowed on this byte array");
  }

  @Override
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{
    throw new AssertionFailedError("No modification allowed on this byte array");
  }

  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{
    throw new AssertionFailedError("No modification allowed on this byte array");
  }
}
