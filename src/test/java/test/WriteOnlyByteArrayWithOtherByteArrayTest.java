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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.ansill.arrays.TestUtility.f;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public interface WriteOnlyByteArrayWithOtherByteArrayTest extends WriteOnlyByteArrayTest, OtherByteArrayTest{

  static <T extends ReadOnlyByteArray> Iterable<DynamicTest> generateWriteByteArrayTests(
    @Nonnull Random rng,
    @Nonnull String type,
    @Nonnull BiFunction<WriteOnlyByteArray,Long,Byte> testBAReaderFun,
    @Nonnull Function<Long,WriteOnlyByteArray> testWOBAAllocator,
    @Nonnull Consumer<WriteOnlyByteArray> testWOBACleanerConsumer,
    @Nonnull Function<Long,ReadableWritableByteArray> controlRWBAAllocator,
    @Nonnull Function<ReadableWritableByteArray,T> rwbaToROConverter,
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

      // Build set of source bytearray sizes to try
      Set<Long> sourceSizesToTest = new HashSet<>();
      // Smallest possible - only if source is small enough, don't do it if it's too big
      if(selfSize < 100) sourceSizesToTest.add(1L);
      sourceSizesToTest.add(selfSize); // Maximum

      // Set up RNG for float, we may need to make RNG more biased for very large source size (to reduce the
      // number of tests because a small source bytearray on a very large bytearray can generate a lot of test and
      // takes a while to complete)
      Supplier<Float> nextFloat = rng::nextFloat;
      if(selfSize >= 100) nextFloat = () -> (rng.nextFloat() / 2) + 0.5f; // Biased rng, 0.5 - 1.0
      if(selfSize >= 250) nextFloat = () -> (rng.nextFloat() / 4) + 0.75f; // Biased rng, 0.75 - 1.0
      if(selfSize >= 500) nextFloat = () -> (rng.nextFloat() / 10) + 0.90f; // Biased rng, 0.9 - 1.0
      if(selfSize >= 750) nextFloat = () -> (rng.nextFloat() / 20) + 0.95f; // Biased rng, 0.95 - 1.0

      // Come up random source sizes to try
      int lastSize = sourceSizesToTest.size();
      while(sourceSizesToTest.size() < selfSize && sourceSizesToTest.size() < (TRIALS + lastSize)){

        // Pick a random source size
        long sourceSize = (long) (selfSize * nextFloat.get());

        // Make sure not zero or max size
        if(sourceSize == 0 || sourceSize == selfSize) continue;

        // Add
        sourceSizesToTest.add(sourceSize);

      }

      // Loop on source sizes
      for(long sourceSize : sourceSizesToTest){

        // Do all possible valid byteIndex
        for(int tempByteIndex = 0; tempByteIndex <= selfSize - sourceSize; tempByteIndex++){

          // Get test-local RNG seed
          int testLocalSeed = (int) (rng.nextInt() ^ selfSize ^ sourceSize);

          // Write test
          final int byteIndex = tempByteIndex;
          tests.add(dynamicTest(
            f("full write({}, {}(size={})) on ByteArray of {}B size", byteIndex, type, sourceSize, selfSize),
            () -> {

              // Wrap in try and catch for possible OOM if trying to allocate max memory
              try{

                // Allocate the writeonly bytearray
                WriteOnlyByteArray testByteArray = testWOBAAllocator.apply(selfSize);

                try{

                  // Assert writeonly if applicable
                  assertEquals(isReadableWritableOK, testByteArray instanceof ReadableWritableByteArray);

                  // Randomize test bytearray
                  {
                    Random random = new Random(testLocalSeed + 32342);
                    for(long index = 0; index < testByteArray.size(); index++){
                      testByteArray.writeByte(index, (byte) random.nextInt());
                    }
                  }

                  // Assert size
                  assertEquals(selfSize, testByteArray.size());

                  // Allocate source bytearray
                  ReadableWritableByteArray sourceRWBA = controlRWBAAllocator.apply(sourceSize);
                  try{

                    // Write random bytes to source bytearray
                    assertEquals(sourceSize, sourceRWBA.size());
                    {
                      Random testRNG = new Random(testLocalSeed);
                      for(long index = 0; index < sourceSize; index++){
                        sourceRWBA.writeByte(index, (byte) testRNG.nextInt());
                      }
                    }

                    // Convert to readonly if applicable
                    T source = rwbaToROConverter.apply(sourceRWBA);

                    // Call it
                    testByteArray.write(byteIndex, source);

                    // Check the source bytearray to make sure nothing is mutated (check for side effects)
                    {
                      Random testRNG = new Random(testLocalSeed);
                      assertEquals(selfSize, testByteArray.size());
                      for(long index = 0; index < sourceSize; index++){
                        byte expectedValue = (byte) testRNG.nextInt();
                        assertEquals(expectedValue, sourceRWBA.readByte(index), "Index: " + index);
                        assertEquals(expectedValue, source.readByte(index), "Index: " + index);
                      }
                    }

                    // Check the test bytearray
                    {
                      Random random = new Random(testLocalSeed + 32342);
                      Random testRNG = new Random(testLocalSeed);
                      for(long index = 0; index < selfSize; index++){
                        byte exp = (byte) random.nextInt();
                        if(index < byteIndex || index >= byteIndex + sourceSize) assertEquals(
                          exp,
                          testBAReaderFun.apply(testByteArray, index)
                        );
                        else{
                          assertEquals(
                            (byte) testRNG.nextInt(),
                            testBAReaderFun.apply(testByteArray, index),
                            "Index: " + index
                          );
                        }
                      }
                    }
                  }finally{
                    controlRWBACleanerConsumer.accept(sourceRWBA);
                  }

                }finally{
                  testWOBACleanerConsumer.accept(testByteArray);
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

  @DisplayName("Test valid write(long, ReadOnlyByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteCallsReadOnlyByteArray(){
    return generateWriteByteArrayTests(
      getRNG(),
      "ReadOnlyByteArray",
      this::readTestWriteOnlyByteArray,
      this::createTestWriteOnlyByteArray,
      this::cleanTestByteArray,
      this::createControlReadableWritable,
      ReadableWritableByteArray::toReadOnly,
      this::cleanControlByteArray,
      this.isReadableWritableOK()
    );
  }

  @DisplayName("Test valid write(long, ReadableWritableByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteCallsReadableWritableByteArray(){
    return generateWriteByteArrayTests(
      getRNG(),
      "ReadableWritableByteArray",
      this::readTestWriteOnlyByteArray,
      this::createTestWriteOnlyByteArray,
      this::cleanTestByteArray,
      this::createControlReadableWritable,
      rwba -> rwba,
      this::cleanControlByteArray,
      this.isReadableWritableOK()
    );
  }

}
