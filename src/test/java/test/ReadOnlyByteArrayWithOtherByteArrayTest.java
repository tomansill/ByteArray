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

import static com.ansill.arrays.TestUtility.f;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public interface ReadOnlyByteArrayWithOtherByteArrayTest extends ReadOnlyByteArrayTest, OtherByteArrayTest{

  @Nonnull
  static <T extends WriteOnlyByteArray> Iterable<DynamicTest> generateValidSubsetOfByteArrayTests(
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

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(1L); // Test size of one
    sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Self-test
      tests.add(dynamicTest(f(
        "subset({}, {}) on ByteArray of {}B size - then checked with read(long,{}) calls",
        0,
        size,
        size,
        type
      ), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = testROBAAllocator.apply(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              Random testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                testBAWriterFun.accept(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Get subset
            var subset = testByteArray.subsetOf(0, size);

            // Assert readonly if applicable
            if(!isReadableWritableOK) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            // Assert size
            assertEquals(size, subset.size());

            // Should be same object
            assertSame(testByteArray, subset);

            // Check using read call
            {
              var control = controlRWBAAllocator.apply(size);
              try{
                var adj = rwbaToWOConverter.apply(control);
                subset.read(0, adj);
                Random testRNG = new Random(testLocalSeed);
                long innerByteIndex = 0;
                for(long index = 0; index < size; index++){
                  byte expected = (byte) testRNG.nextInt();
                  assertEquals(expected, control.readByte(innerByteIndex), "Index: " + innerByteIndex);
                  innerByteIndex++;
                }
              }finally{
                controlRWBACleanerConsumer.accept(control);
              }
            }

          }finally{
            testROBACleanerConsumer.accept(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          oom.printStackTrace();
          System.out.println("Out of memory. Cannot perform this test due to insufficient memory space");
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Repeat trials
      for(int trial = 0; trial < TRIALS; trial++){

        // Choose byteIndex
        long byteIndex = size == 1 ? 0 : Long.max(0, rng.nextInt((int) size) - 1);

        // Choose size
        long subSize = size == 1 ? 1 : Long.min(size - byteIndex, rng.nextInt((int) size) + 1);

        // Skip test if size is same
        if(subSize == size) continue;

        // Write test
        tests.add(dynamicTest(f(
          "subset({}, {}) on ByteArray of {}B size - then checked with read(long,{}) calls",
          byteIndex,
          subSize,
          size,
          type
        ), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = testROBAAllocator.apply(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Write random bytes to test bytearray
              {
                Random testRNG = new Random(testLocalSeed);
                for(long index = 0; index < size; index++){
                  testBAWriterFun.accept(testByteArray, index, (byte) testRNG.nextInt());
                }
              }

              // Get subset
              ReadOnlyByteArray subset = testByteArray.subsetOf(byteIndex, subSize);

              // Assert readonly if applicable
              if(!isReadableWritableOK) assertFalse(testByteArray instanceof ReadableWritableByteArray);

              // Assert size
              assertEquals(subSize, subset.size());

              // Check using read call
              {
                var control = controlRWBAAllocator.apply(subSize);
                try{
                  var adj = rwbaToWOConverter.apply(control);
                  subset.read(0, adj);
                  Random testRNG = new Random(testLocalSeed);
                  long innerByteIndex = 0;
                  for(long index = 0; index < size; index++){
                    byte expected = (byte) testRNG.nextInt();
                    if(index < byteIndex || index >= byteIndex + subSize) continue;
                    assertEquals(expected, control.readByte(innerByteIndex), "Index: " + innerByteIndex);
                    innerByteIndex++;
                  }
                }finally{
                  controlRWBACleanerConsumer.accept(control);
                }
              }

              // Write different random bytes to test bytearray (to test if changes to original byte array propagates to subset)
              int diffTestLocalSeed = testLocalSeed + 233432;
              {
                Random testRNG = new Random(diffTestLocalSeed);
                for(long index = 0; index < size; index++){
                  testBAWriterFun.accept(testByteArray, index, (byte) testRNG.nextInt());
                }
              }

              // Check using read call
              {
                ReadableWritableByteArray control = controlRWBAAllocator.apply(subSize);
                try{
                  var adj = rwbaToWOConverter.apply(control);
                  subset.read(0, adj);
                  Random testRNG = new Random(diffTestLocalSeed);
                  long innerByteIndex = 0;
                  for(long index = 0; index < size; index++){
                    byte expected = (byte) testRNG.nextInt();
                    if(index < byteIndex || index >= byteIndex + subSize) continue;
                    assertEquals(expected, control.readByte(innerByteIndex), "Index: " + innerByteIndex);
                    innerByteIndex++;
                  }
                }finally{
                  controlRWBACleanerConsumer.accept(control);
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

          // Clean up
          System.gc();
        }));
      }
    }

    // Return tests
    return tests;
  }

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
                if(!isReadableWritableOK) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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

  @DisplayName("Test valid subsetOf(long, long) with read(long,WriteOnlyByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidSubsetOfCallsWriteOnlyByteArray(){
    return generateValidSubsetOfByteArrayTests(
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

  @DisplayName("Test valid subsetOf(long, long) with read(long,ReadableWritableByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidSubsetOfCallsReadableWritableByteArray(){
    return generateValidSubsetOfByteArrayTests(
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
