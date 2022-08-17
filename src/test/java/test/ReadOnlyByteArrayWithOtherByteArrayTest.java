package test;

import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static test.TestUtility.f;

public interface ReadOnlyByteArrayWithOtherByteArrayTest extends ReadOnlyByteArrayTest, OtherByteArrayTest{

  static <T extends WriteOnlyByteArray> Iterable<DynamicTest> generateReadByteArrayTests(
    @Nonnull Random rng,
    @Nonnull String type,
    @Nonnull TriConsumer<ReadOnlyByteArray,Long,Byte> testBAWriterFun,
    @Nonnull Function<Long,ReadOnlyByteArray> testROBAAllocator,
    @Nonnull Consumer<ReadOnlyByteArray> testROBACleanerConsumer,
    @Nonnull Function<Long,ReadableWritableByteArray> controlRWBAAllocator,
    @Nonnull Function<ReadableWritableByteArray,T> rwbaToWOConverter,
    @Nonnull Consumer<ReadableWritableByteArray> controlRWBACleanerConsumer,
    boolean isReadableWritableOK
  ){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Self sizes to test
    Set<Long> selfSizesToTest = new HashSet<>();
    selfSizesToTest.add(1L); // Test size of one
    //selfSizesToTest.add((long) Short.MAX_VALUE); // Big enough
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(selfSizesToTest.add((long) rng.nextInt(955) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long selfSize : selfSizesToTest){

      // Build set of destination bytearray sizes to try
      Set<Long> destSizesToTest = new HashSet<>();
      // Smallest possible - only if source is small enough, don't do it if it's too big
      if(selfSize < 100) destSizesToTest.add(1L);
      destSizesToTest.add(selfSize); // Maximum

      // Set up RNG for float, we may need to make RNG more biased for very large destination size (to reduce the
      // number of tests because a small destination bytearray on a very large bytearray can generate a lot of test and
      // takes a while to complete)
      Supplier<Float> nextFloat = rng::nextFloat;
      if(selfSize >= 100) nextFloat = () -> (rng.nextFloat() / 2) + 0.5f; // Biased rng, 0.5 - 1.0
      if(selfSize >= 250) nextFloat = () -> (rng.nextFloat() / 4) + 0.75f; // Biased rng, 0.75 - 1.0
      if(selfSize >= 500) nextFloat = () -> (rng.nextFloat() / 10) + 0.90f; // Biased rng, 0.9 - 1.0
      if(selfSize >= 750) nextFloat = () -> (rng.nextFloat() / 20) + 0.95f; // Biased rng, 0.95 - 1.0

      // Come up random destination sizes to try
      int lastSize = destSizesToTest.size();
      while(destSizesToTest.size() < selfSize && destSizesToTest.size() < (TRIALS + lastSize)){

        // Pick a random destination size
        long destSize = (long) (selfSize * nextFloat.get());

        // Make sure not zero or max size
        if(destSize == 0 || destSize == selfSize) continue;

        // Add
        destSizesToTest.add(destSize);

      }

      // Loop on dest sizes
      for(Long destSize : destSizesToTest){

        // Do all possible valid byteIndex
        for(int tempByteIndex = 0; tempByteIndex <= selfSize - destSize; tempByteIndex++){

          // Get test-local RNG seed
          int testLocalSeed = (int) (rng.nextInt() ^ selfSize ^ destSize);

          // Write test
          final int byteIndex = tempByteIndex;
          tests.add(dynamicTest(
            f("full read({}, {}(size={})) on ByteArray of {}B size", byteIndex, type, destSize, selfSize),
            () -> {

              // Wrap in try and catch for possible OOM if trying to allocate max memory
              try{

                // Allocate the readonly bytearray
                ReadOnlyByteArray testByteArray = testROBAAllocator.apply(selfSize);

                // Assert readonly if applicable
                assertEquals(isReadableWritableOK, testByteArray instanceof ReadableWritableByteArray);

                try{

                  // Assert size
                  assertEquals(selfSize, testByteArray.size());

                  // Write random bytes to test bytearray
                  {
                    Random testRNG = new Random(testLocalSeed);
                    for(long index = 0; index < selfSize; index++){
                      testBAWriterFun.accept(testByteArray, index, (byte) testRNG.nextInt());
                    }
                  }

                  // Allocate destination bytearray
                  ReadableWritableByteArray destinationRWBA = controlRWBAAllocator.apply(destSize);
                  try{
                    assertEquals(destSize, destinationRWBA.size());
                    T destination = rwbaToWOConverter.apply(destinationRWBA);

                    // Call it
                    testByteArray.read(byteIndex, destination);

                    // Check the destination bytearray
                    {
                      Random testRNG = new Random(testLocalSeed);
                      for(long index = 0; index < selfSize; index++){
                        if(index < byteIndex) testRNG.nextInt(); // Roll up the RNG
                        else if(index >= byteIndex + destSize) break;
                        else{
                          assertEquals(
                            (byte) testRNG.nextInt(),
                            destinationRWBA.readByte(index - byteIndex),
                            "Index: " + index
                          );
                        }
                      }
                    }
                  }finally{
                    controlRWBACleanerConsumer.accept(destinationRWBA);
                  }

                  // Now check the test bytearray to make sure nothing is mutated (check for side effects)
                  {
                    Random testRNG = new Random(testLocalSeed);
                    assertEquals(selfSize, testByteArray.size());
                    for(long index = 0; index < selfSize; index++){
                      assertEquals((byte) testRNG.nextInt(), testByteArray.readByte(index), "Index: " + index);
                    }
                  }
                }finally{
                  testROBACleanerConsumer.accept(testByteArray);
                }

              }catch(OutOfMemoryError oom){
                System.gc();
                oom.printStackTrace();
                fail("Cannot perform test due to insufficient memory space");
              }
            }
          ));
        }
      }
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid read(long, WriteOnlyByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadCallsWriteOnlyByteArray(){
    return generateReadByteArrayTests(
      getRNG(),
      "WriteOnlyByteArray",
      this::writeTestReadOnlyByteArray,
      this::createTestReadOnlyByteArray,
      this::cleanTestByteArray,
      this::createControlReadableWritable,
      ReadableWritableByteArray::toWriteOnly,
      this::cleanControlByteArray,
      this.isReadableWritableOK()
    );
  }

  @DisplayName("Test valid read(long, ReadableWritableByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadCallsReadableWritableByteArray(){
    return generateReadByteArrayTests(
      getRNG(),
      "ReadableWritableByteArray",
      this::writeTestReadOnlyByteArray,
      this::createTestReadOnlyByteArray,
      this::cleanTestByteArray,
      this::createControlReadableWritable,
      rwba -> rwba,
      this::cleanControlByteArray,
      this.isReadableWritableOK()
    );
  }

}
