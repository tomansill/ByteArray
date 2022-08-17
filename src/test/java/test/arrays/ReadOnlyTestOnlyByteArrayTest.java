package test.arrays;

import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ReadOnlyByteArray;
import test.ReadOnlyByteArray64BitTest;
import test.ReadOnlyByteArrayWithOtherByteArrayTest;

import javax.annotation.Nonnull;

public class ReadOnlyTestOnlyByteArrayTest
  implements ReadOnlyByteArray64BitTest, TestOnlyByteArrayTest, ReadOnlyByteArrayWithOtherByteArrayTest{

  @Nonnull
  @Override
  public ReadOnlyByteArray createTestReadOnlyByteArray(long size){
    return new TestOnlyByteArray(size);
  }

  @Override
  public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

    // Check if TestByteArray
    if(!(testByteArray instanceof TestOnlyByteArray)) throw new RuntimeException();

    // Write
    try{
      ((TestOnlyByteArray) testByteArray).writeByte(byteIndex, value);
    }catch(ByteArrayIndexOutOfBoundsException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isReadableWritableOK(){
    return true;
  }
}
