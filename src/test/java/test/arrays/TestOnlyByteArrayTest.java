package test.arrays;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.MultipleByteArrayTest;
import com.ansill.arrays.ReadOnlyByteArray;
import sun.misc.Unsafe;
import test.ByteArrayTest;
import test.ReadOnlyByteArray64BitTest;
import test.ReadOnlyByteArrayWithOtherByteArrayTest;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public interface TestOnlyByteArrayTest extends ByteArrayTest{

  @Override
  default void cleanTestByteArray(@Nonnull ByteArray byteArray){

    // Save size
    long size = byteArray.size();

    // Unsafe is needed to clear away DirectByteBuffers used in TestOnlyByteArray
    // System.gc() alone won't work that well with DirectByteBuffers. Unsafe is needed to fully clear away buffers.
    Unsafe u;
    try{
      Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      u = (Unsafe) f.get(null);
    }catch(NoSuchFieldException | IllegalAccessException nsfe){
      throw new RuntimeException(nsfe);
    }

    // Do a recursive clean
    MultipleByteArrayTest.clean(u, byteArray);

    // Trigger a GC to blast away any unused stuff
    System.gc();

    // Log it if actually big
    if(size >= (Integer.MAX_VALUE * 0.5)) System.out.println("Cleared away " + size + "B");
  }

  class ReadOnlyTest
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
