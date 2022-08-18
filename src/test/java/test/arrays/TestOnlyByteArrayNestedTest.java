package test.arrays;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.TestUtility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import test.ByteArrayTest;
import test.ReadOnlyByteArray64BitTest;
import test.ReadOnlyByteArrayWithOtherByteArrayTest;

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
}
