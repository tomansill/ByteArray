package test.arrays;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.TestUtility;
import test.ByteArrayTest;

import javax.annotation.Nonnull;

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
