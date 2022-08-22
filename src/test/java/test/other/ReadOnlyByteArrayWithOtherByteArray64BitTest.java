package test.other;

import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseOtherByteArrayTest;
import test.BaseReadOnlyByteArrayTest;
import test.TriConsumer;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public interface ReadOnlyByteArrayWithOtherByteArray64BitTest extends BaseReadOnlyByteArrayTest, BaseOtherByteArrayTest{

  static <T extends WriteOnlyByteArray> void testReadOn64BitData(
    @Nonnull Random rng,
    @Nonnull TriConsumer<ReadOnlyByteArray,Long,Byte> testBAWriterFun,
    @Nonnull Function<Long,ReadOnlyByteArray> testROBAAllocator,
    @Nonnull Consumer<ReadOnlyByteArray> testROBACleanerConsumer,
    @Nonnull Function<Long,ReadableWritableByteArray> controlRWBAAllocator,
    @Nonnull Function<ReadableWritableByteArray,T> rwbaToWOConverter,
    @Nonnull Consumer<ReadableWritableByteArray> controlRWBACleanerConsumer,
    boolean isReadableWritableOK
  ){

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
      var testByteArray = testROBAAllocator.apply(size);

      // Assert readonly if applicable
      if(!isReadableWritableOK) assertFalse(testByteArray instanceof ReadableWritableByteArray);

      try{

        // Check size
        assertEquals(size, testByteArray.size());

        // Set up constant seed to use for random data
        var seed = rng.nextInt();

        // Pick start
        long byteIndex = Integer.MAX_VALUE - 500 - rng.nextInt(500);
        long maxSize = testByteArray.size() - byteIndex;
        long outSize = Long.min(testByteArray.size() - byteIndex, rng.nextInt(5_000) + 1_000);

        // Write random data to portion that covers beyond i32 zone
        long randStart = Integer.MAX_VALUE - 1_500 - rng.nextInt(1_500);
        {
          for(long index = 0; index < maxSize; index++){
            var ind = randStart + index;
            var i64rng = new Random(seed + ind); // index-specific RNG
            testBAWriterFun.accept(testByteArray, ind, (byte) i64rng.nextInt());
          }
        }

        // Allocate destination ba
        var destinationRW = controlRWBAAllocator.apply(outSize);
        var destination = rwbaToWOConverter.apply(destinationRW);

        try{

          // Check it
          assertEquals(outSize, destination.size());

          // Read it
          testByteArray.read(byteIndex, destination);

          // Check destination BA
          for(long index = 0; index < outSize; index++){
            var i64rng = new Random(seed + (byteIndex + index));
            byte expected = (byte) i64rng.nextInt();
            assertEquals(expected, destinationRW.readByte(index), "Index: " + index);
          }

          // Check test BA for any side effects
          for(long index = randStart - 200; index < testByteArray.size(); index++){
            byte expected = 0;
            if(index >= randStart && index < (randStart + maxSize)){
              var i64rng = new Random(seed + index);
              expected = (byte) i64rng.nextInt();
            }
            assertEquals(expected, testByteArray.readByte(index), "Index: " + index);
          }

        }finally{
          controlRWBACleanerConsumer.accept(destinationRW);
        }
      }finally{
        testROBACleanerConsumer.accept(testByteArray);
      }
    }catch(OutOfMemoryError oom){
      System.gc();
      fail("Not enough memory to continue this test");
    }
  }

  @Test
  @DisplayName("Test read(long, WriteOnlyByteArray) on 64-bit addressable data")
  default void testReadWriteOnly64(){
    testReadOn64BitData(
      getRNG(),
      this::writeTestReadOnlyByteArray,
      this::createTestReadOnlyByteArray,
      this::cleanTestByteArray,
      this::createControlReadableWritable,
      ReadableWritableByteArray::toWriteOnly,
      this::cleanControlByteArray,
      isReadableWritableOK()
    );
  }

  @Test
  @DisplayName("Test read(long, ReadableWritableByteArray) on 64-bit addressable data")
  default void testReadReadableWritable64(){
    testReadOn64BitData(
      getRNG(),
      this::writeTestReadOnlyByteArray,
      this::createTestReadOnlyByteArray,
      this::cleanTestByteArray,
      this::createControlReadableWritable,
      ba -> ba,
      this::cleanControlByteArray,
      isReadableWritableOK()
    );
  }
}
