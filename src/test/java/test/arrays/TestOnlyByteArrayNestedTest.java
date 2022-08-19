package test.arrays;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.TestUtility;
import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import test.ByteArrayTest;
import test.ReadOnlyByteArray64BitTest;
import test.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.ReadableWritableByteArray64BitTest;
import test.ReadableWritableByteArrayWithOtherByteArrayTest;
import test.WriteOnlyByteArray64BitTest;
import test.WriteOnlyByteArrayWithOtherByteArrayTest;

import javax.annotation.Nonnull;

@DisplayName("TestOnlyByteArray Test Suite")
public class TestOnlyByteArrayNestedTest{

  public interface TestOnlyByteArrayTest extends ByteArrayTest{

    @Override
    default void cleanTestByteArray(@Nonnull ByteArray byteArray){

      // Save size
      long size = byteArray.size();

      // Do a recursive clean
      TestUtility.clean(TestUtility.UNSAFE, byteArray);

      // Trigger a GC to blast away any unused stuff
      System.gc();

      // Log it if actually big
      if(size >= (Integer.MAX_VALUE * 0.5)) System.out.println("Cleared away " + size + "B");
    }

  }

  @Nested
  @DisplayName("ReadOnly test")
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

  @Nested
  @DisplayName("WriteOnly test")
  public class WriteOnlyTestOnlyByteArrayTest
    implements WriteOnlyByteArray64BitTest, TestOnlyByteArrayTest, WriteOnlyByteArrayWithOtherByteArrayTest{

    @Override
    public boolean isReadableWritableOK(){
      return true;
    }

    @Nonnull
    @Override
    public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
      return new TestOnlyByteArray(size).toWriteOnly();
    }

    @Override
    public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

      // Check if TestByteArray
      if(!(testByteArray instanceof TestOnlyByteArray)) throw new RuntimeException();

      // Write
      try{
        return ((TestOnlyByteArray) testByteArray).readByte(byteIndex);
      }catch(ByteArrayIndexOutOfBoundsException e){
        throw new RuntimeException(e);
      }
    }
  }

  @Nested
  @DisplayName("ReadableWritable test")
  public class ReadableWritableByteArrayTest
    implements ReadableWritableByteArray64BitTest, TestOnlyByteArrayTest,
    ReadableWritableByteArrayWithOtherByteArrayTest{

    @Override
    public boolean isReadableWritableOK(){
      return true;
    }

    @Nonnull
    @Override
    public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
      return new TestOnlyByteArray(size);
    }

    @Override
    public byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, long byteIndex){

      // Check if TestByteArray
      if(!(testByteArray instanceof TestOnlyByteArray)) throw new RuntimeException();

      // Write
      try{
        return ((TestOnlyByteArray) testByteArray).readByte(byteIndex);
      }catch(ByteArrayIndexOutOfBoundsException e){
        throw new RuntimeException(e);
      }
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
  }
}
