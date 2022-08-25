package test.self;

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
import test.BaseReadOnlyByteArrayTest;
import test.TriConsumer;
import test.arrays.DoNotTouchMeByteArray;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.ansill.arrays.TestUtility.f;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public interface SelfReadOnlyByteArrayTest extends BaseReadOnlyByteArrayTest, SelfByteArrayTest{

  @Nonnull
  static Iterable<DynamicTest> generateTestsInvalidReadCallsByteArray(
    @Nonnull Random rng,
    @Nonnull String type,
    @Nonnull TriConsumer<ReadOnlyByteArray,Long,Byte> testBAWriterFun,
    @Nonnull Function<Long,ReadOnlyByteArray> testROBAAllocator,
    @Nonnull Consumer<ReadOnlyByteArray> testROBACleanerConsumer,
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
        f("test read(0, {}(size={})) on ByteArray of {}B size", type, selfSize + 1, selfSize),
        () -> {

          // Wrap in try to make sure memory gets cleaned up
          try{

            // Allocate test array
            ReadOnlyByteArray testArray = testROBAAllocator.apply(selfSize);

            // Assert readonly if applicable
            if(!isReadableWritableOk) assertFalse(testArray instanceof ReadableWritableByteArray);

            // Try and finally to clean up test array
            try{

              // Fill contents of testArray
              {
                Random testRng = new Random(testLocalRNG);
                for(long i = 0; i < selfSize; i++){
                  testBAWriterFun.accept(testArray, i, (byte) testRng.nextInt());
                }
              }

              // Build control array
              ReadableWritableByteArray controlArray = new DoNotTouchMeByteArray(selfSize + 1);
              WriteOnlyByteArray control = type.contains("ReadableWritable") ? controlArray : controlArray.toWriteOnly();

              // Build the expected exception
              ByteArrayLengthOverBoundsException expected = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> IndexingUtility.checkRead(0, control, selfSize)
              );

              // Test it
              ByteArrayLengthOverBoundsException actual = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> testArray.read(0, control)
              );

              // Compare messages
              assertEquals(expected.getMessage(), actual.getMessage());

              // Check testArray for any side effects
              {
                Random testRng = new Random(testLocalRNG);
                for(long i = 0; i < selfSize; i++){
                  assertEquals((byte) testRng.nextInt(), testArray.readByte(i));
                }
              }

            }finally{
              testROBACleanerConsumer.accept(testArray);
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
            ReadOnlyByteArray testArray = testROBAAllocator.apply(selfSize);

            // Assert readonly if applicable
            if(!isReadableWritableOk) assertFalse(testArray instanceof ReadableWritableByteArray);

            // Try and finally to clean up test array
            try{

              // Fill contents of testArray
              {
                Random testRng = new Random(testLocalRNG);
                for(long i = 0; i < selfSize; i++){
                  testBAWriterFun.accept(testArray, i, (byte) testRng.nextInt());
                }
              }

              // Build control array
              ReadableWritableByteArray controlArray = new DoNotTouchMeByteArray(len);
              WriteOnlyByteArray control = type.contains("ReadableWritable") ? controlArray : controlArray.toWriteOnly();

              // Build the expected exception
              ByteArrayIndexOutOfBoundsException expected = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkRead(byteIndex, control, selfSize)
              );

              // Test it
              ByteArrayIndexOutOfBoundsException actual = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testArray.read(byteIndex, control)
              );

              // Compare messages
              assertEquals(expected.getMessage(), actual.getMessage());

              // Check testArray for any side effects
              {
                Random testRng = new Random(testLocalRNG);
                for(long i = 0; i < selfSize; i++){
                  assertEquals((byte) testRng.nextInt(), testArray.readByte(i));
                }
              }

            }finally{
              testROBACleanerConsumer.accept(testArray);
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
            ReadOnlyByteArray testArray = testROBAAllocator.apply(selfSize);

            // Assert readonly if applicable
            if(!isReadableWritableOk) assertFalse(testArray instanceof ReadableWritableByteArray);

            // Try and finally to clean up test array
            try{

              // Fill contents of testArray
              {
                Random testRng = new Random(testLocalRNG);
                for(long i = 0; i < selfSize; i++){
                  testBAWriterFun.accept(testArray, i, (byte) testRng.nextInt());
                }
              }

              // Build control array
              ReadableWritableByteArray controlArray = new DoNotTouchMeByteArray(len);
              WriteOnlyByteArray control = type.contains("ReadableWritable") ? controlArray : controlArray.toWriteOnly();

              // Build the expected exception
              ByteArrayIndexOutOfBoundsException expected = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkRead(byteIndex, control, selfSize)
              );

              // Test it
              ByteArrayIndexOutOfBoundsException actual = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testArray.read(byteIndex, control)
              );

              // Compare messages
              assertEquals(expected.getMessage(), actual.getMessage());

              // Check testArray for any side effects
              {
                Random testRng = new Random(testLocalRNG);
                for(long i = 0; i < selfSize; i++){
                  assertEquals((byte) testRng.nextInt(), testArray.readByte(i));
                }
              }

            }finally{
              testROBACleanerConsumer.accept(testArray);
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

  @DisplayName("Test toString()")
  @Test
  default void testToString(){

    // Simple toString test
    ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(1);

    // Assert readonly if applicable
    if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

    // ToString it
    assertNotNull(testByteArray.toString());

  }

  @DisplayName("Test invalid read(long, WriteOnlyByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadCallsWriteOnlyByteArray(){
    return generateTestsInvalidReadCallsByteArray(
      this.getRNG(),
      "WriteOnlyByteArray",
      this::writeTestReadOnlyByteArray,
      this::createTestReadOnlyByteArray,
      this::cleanTestByteArray,
      this.isReadableWritableOK()
    );
  }

  @DisplayName("Test invalid read(long, ReadableWritableByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadCallsReadableWritableByteArray(){
    return generateTestsInvalidReadCallsByteArray(
      this.getRNG(),
      "ReadableWritableByteArray",
      this::writeTestReadOnlyByteArray,
      this::createTestReadOnlyByteArray,
      this::cleanTestByteArray,
      this.isReadableWritableOK()
    );
  }

  @DisplayName("Test valid readByte(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadByteCalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(1L); // Test size of one
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readByte(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              Random testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              Random testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                assertEquals((byte) testRNG.nextInt(), testByteArray.readByte(index), "Index: " + index);
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

  @DisplayName("Test valid readShortBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadShortBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(2L); // Test size of two
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readShortBE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              int randBuf = (0xff & testRNG.nextInt()); // Pre-roll
              for(long index = 0; index < size - 1; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                short expected = (short) (0xffff & randBuf);
                assertEquals(expected, testByteArray.readShortBE(index), "Index: " + index);
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

  @DisplayName("Test valid readShortLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadShortLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(2L); // Test size of two
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readShortLE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              int randBuf = (0xff & testRNG.nextInt()); // Pre-roll
              for(long index = 0; index < size - 1; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                short expected = (short) (((0xff & randBuf) << 8) | ((0xff00 & randBuf) >> 8));
                assertEquals(expected, testByteArray.readShortLE(index), "Index: " + index);
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

  @DisplayName("Test valid readIntBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadIntBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(4L); // Test size of four
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readIntBE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              int randBuf = (0xff & testRNG.nextInt()) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())); // Pre-roll
              for(long index = 0; index < size - 3; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                assertEquals(randBuf, testByteArray.readIntBE(index), "Index: " + index);
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

  @DisplayName("Test valid readIntLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadIntLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(4L); // Test size of four
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readIntLE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              int randBuf = (0xff & testRNG.nextInt()) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())); // Pre-roll
              for(long index = 0; index < size - 3; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                int expected = (randBuf << 24) |
                               ((0xff00 & randBuf) << 8) |
                               ((0xff0000 & randBuf) >>> 8) |
                               (randBuf >>> 24);
                assertEquals(expected, testByteArray.readIntLE(index), "Index: " + index);
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

  @DisplayName("Test valid readLongBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadLongBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(8L); // Test size of eight
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readLongBE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              long randBuf = (0xff & testRNG.nextInt()) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())); // Pre-roll
              for(long index = 0; index < size - 7; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                assertEquals(randBuf, testByteArray.readLongBE(index), "Index: " + index);
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

  @DisplayName("Test valid readLongLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadLongLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(8L); // Test size of eight
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readLongLE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              long randBuf = (0xff & testRNG.nextInt()) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())); // Pre-roll
              for(long index = 0; index < size - 7; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                long expected = (randBuf << 56);
                expected |= ((0xff00 & randBuf) << 40);
                expected |= ((0xff0000 & randBuf) << 24);
                expected |= ((0xff000000L & randBuf) << 8);
                expected |= ((0xff00000000L & randBuf) >>> 8);
                expected |= ((0xff0000000000L & randBuf) >>> 24);
                expected |= ((0xff000000000000L & randBuf) >>> 40);
                expected |= (randBuf >>> 56);
                assertEquals(expected, testByteArray.readLongLE(index), "Index: " + index);
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

  @DisplayName("Test valid readFloatBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadFloatBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(4L); // Test size of four
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readFloatBE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              int randBuf = (0xff & testRNG.nextInt()) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())); // Pre-roll
              for(long index = 0; index < size - 3; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                assertEquals(Float.intBitsToFloat(randBuf), testByteArray.readFloatBE(index), "Index: " + index);
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

  @DisplayName("Test valid readFloatLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadFloatLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(4L); // Test size of four
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readFloatLE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              int randBuf = (0xff & testRNG.nextInt()) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())); // Pre-roll
              for(long index = 0; index < size - 3; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                int expected = (randBuf << 24) |
                               ((0xff00 & randBuf) << 8) |
                               ((0xff0000 & randBuf) >>> 8) |
                               (randBuf >>> 24);
                assertEquals(Float.intBitsToFloat(expected), testByteArray.readFloatLE(index), "Index: " + index);
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

  @DisplayName("Test valid readDoubleBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadDoubleBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(8L); // Test size of eight
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readDoubleBE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              long randBuf = (0xff & testRNG.nextInt()) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())); // Pre-roll
              for(long index = 0; index < size - 7; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                assertEquals(Double.longBitsToDouble(randBuf), testByteArray.readDoubleBE(index), "Index: " + index);
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

  @DisplayName("Test valid readDoubleLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidReadDoubleLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(8L); // Test size of eight
    //sizesToTest.add((long) (Short.MAX_VALUE * 4)); // Silly big
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full readDoubleLE(long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Now check them all
            {
              var testRNG = new Random(testLocalSeed);
              long randBuf = (0xff & testRNG.nextInt()) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())) << 8; // Pre-roll
              randBuf = (randBuf | (0xff & testRNG.nextInt())); // Pre-roll
              for(long index = 0; index < size - 7; index++){
                randBuf <<= 8;
                randBuf |= (0xff & testRNG.nextInt());
                long expected = (randBuf << 56);
                expected |= ((0xff00 & randBuf) << 40);
                expected |= ((0xff0000 & randBuf) << 24);
                expected |= ((0xff000000L & randBuf) << 8);
                expected |= ((0xff00000000L & randBuf) >>> 8);
                expected |= ((0xff0000000000L & randBuf) >>> 24);
                expected |= ((0xff000000000000L & randBuf) >>> 40);
                expected |= (randBuf >>> 56);
                assertEquals(Double.longBitsToDouble(expected), testByteArray.readDoubleLE(index), "Index: " + index);
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

  @DisplayName("Test valid subsetOf(long,long) calls with readByte(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidSubsetOfCallsWithReadCalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

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
      tests.add(dynamicTest(f("subset({}, {}) on ByteArray of {}B size", 0, size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to test bytearray
            {
              Random testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
              }
            }

            // Get subset
            ReadOnlyByteArray subset = testByteArray.subsetOf(0, size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(subset instanceof ReadableWritableByteArray);

            // Assert size
            assertEquals(size, subset.size());

            // Should be same object
            assertSame(testByteArray, subset);

            // Check using readByte calls
            {
              Random testRNG = new Random(testLocalSeed);
              long innerByteIndex = 0;
              for(long index = 0; index < size; index++){
                byte expected = (byte) testRNG.nextInt();
                assertEquals(expected, subset.readByte(innerByteIndex), "Index: " + innerByteIndex);
                innerByteIndex++;
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

      // Repeat trials
      for(int trial = 0; trial < TRIALS; trial++){

        // Choose byteIndex
        long byteIndex = size == 1 ? 0 : Long.max(0, rng.nextInt((int) size) - 1);

        // Choose size
        long subSize = size == 1 ? 1 : Long.min(size - byteIndex, rng.nextInt((int) size) + 1);

        // Skip test if size is same
        if(subSize == size) continue;

        // Write test
        tests.add(dynamicTest(f("subset({}, {}) on ByteArray of {}B size", byteIndex, subSize, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Write random bytes to test bytearray
              {
                Random testRNG = new Random(testLocalSeed);
                for(long index = 0; index < size; index++){
                  writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
                }
              }

              // Get subset
              ReadOnlyByteArray subset = testByteArray.subsetOf(byteIndex, subSize);

              // Assert readonly if applicable
              if(!isReadableWritableOK()) assertFalse(subset instanceof ReadableWritableByteArray);

              // Assert size
              assertEquals(subSize, subset.size());

              // Check using readByte calls
              {
                Random testRNG = new Random(testLocalSeed);
                long innerByteIndex = 0;
                for(long index = 0; index < size; index++){
                  byte expected = (byte) testRNG.nextInt();
                  if(index < byteIndex || index >= byteIndex + subSize) continue;
                  assertEquals(expected, subset.readByte(innerByteIndex), "Index: " + innerByteIndex);
                  innerByteIndex++;
                }
              }

              // Write different random bytes to test bytearray (to test if changes to original byte array propagates to subset)
              int diffTestLocalSeed = testLocalSeed + 233432;
              {
                Random testRNG = new Random(diffTestLocalSeed);
                for(long index = 0; index < size; index++){
                  writeTestReadOnlyByteArray(testByteArray, index, (byte) testRNG.nextInt());
                }
              }

              // Check using readByte calls
              {
                Random testRNG = new Random(diffTestLocalSeed);
                long innerByteIndex = 0;
                for(long index = 0; index < size; index++){
                  byte expected = (byte) testRNG.nextInt();
                  if(index < byteIndex || index >= byteIndex + subSize) continue;
                  assertEquals(expected, subset.readByte(innerByteIndex), "Index: " + innerByteIndex);
                  innerByteIndex++;
                }
              }

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

  @DisplayName("Test bad readByte(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadByteCalls(){

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
      tests.add(dynamicTest(f("readByte(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readByte(-1)
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
        tests.add(dynamicTest(f("readByte({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readByte(index)
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
      tests.add(dynamicTest(f("readByte({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readByte(size)
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
        tests.add(dynamicTest(f("readByte({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readByte(index)
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

  @DisplayName("Test bad readShortBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadShortBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readShortBE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readShortBE(-1)
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
        tests.add(dynamicTest(f("readShortBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readShortBE(index)
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
      tests.add(dynamicTest(f("readShortBE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readShortBE(size)
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
        tests.add(dynamicTest(f("readShortBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readShortBE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readShortBE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 2, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readShortBE(size - 1)
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad readShortLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadShortLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readShortLE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readShortLE(-1)
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
        tests.add(dynamicTest(f("readShortLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readShortLE(index)
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
      tests.add(dynamicTest(f("readShortLE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readShortLE(size)
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
        tests.add(dynamicTest(f("readShortLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readShortLE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readShortLE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 2, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readShortLE(size - 1)
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad readIntBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadIntBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readIntBE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readIntBE(-1)
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
        tests.add(dynamicTest(f("readIntBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readIntBE(index)
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
      tests.add(dynamicTest(f("readIntBE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readIntBE(size)
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
        tests.add(dynamicTest(f("readIntBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readIntBE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readIntBE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 4, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readIntBE(size - 1)
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad readIntLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadIntLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readIntLE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readIntLE(-1)
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
        tests.add(dynamicTest(f("readIntLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readIntLE(index)
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
      tests.add(dynamicTest(f("readIntLE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readIntLE(size)
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
        tests.add(dynamicTest(f("readIntLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readIntLE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readIntLE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 4, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readIntLE(size - 1)
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad readLongBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadLongBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readLongBE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readLongBE(-1)
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
        tests.add(dynamicTest(f("readLongBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readLongBE(index)
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
      tests.add(dynamicTest(f("readLongBE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readLongBE(size)
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
        tests.add(dynamicTest(f("readLongBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readLongBE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readLongBE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readLongBE(size - 1)
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad readLongLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadLongLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readLongLE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readLongLE(-1)
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
        tests.add(dynamicTest(f("readLongLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readLongLE(index)
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
      tests.add(dynamicTest(f("readLongLE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readLongLE(size)
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
        tests.add(dynamicTest(f("readLongLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readLongLE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readLongLE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readLongLE(size - 1)
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad readFloatBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadFloatBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readFloatBE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readFloatBE(-1)
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
        tests.add(dynamicTest(f("readFloatBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readFloatBE(index)
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
      tests.add(dynamicTest(f("readFloatBE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readFloatBE(size)
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
        tests.add(dynamicTest(f("readFloatBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readFloatBE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readFloatBE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 4, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readFloatBE(size - 1)
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad readFloatLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadFloatLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readFloatLE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readFloatLE(-1)
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
        tests.add(dynamicTest(f("readFloatLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readFloatLE(index)
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
      tests.add(dynamicTest(f("readFloatLE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readFloatLE(size)
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
        tests.add(dynamicTest(f("readFloatLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readFloatLE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readFloatLE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 4, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readFloatLE(size - 1)
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad readDoubleBE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadDoubleBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readDoubleBE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readDoubleBE(-1)
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
        tests.add(dynamicTest(f("readDoubleBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readDoubleBE(index)
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
      tests.add(dynamicTest(f("readDoubleBE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readDoubleBE(size)
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
        tests.add(dynamicTest(f("readDoubleBE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readDoubleBE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readDoubleBE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readDoubleBE(size - 1)
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
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test bad readDoubleLE(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidReadDoubleLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(1L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("readDoubleLE(-1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readDoubleLE(-1)
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
        tests.add(dynamicTest(f("readDoubleLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readDoubleLE(index)
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
      tests.add(dynamicTest(f("readDoubleLE({}) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
              () -> testByteArray.readDoubleLE(size)
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
        tests.add(dynamicTest(f("readDoubleLE({}) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            ReadOnlyByteArray testByteArray = createTestReadOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                () -> testByteArray.readDoubleLE(index)
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

      // Write test for length that exceeds capacity
      tests.add(dynamicTest(f("readDoubleLE({}) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          var testByteArray = createTestReadOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size - 1, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.readDoubleLE(size - 1)
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
    }

    // Return tests
    return tests;
  }
}
