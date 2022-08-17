package test;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ByteArrayLengthOverBoundsException;
import com.ansill.arrays.IndexingUtility;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static test.TestUtility.f;

public interface WriteOnlyByteArrayTest extends ByteArrayTest{

  @Nonnull
  static Iterable<DynamicTest> generateTestsInvalidWriteCallsByteArray(
    @Nonnull Random rng,
    @Nonnull String type,
    @Nonnull BiFunction<WriteOnlyByteArray,Long,Byte> testBAReaderFun,
    @Nonnull Function<Long,WriteOnlyByteArray> testWOBAAllocator,
    @Nonnull Consumer<WriteOnlyByteArray> testWOBACleanerConsumer,
    boolean isReadableWritableOk
  ){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Self sizes to test
    Set<Long> selfSizesToTest = new HashSet<>();
    selfSizesToTest.add(1L); // Test size of one
    selfSizesToTest.add((long) Short.MAX_VALUE); // Big enough
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(selfSizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long selfSize : selfSizesToTest){

      // Test-local RNG
      int testLocalRNG = rng.nextInt();

      // Test too-large bytearray
      tests.add(dynamicTest(
        f("test write(0, {}(size={})) on ByteArray of {}B size", type, selfSize + 1, selfSize),
        () -> {

          // Wrap in try to make sure memory gets cleaned up
          try{

            // Allocate test array
            WriteOnlyByteArray testArray = testWOBAAllocator.apply(selfSize);

            // Assert readonly if applicable
            assertEquals(isReadableWritableOk, testArray instanceof ReadableWritableByteArray);

            // Try and finally to clean up test array
            try{

              // Build control array
              ReadableWritableByteArray controlArray = new TestOnlyByteArray(selfSize + 1);
              ReadOnlyByteArray control = type.contains("ReadableWritable") ? controlArray : controlArray.toReadOnly();

              // Fill the control array
              {
                Random random = new Random(testLocalRNG);
                for(long i = 0; i < controlArray.size(); i++){
                  controlArray.writeByte(i, (byte) random.nextInt());
                }
              }

              // Build the expected exception
              ByteArrayLengthOverBoundsException expected = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> IndexingUtility.checkWrite(0, control, selfSize)
              );

              // Test it
              ByteArrayLengthOverBoundsException actual = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> testArray.write(0, control)
              );

              // Compare messages
              assertEquals(expected.getMessage(), actual.getMessage());

              // Check testArray for any side effects
              for(long i = 0; i < testArray.size(); i++){
                assertEquals((byte) 0, testBAReaderFun.apply(testArray, i));
              }

              // Check control for any side effects
              {
                Random random = new Random(testLocalRNG);
                for(long i = 0; i < controlArray.size(); i++){
                  assertEquals((byte) random.nextInt(), controlArray.readByte(i));
                }
              }

            }finally{
              testWOBACleanerConsumer.accept(testArray);
            }
          }catch(OutOfMemoryError oom){
            System.gc();
            oom.printStackTrace();
            fail("Cannot perform test due to insufficient memory space");
          }

          // Clean up
          System.gc();
        }
      ));

      // Test bytearray at negative offset
      for(int trial = 0; trial < TRIALS; trial++){
        int byteIndex = -Integer.max(rng.nextInt((int) selfSize), 1);
        int len = Integer.max(1, rng.nextInt((int) selfSize));
        tests.add(dynamicTest(f(
          "test read({}, {}(size={})) on ByteArray of {}B size",
          byteIndex,
          type,
          len,
          selfSize
        ), () -> {

          // Wrap in try to make sure memory gets cleaned up
          try{

            // Allocate test array
            WriteOnlyByteArray testArray = testWOBAAllocator.apply(selfSize);

            // Assert readonly if applicable
            assertEquals(isReadableWritableOk, testArray instanceof ReadableWritableByteArray);

            // Try and finally to clean up test array
            try{

              // Build control array
              ReadableWritableByteArray controlArray = new TestOnlyByteArray(selfSize + 1);
              ReadOnlyByteArray control = type.contains("ReadableWritable") ? controlArray : controlArray.toReadOnly();

              // Fill the control array
              {
                Random random = new Random(testLocalRNG);
                for(long i = 0; i < controlArray.size(); i++){
                  controlArray.writeByte(i, (byte) random.nextInt());
                }
              }

              // Build the expected exception
              ByteArrayIndexOutOfBoundsException expected = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkWrite(byteIndex, control, selfSize)
              );

              // Test it
              ByteArrayIndexOutOfBoundsException actual = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testArray.write(byteIndex, control)
              );

              // Compare messages
              assertEquals(expected.getMessage(), actual.getMessage());

              // Check testArray for any side effects
              for(long i = 0; i < testArray.size(); i++){
                assertEquals((byte) 0, testBAReaderFun.apply(testArray, i));
              }

              // Check control for any side effects
              {
                Random random = new Random(testLocalRNG);
                for(long i = 0; i < controlArray.size(); i++){
                  assertEquals((byte) random.nextInt(), controlArray.readByte(i));
                }
              }

            }finally{
              testWOBACleanerConsumer.accept(testArray);
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

      // Test bytearray at byte index that is over the bytearray's size
      for(int trial = 0; trial < TRIALS; trial++){
        int byteIndex = (int) (selfSize + rng.nextInt((int) selfSize) + 1);
        int len = Integer.max(1, rng.nextInt((int) selfSize));
        tests.add(dynamicTest(f(
          "test read({}, {}(size={})) on ByteArray of {}B size",
          byteIndex,
          type,
          len,
          selfSize
        ), () -> {

          // Wrap in try to make sure memory gets cleaned up
          try{

            // Allocate test array
            WriteOnlyByteArray testArray = testWOBAAllocator.apply(selfSize);

            // Assert readonly if applicable
            assertEquals(isReadableWritableOk, testArray instanceof ReadableWritableByteArray);

            // Try and finally to clean up test array
            try{

              // Build control array
              ReadableWritableByteArray controlArray = new TestOnlyByteArray(selfSize + 1);
              ReadOnlyByteArray control = type.contains("ReadableWritable") ? controlArray : controlArray.toReadOnly();

              // Fill the control array
              {
                Random random = new Random(testLocalRNG);
                for(long i = 0; i < controlArray.size(); i++){
                  controlArray.writeByte(i, (byte) random.nextInt());
                }
              }

              // Build the expected exception
              ByteArrayIndexOutOfBoundsException expected = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkWrite(byteIndex, control, selfSize)
              );

              // Test it
              ByteArrayIndexOutOfBoundsException actual = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testArray.write(byteIndex, control)
              );

              // Compare messages
              assertEquals(expected.getMessage(), actual.getMessage());

              // Check testArray for any side effects
              for(long i = 0; i < testArray.size(); i++){
                assertEquals((byte) 0, testBAReaderFun.apply(testArray, i));
              }

              // Check control for any side effects
              {
                Random random = new Random(testLocalRNG);
                for(long i = 0; i < controlArray.size(); i++){
                  assertEquals((byte) random.nextInt(), controlArray.readByte(i));
                }
              }

            }finally{
              testWOBACleanerConsumer.accept(testArray);
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

  @Nonnull
  WriteOnlyByteArray createTestWriteOnlyByteArray(@Nonnegative long size);

  byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, @Nonnegative long byteIndex);

  @DisplayName("Test toString()")
  @Test
  default void testToString(){

    // Simple toString test
    WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(1);

    // ToString it
    assertNotNull(testByteArray.toString());

  }

  @DisplayName("Test invalid write(long, ReadOnlyByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteCallsReadOnlyByteArray(){
    return generateTestsInvalidWriteCallsByteArray(
      this.getRNG(),
      "WriteOnlyByteArray",
      this::readTestWriteOnlyByteArray,
      this::createTestWriteOnlyByteArray,
      this::cleanTestByteArray,
      this.isReadableWritableOK()
    );
  }

  @DisplayName("Test invalid write(long, ReadableWritableByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteCallsReadableWritableByteArray(){
    return generateTestsInvalidWriteCallsByteArray(
      this.getRNG(),
      "ReadableWritableByteArray",
      this::readTestWriteOnlyByteArray,
      this::createTestWriteOnlyByteArray,
      this::cleanTestByteArray,
      this.isReadableWritableOK()
    );
  }

  @Nonnull
  @Override
  default ByteArray createTestByteArray(long size){
    return createTestWriteOnlyByteArray(size);
  }

  @DisplayName("Test valid writeByte(long, byte) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteByteCalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(1L); // Test size of one
    //sizesToTest.add((long) Short.MAX_VALUE); // Big enough
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full writeByte(long, byte) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          assertEquals(isReadableWritableOK(), testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            ReadableWritableByteArray controlOne = new TestOnlyByteArray(size);
            {
              Random testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                controlOne.writeByte(index, (byte) testRNG.nextInt());
              }
            }

            // Randomize the write order
            List<Integer> indices = new LinkedList<>();
            {
              Random testRNG = new Random(testLocalSeed);
              IntStream.range(0, Math.toIntExact(size)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Write it
                testByteArray.writeByte(byteIndex, controlOne.readByte(byteIndex));

                // Add to written set
                written.add(byteIndex);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestWriteOnlyByteArray(testByteArray, i);
                  if(written.contains(i)) assertEquals(controlOne.readByte(i), testVal);
                  else assertEquals(0, testVal);
                }
              }
            }

            // Come up with new control byte array (to check for overwrite correctness)
            ReadableWritableByteArray controlTwo = new TestOnlyByteArray(size);
            {
              Random testRNG = new Random(testLocalSeed + 342);
              for(long index = 0; index < size; index++){
                controlTwo.writeByte(index, (byte) testRNG.nextInt());
              }
            }

            // Randomize the write order
            {
              indices = new LinkedList<>();
              Random testRNG = new Random(testLocalSeed + 232);
              IntStream.range(0, Math.toIntExact(size)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Write it
                testByteArray.writeByte(byteIndex, controlTwo.readByte(byteIndex));

                // Add to written set
                written.add(byteIndex);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestWriteOnlyByteArray(testByteArray, i);
                  if(written.contains(i)) assertEquals(controlTwo.readByte(i), testVal);
                  else assertEquals(controlOne.readByte(i), testVal);
                }
              }
            }

          }finally{
            cleanTestByteArray(testByteArray);
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad writeByte(long, byte) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteByteCalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeByte(-1,byte) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          assertEquals(isReadableWritableOK(), testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWriteByte(-1, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeByte(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
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

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeByte({},byte) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            assertEquals(isReadableWritableOK(), testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWriteByte(index, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeByte(index, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
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

      // Write test for index that exceeds capacity
      tests.add(dynamicTest(f("writeByte({},byte) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          assertEquals(isReadableWritableOK(), testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWriteByte(size, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeByte(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
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

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeByte({},byte) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            assertEquals(isReadableWritableOK(), testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWriteByte(index, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeByte(index, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
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

  // TODO add test to test valid subsetOf calls
}
