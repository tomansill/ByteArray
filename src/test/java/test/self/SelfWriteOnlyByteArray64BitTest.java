package test.self;

import com.ansill.arrays.ReadableWritableByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseWriteOnlyByteArrayTest;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public interface SelfWriteOnlyByteArray64BitTest extends BaseWriteOnlyByteArrayTest{


  @DisplayName("Test readWrite(long) on 64-bit addressable data")
  @Test
  default void testWriteByteOn64BitData(){

    // Get RNG
    Random rng = getRNG();

    // Set up size that is beyond i32
    long size = ((long) Integer.MAX_VALUE) + rng.nextInt(9_000) + 1_000;

    // Print size
    System.out.println("Size: " +
                       size +
                       "B - " +
                       String.format("%.2f", size / 1000.0) +
                       "KB - " +
                       String.format("%.2f", size / 1_000_000.0) +
                       "MB - " +
                       String.format("%.2f", size / 1_000_000_000.0) +
                       "GB");

    try{

      // Allocate it
      var testByteArray = createTestWriteOnlyByteArray(size);

      // Assert readonly if applicable
      if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

      try{

        // Check size
        assertEquals(size, testByteArray.size());

        // Repeat many times to sparsely read across huge i64 address space (randomly choose positions)
        int repetitions = 1_000_000;
        Set<Long> visited = new HashSet<>(repetitions);
        boolean visitedBeyondI32 = false;
        for(int repetition = 0; repetition < repetitions; repetition++){

          // Choose an byteIndex
          long byteIndex;

          // Make sure we do visit beyond i32 land
          if((repetition <= repetitions / 2) || visitedBeyondI32) byteIndex = (long) (size * rng.nextFloat());
          else{
            long range = size - Integer.MAX_VALUE - 20;
            byteIndex = ((long) (range * rng.nextDouble())) + Integer.MAX_VALUE + 20;
          }

          // Check if beyond i32
          if(byteIndex > Integer.MAX_VALUE) visitedBeyondI32 = true;

          // Add to visited - loop if already in the visited
          if(!visited.add(byteIndex)){
            repetition--;
            continue;
          }

          // Get random byte
          byte value = (byte) rng.nextInt();

          // Write
          testByteArray.writeByte(byteIndex, value);

          // Check it
          assertEquals(
            value,
            assertDoesNotThrow(() -> readTestWriteOnlyByteArray(testByteArray, byteIndex)),
            "Index: " + byteIndex
          );

        }
      }finally{
        cleanTestByteArray(testByteArray);
      }
    }catch(OutOfMemoryError oom){
      System.gc();
      fail("Not enough memory to continue this test");
    }
  }

}
