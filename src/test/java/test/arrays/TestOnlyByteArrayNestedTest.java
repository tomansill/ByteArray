package test.arrays;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.TestUtility;
import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import test.BaseByteArrayTest;
import test.BaseReadableWritableByteArrayTest;
import test.other.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.other.ReadableWritableByteArrayWithOtherByteArrayTest;
import test.other.WriteOnlyByteArrayWithOtherByteArrayTest;
import test.self.SelfReadOnlyByteArray64BitTest;
import test.self.SelfWriteOnlyByteArray64BitTest;

import javax.annotation.Nonnull;

@DisplayName("TestOnlyByteArray Test Suite")
public class TestOnlyByteArrayNestedTest{

  public interface TestOnlyByteArrayTest extends BaseByteArrayTest{

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
    implements SelfReadOnlyByteArray64BitTest, TestOnlyByteArrayTest, ReadOnlyByteArrayWithOtherByteArrayTest{

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
    implements SelfWriteOnlyByteArray64BitTest, TestOnlyByteArrayTest, WriteOnlyByteArrayWithOtherByteArrayTest{

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
    implements TestOnlyByteArrayTest,
    ReadableWritableByteArrayWithOtherByteArrayTest, BaseReadableWritableByteArrayTest, SelfWriteOnlyByteArray64BitTest,
    SelfReadOnlyByteArray64BitTest{

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
