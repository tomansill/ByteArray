package com.ansill.arrays;

import org.junit.jupiter.api.DisplayName;
import test.ByteArrayTest;
import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@DisplayName("MultipleByteArray tests")
public interface MultipleByteArrayTest extends ByteArrayTest{

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

  @Nonnull
  static ReadableWritableByteArray createReadableWritableByteArray(long size, int seed){

    // Seed RNG
    Random random = new Random(seed);

    // We want around 5 chunks if possible
    int chunkSize = (int) (size / 5);
    if(chunkSize < 2) chunkSize = (int) size;

    // Build the list
    List<ReadableWritableByteArray> bytearrays = new ArrayList<>();
    long runningSize = size;
    while(runningSize > 0){

      // Set up size
      long innerSize = Long.min(random.nextInt(chunkSize) + 1, runningSize);

      // 25% chance that it'll be inner multiplebytearray
      if(random.nextFloat() <= 0.25){

        // Add to the list
        bytearrays.add(createReadableWritableByteArray(innerSize, random.nextInt()));

      }else{

        // Add to the list
        bytearrays.add(new TestOnlyByteArray(innerSize));

      }

      // Update running size
      runningSize -= innerSize;
    }

    return new ReadableWritableMultipleByteArray(bytearrays);
  }

  @Nonnull
  default ReadableWritableByteArray createTestReadableWritableByteArray(long size, int seed){
    return createReadableWritableByteArray(size, seed);
  }

}
