package test.self;

import com.ansill.arrays.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.BaseByteArrayTest;
import test.TriConsumer;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.ansill.arrays.TestUtility.f;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public interface SelfByteArrayTest extends BaseByteArrayTest{

  Logger LOGGER = LoggerFactory.getLogger(SelfByteArrayTest.class);

  @Nonnull
  static Iterable<DynamicTest> generateTestsToString(
          @Nonnull Random rng,
          @Nonnull String type,
          long limit,
          @Nonnull String expectedName,
          @Nonnull Function<Long, ByteArray> testBAAllocator,
          @Nonnull TriConsumer<ByteArray, Long, Byte> testBAWriterFun,
          @Nonnull BiFunction<ByteArray, Long, Byte> testBAReaderFun,
          @Nonnull Consumer<ByteArray> testBACleanerConsumer,
          boolean isReadableWritableOk
  ) {

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Self sizes to test
    Set<Long> selfSizesToTest = new HashSet<>();
    selfSizesToTest.add(1L); // Test size of one
    selfSizesToTest.add(limit / 2);
    selfSizesToTest.add(limit - 1);
    selfSizesToTest.add(limit);
    selfSizesToTest.add(limit + 2);
    selfSizesToTest.add(limit * 2);
    selfSizesToTest.add((long) Short.MAX_VALUE); // Big enough
    for (int trial = 0; trial < BaseByteArrayTest.TRIALS; trial++) { // Add random sizes to try
      if (selfSizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for (long selfSize : selfSizesToTest) {

      // Test-local RNG
      int testLocalRNG = rng.nextInt();

      // Test
      tests.add(dynamicTest(
              f("test toString() on {} of {}B size", type, selfSize),
              () -> {

                // Wrap in try to make sure memory gets cleaned up
                try {

                  // Allocate test array
                  ByteArray testArray = testBAAllocator.apply(selfSize);

                  // Assert readonly if applicable
                  if (!isReadableWritableOk) assertFalse(testArray instanceof ReadableWritableByteArray);

                  // Try and finally to clean up test array
                  try {

                    // Write
                    {
                      Random random = new Random(testLocalRNG);
                      for (long i = 0; i < testArray.size(); i++) {
                        testBAWriterFun.accept(testArray, i, (byte) random.nextInt());
                      }
                    }

                    // Test it
                    final String stringRepresentation = testArray.toString();

                    // Assert it
                    assertNotNull(stringRepresentation);
                    assertTrue(stringRepresentation.startsWith(expectedName), format("toString output does not start with expected '%s' - full: %s", expectedName, stringRepresentation));
                    var workingString = stringRepresentation.substring(expectedName.length());
                    assertTrue(workingString.startsWith("("), format("toString output does not have the expected '(' - full: %s", stringRepresentation));
                    assertTrue(workingString.endsWith(")"), format("toString output does not have the expected ')' - full: %s", stringRepresentation));
                    workingString = workingString.substring(1, workingString.length() - 1);
                    assertTrue(workingString.startsWith("size=" + selfSize + ", "), format("toString output does not have the expected 'size=%s, ' - full: %s", selfSize, stringRepresentation));
                    workingString = workingString.substring(("size=" + selfSize + ", ").length());
                    assertTrue(workingString.startsWith("content="), format("toString output does not have the expected 'content=' - full: %s", stringRepresentation));
                    workingString = workingString.substring(("content=").length());
                    assertTrue(workingString.startsWith("["), format("toString output does not have the expected '[' - full: %s", stringRepresentation));
                    assertTrue(workingString.endsWith("]"), format("toString output does not have the expected ']' - full: %s", stringRepresentation));
                    workingString = workingString.substring(1, workingString.length() - 1);
                    if (selfSize > limit) {
                      assertTrue(workingString.endsWith("..."), format("toString output does not have the expected '...' - full: %s", stringRepresentation));
                      workingString = workingString.substring(0, workingString.length() - "...".length());
                    }
                    long maxSizeToIterate = Long.min(selfSize, limit);
                    for (long i = 0; i < maxSizeToIterate; i++) {
                      assertTrue(workingString.length() >= 2, format("reached to an unexpected end of toString string - full: %s", stringRepresentation));
                      var hex = workingString.substring(0, 2);
                      workingString = workingString.substring(2);
                      byte cur = testBAReaderFun.apply(testArray, i);
                      assertEquals(getHex(cur), hex, "index " + i);
                      if (i < maxSizeToIterate - 1) {
                        assertTrue(workingString.startsWith("_"), format("toString output does not have the expected '_' - full: %s", stringRepresentation));
                        workingString = workingString.substring(1);
                      }
                    }
                    assertTrue(workingString.isBlank(), format("toString output unexpected characters after consuming string... - full: %s", stringRepresentation));

                    // Check testArray for any side effects
                    {
                      Random random = new Random(testLocalRNG);
                      for (long i = 0; i < testArray.size(); i++) {
                        assertEquals((byte) random.nextInt(), testBAReaderFun.apply(testArray, i));
                      }
                    }

                  } finally {
                    testBACleanerConsumer.accept(testArray);
                  }
                } catch (OutOfMemoryError oom) {
                  System.gc();
                  oom.printStackTrace();
                  fail("Cannot perform test due to insufficient memory space");
                }

                // Clean up
                System.gc();
              }
      ));
    }

    // Return tests
    return tests;
  }

  @Nonnull
  static String getHex(byte value) {

    // Convert to hex
    var hexValue = Long.toHexString(value & 0xffL);

    // Prefix if one char
    if (hexValue.length() == 1) hexValue = "0" + hexValue;

    // Return
    return hexValue;
  }

  @DisplayName("Test toString()")
  @TestFactory
  Iterable<DynamicTest> testToString();

  @DisplayName("Test bad subsetOf(long,long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidSubsetOfCalls(){

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
      tests.add(dynamicTest(f("subsetOf(-1,1) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the bytearray
          ByteArray testByteArray = createTestByteArray(size);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkSubsetOf(-1, 1, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.subsetOf(-1, 1)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("Out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("subsetOf({},1) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the bytearray
            ByteArray testByteArray = createTestByteArray(size);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkSubsetOf(index, 1, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.subsetOf(index, 1)
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
      tests.add(dynamicTest(f("subsetOf({},1) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the bytearray
          ByteArray testByteArray = createTestByteArray(size);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkSubsetOf(size, 1, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.subsetOf(size, 1)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("Out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that has zero capacity
      if(size > 1) {
        for (int trial = 0; trial < TRIALS; trial++) {
          long index = rng.nextInt((int) (size - 1));
          tests.add(dynamicTest(f("subsetOf({},0) on ByteArray of {}B size", index, size), () -> {

            // Wrap in try and catch for possible OOM if trying to allocate max memory
            try {

              // Allocate the bytearray
              ByteArray testByteArray = createTestByteArray(size);

              try {

                // Assert size
                assertEquals(size, testByteArray.size());

                // Get the expected exception
                ByteArrayInvalidLengthException expectedEx = assertThrows(
                        ByteArrayInvalidLengthException.class,
                        () -> IndexingUtility.checkSubsetOf(index, 0, size)
                );

                // Now test the byte array
                ByteArrayInvalidLengthException actualEx = assertThrows(
                        ByteArrayInvalidLengthException.class,
                        () -> testByteArray.subsetOf(index, 0)
                );

                // Check the message
                assertEquals(expectedEx.getMessage(), actualEx.getMessage());

              } finally {
                cleanTestByteArray(testByteArray);
              }

            } catch (OutOfMemoryError oom) {
              System.gc();
              oom.printStackTrace();
              LOGGER.warn("Out of memory. Cannot perform this test due to insufficient memory space");
              fail("Cannot perform test due to insufficient memory space");
            }

            // Clean up
            System.gc();
          }));
        }
      }

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("subsetOf({},1) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the bytearray
            ByteArray testByteArray = createTestByteArray(size);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkSubsetOf(index, 1, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.subsetOf(index, 1)
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

      // Write test for bad size
      tests.add(dynamicTest(f("subsetOf(0,{}) on ByteArray of {}B size", -1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the bytearray
          ByteArray testByteArray = createTestByteArray(size);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayInvalidLengthException expectedEx = assertThrows(
              ByteArrayInvalidLengthException.class,
              () -> IndexingUtility.checkSubsetOf(0, -1, size)
            );

            // Now test the byte array
            ByteArrayInvalidLengthException actualEx = assertThrows(
              ByteArrayInvalidLengthException.class,
              () -> testByteArray.subsetOf(0, -1)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("Out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for size that exceeds capacity
      tests.add(dynamicTest(f("subsetOf(0,{}) on ByteArray of {}B size", size + 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the bytearray
          ByteArray testByteArray = createTestByteArray(size);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayLengthOverBoundsException expectedEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> IndexingUtility.checkSubsetOf(0, size + 1, size)
            );

            // Now test the byte array
            ByteArrayLengthOverBoundsException actualEx = assertThrows(
              ByteArrayLengthOverBoundsException.class,
              () -> testByteArray.subsetOf(0, size + 1)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("Out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = 1 + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("subsetOf({},{}) on ByteArray of {}B size", index, size, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the bytearray
            ByteArray testByteArray = createTestByteArray(size);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkSubsetOf(index, size, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.subsetOf(index, size)
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
}
