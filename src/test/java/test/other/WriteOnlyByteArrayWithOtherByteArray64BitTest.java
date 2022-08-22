package test.other;

import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseOtherByteArrayTest;
import test.BaseWriteOnlyByteArrayTest;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public interface WriteOnlyByteArrayWithOtherByteArray64BitTest
  extends BaseWriteOnlyByteArrayTest, BaseOtherByteArrayTest{

  static <T extends ReadOnlyByteArray> void testWriteOn64BitData(
    @Nonnull Random rng,
    @Nonnull BiFunction<WriteOnlyByteArray,Long,Byte> testBAReaderFun,
    @Nonnull Function<Long,WriteOnlyByteArray> testWOBAAllocator,
    @Nonnull Consumer<WriteOnlyByteArray> testWOBACleanerConsumer,
    @Nonnull Function<Long,ReadableWritableByteArray> controlRWBAAllocator,
    @Nonnull Function<ReadableWritableByteArray,T> rwbaToROConverter,
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
      var testByteArray = testWOBAAllocator.apply(size);

      // Assert readonly if applicable
      if(!isReadableWritableOK) assertFalse(testByteArray instanceof ReadableWritableByteArray);

      try{

        // Check size
        assertEquals(size, testByteArray.size());

        // Set up constant seed to use for random data
        var seed = rng.nextInt();

        // Pick start
        long byteIndex = Integer.MAX_VALUE - 500 - rng.nextInt(500);
        long outSize = Long.min(testByteArray.size() - byteIndex, rng.nextInt(5_000) + 1_000);

        // Allocate source ba
        var sourceRW = controlRWBAAllocator.apply(outSize);
        var source = rwbaToROConverter.apply(sourceRW);

        try{

          // Check it
          assertEquals(outSize, source.size());

          // Write random data to source
          {
            for(long index = 0; index < source.size(); index++){
              var i64rng = new Random(seed + (byteIndex + index)); // index-specific RNG
              sourceRW.writeByte(index, (byte) i64rng.nextInt());
            }
          }

          // Write it
          testByteArray.write(byteIndex, source);

          // Check test BA
          for(long index = byteIndex - 200; index < testByteArray.size(); index++){
            byte expected = 0;
            if(index >= byteIndex && index < (byteIndex + outSize)){
              var i64rng = new Random(seed + index);
              expected = (byte) i64rng.nextInt();
            }
            assertEquals(expected, testBAReaderFun.apply(testByteArray, index), "Index: " + index);
          }

          // Check source BA for side effects
          for(long index = 0; index < outSize; index++){
            var i64rng = new Random(seed + (byteIndex + index));
            byte expected = (byte) i64rng.nextInt();
            assertEquals(expected, sourceRW.readByte(index), "Index: " + index);
          }

        }finally{
          controlRWBACleanerConsumer.accept(sourceRW);
        }
      }finally{
        testWOBACleanerConsumer.accept(testByteArray);
      }
    }catch(OutOfMemoryError oom){
      System.gc();
      fail("Not enough memory to continue this test");
    }
  }

  @Test
  @DisplayName("Test write(long, ReadOnlyByteArray) on 64-bit addressable data")
  default void testWriteWriteOnly64(){
    testWriteOn64BitData(
      getRNG(),
      this::readTestWriteOnlyByteArray,
      this::createTestWriteOnlyByteArray,
      this::cleanTestByteArray,
      this::createControlReadableWritable,
      ReadableWritableByteArray::toReadOnly,
      this::cleanControlByteArray,
      isReadableWritableOK()
    );
  }

  @Test
  @DisplayName("Test write(long, ReadableWritableByteArray) on 64-bit addressable data")
  default void testWriteReadableWritable64(){
    testWriteOn64BitData(
      getRNG(),
      this::readTestWriteOnlyByteArray,
      this::createTestWriteOnlyByteArray,
      this::cleanTestByteArray,
      this::createControlReadableWritable,
      ba -> ba,
      this::cleanControlByteArray,
      isReadableWritableOK()
    );
  }
}
