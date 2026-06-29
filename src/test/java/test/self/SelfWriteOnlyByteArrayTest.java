package test.self;

import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ByteArrayLengthOverBoundsException;
import com.ansill.arrays.IndexingUtility;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.BaseWriteOnlyByteArrayTest;
import test.arrays.TestOnlyByteArray;

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

import static com.ansill.arrays.TestUtility.f;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public interface SelfWriteOnlyByteArrayTest extends BaseWriteOnlyByteArrayTest, SelfByteArrayTest{

  Logger LOGGER = LoggerFactory.getLogger(SelfWriteOnlyByteArrayTest.class);
  
  @Nonnull
  static Iterable<DynamicTest> generateTestsInvalidCopyFromCallsByteArray(
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

      // Test null bytearray
      tests.add(dynamicTest(
              f("test copyFrom(0, null) on ByteArray of {}B size", selfSize),
              () -> {

                // Wrap in try to make sure memory gets cleaned up
                try{

                  // Allocate test array
                  WriteOnlyByteArray testArray = testWOBAAllocator.apply(selfSize);

                  // Assert readonly if applicable
                  if(!isReadableWritableOk) assertFalse(testArray instanceof ReadableWritableByteArray);

                  // Try and finally to clean up test array
                  try{

                    // Build the expected exception
                    IllegalArgumentException expected = assertThrows(
                            IllegalArgumentException.class,
                            () -> IndexingUtility.checkCopyFrom(0, null, selfSize)
                    );

                    // Test it
	                  //noinspection DataFlowIssue
                    IllegalArgumentException actual = assertThrows(
                            IllegalArgumentException.class,
                            () -> testArray.copyFrom(0, null)
                    );

                    // Compare messages
                    assertEquals(expected.getMessage(), actual.getMessage());

                    // Check testArray for any side effects
                    for(long i = 0; i < testArray.size(); i++){
                      assertEquals((byte) 0, testBAReaderFun.apply(testArray, i));
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

      // Test too-large bytearray
      tests.add(dynamicTest(
        f("test copyFrom(0, {}(size={})) on ByteArray of {}B size", type, selfSize + 1, selfSize),
        () -> {

          // Wrap in try to make sure memory gets cleaned up
          try{

            // Allocate test array
            WriteOnlyByteArray testArray = testWOBAAllocator.apply(selfSize);

            // Assert readonly if applicable
            if(!isReadableWritableOk) assertFalse(testArray instanceof ReadableWritableByteArray);

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
                () -> IndexingUtility.checkCopyFrom(0, control, selfSize)
              );

              // Test it
              ByteArrayLengthOverBoundsException actual = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> testArray.copyFrom(0, control)
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
          "test copyTo({}, {}(size={})) on ByteArray of {}B size",
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
            if(!isReadableWritableOk) assertFalse(testArray instanceof ReadableWritableByteArray);

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
                () -> IndexingUtility.checkCopyFrom(byteIndex, control, selfSize)
              );

              // Test it
              ByteArrayIndexOutOfBoundsException actual = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testArray.copyFrom(byteIndex, control)
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
          "test copyTo({}, {}(size={})) on ByteArray of {}B size",
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
            if(!isReadableWritableOk) assertFalse(testArray instanceof ReadableWritableByteArray);

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
                () -> IndexingUtility.checkCopyFrom(byteIndex, control, selfSize)
              );

              // Test it
              ByteArrayIndexOutOfBoundsException actual = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testArray.copyFrom(byteIndex, control)
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

  @DisplayName("Test invalid copyFrom(long, ReadOnlyByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidCopyFromCallsReadOnlyByteArray(){
    return generateTestsInvalidCopyFromCallsByteArray(
      this.getRNG(),
      "WriteOnlyByteArray",
      this::readTestByteArray,
      this::createTestWriteOnlyByteArray,
      this::cleanTestByteArray,
      this.isReadableWritableOK()
    );
  }

  @DisplayName("Test invalid copyFrom(long, ReadableWritableByteArray) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidCopyFromCallsReadableWritableByteArray(){
    return generateTestsInvalidCopyFromCallsByteArray(
      this.getRNG(),
      "ReadableWritableByteArray",
      this::readTestByteArray,
      this::createTestWriteOnlyByteArray,
      this::cleanTestByteArray,
      this.isReadableWritableOK()
    );
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
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

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
                  byte testVal = readTestByteArray(testByteArray, i);
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
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeShortBE(long, short) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteShortBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(2L); // Test size of one
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
      tests.add(dynamicTest(f("full writeShortBE(long, short) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                controlOne.writeByte(index, (byte) testRNG.nextInt());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              IntStream.range(0, Math.toIntExact(size - 1)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 1));

                // Write it
                testByteArray.writeShortBE(byteIndex, (short) val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
              IntStream.range(0, Math.toIntExact(size - 1)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlTwo.readByte(byteIndex)) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex + 1));

                // Write it
                testByteArray.writeShortBE(byteIndex, (short) val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeShortLE(long, short) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteShortLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(2L); // Test size of one
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
      tests.add(dynamicTest(f("full writeShortLE(long, short) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                controlOne.writeByte(index, (byte) testRNG.nextInt());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              IntStream.range(0, Math.toIntExact(size - 1)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 1));

                // Write it
                testByteArray.writeShortBE(byteIndex, (short) val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
              IntStream.range(0, Math.toIntExact(size - 1)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlTwo.readByte(byteIndex + 1)) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex));

                // Write it
                testByteArray.writeShortLE(byteIndex, (short) val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeIntBE(long, int) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteIntBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(4L); // Test size of four
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
      tests.add(dynamicTest(f("full writeIntBE(long, int) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                controlOne.writeByte(index, (byte) testRNG.nextInt());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              IntStream.range(0, Math.toIntExact(size - 3)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 2))) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 3));

                // Write it
                testByteArray.writeIntBE(byteIndex, val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
              IntStream.range(0, Math.toIntExact(size - 3)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlTwo.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 2))) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex + 3));

                // Write it
                testByteArray.writeIntBE(byteIndex, val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeIntLE(long, int) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteIntLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(4L); // Test size of four
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
      tests.add(dynamicTest(f("full writeIntLE(long, int) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                controlOne.writeByte(index, (byte) testRNG.nextInt());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              IntStream.range(0, Math.toIntExact(size - 3)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 2))) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 3));

                // Write it
                testByteArray.writeIntBE(byteIndex, val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
              IntStream.range(0, Math.toIntExact(size - 3)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlTwo.readByte(byteIndex + 3)) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 1))) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex));

                // Write it
                testByteArray.writeIntLE(byteIndex, val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeLongBE(long, long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteLongBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(8L); // Test size of eight
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
      tests.add(dynamicTest(f("full writeLongBE(long, long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                controlOne.writeByte(index, (byte) testRNG.nextInt());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              IntStream.range(0, Math.toIntExact(size - 7)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                long val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 3))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 4))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 5))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 6))) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 7));

                // Write it
                testByteArray.writeLongBE(byteIndex, val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);
                written.add(byteIndex + 4);
                written.add(byteIndex + 5);
                written.add(byteIndex + 6);
                written.add(byteIndex + 7);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
              IntStream.range(0, Math.toIntExact(size - 7)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                long val = (0xff & controlTwo.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 3))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 4))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 5))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 6))) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex + 7));

                // Write it
                testByteArray.writeLongBE(byteIndex, val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);
                written.add(byteIndex + 4);
                written.add(byteIndex + 5);
                written.add(byteIndex + 6);
                written.add(byteIndex + 7);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeLongLE(long, long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteLongLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(8L); // Test size of eight
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
      tests.add(dynamicTest(f("full writeLongLE(long, long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size; index++){
                controlOne.writeByte(index, (byte) testRNG.nextInt());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              IntStream.range(0, Math.toIntExact(size - 7)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                long val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 3))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 4))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 5))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 6))) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 7));

                // Write it
                testByteArray.writeLongBE(byteIndex, val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);
                written.add(byteIndex + 4);
                written.add(byteIndex + 5);
                written.add(byteIndex + 6);
                written.add(byteIndex + 7);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
              IntStream.range(0, Math.toIntExact(size - 7)).forEach(indices::add);
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                long val = (0xff & controlTwo.readByte(byteIndex + 7)) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 6))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 5))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 4))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 3))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 1))) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex));

                // Write it
                testByteArray.writeLongLE(byteIndex, val);

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);
                written.add(byteIndex + 4);
                written.add(byteIndex + 5);
                written.add(byteIndex + 6);
                written.add(byteIndex + 7);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeFloatBE(long, float) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteFloatBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(4L); // Test size of four
    //sizesToTest.add((long) Short.MAX_VALUE); // Big enough
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((((long) rng.nextInt(500) + 5) / 4) * 4)) continue; // Round down to nearest 4
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full writeFloatBE(long, float) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size / 4; index++){
                controlOne.writeFloatBE(index * 4, testRNG.nextInt() * testRNG.nextFloat());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              LinkedList<Integer> finalIndices = indices;
              IntStream.range(0, Math.toIntExact(size - 3)).forEach(e -> finalIndices.add((e / 4) *
                                                                                          4)); // Round to nearest 4
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 2))) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 3));

                // Write it
                testByteArray.writeFloatBE(byteIndex, Float.intBitsToFloat(val));

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
                  if(written.contains(i)) assertEquals(controlOne.readByte(i), testVal);
                  else assertEquals(0, testVal);
                }
              }
            }

            // Come up with new control byte array (to check for overwrite correctness)
            var controlTwo = new TestOnlyByteArray(size);
            {
              Random testRNG = new Random(testLocalSeed + 342);
              for(long index = 0; index < size / 4; index++){
                controlTwo.writeFloatBE(index * 4, testRNG.nextInt() * testRNG.nextFloat());
              }
            }

            // Randomize the write order
            {
              indices = new LinkedList<>();
              Random testRNG = new Random(testLocalSeed + 232);
              LinkedList<Integer> finalIndices1 = indices;
              IntStream.range(0, Math.toIntExact(size - 3)).forEach(e -> finalIndices1.add((e / 4) * 4));
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlTwo.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 2))) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex + 3));

                // Write it
                testByteArray.writeFloatBE(byteIndex, Float.intBitsToFloat(val));

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeFloatLE(long, float) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteFloatLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(4L); // Test size of four
    //sizesToTest.add((long) Short.MAX_VALUE); // Big enough
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((((long) rng.nextInt(500) + 5) / 4) * 4)) continue; // Round down to nearest 4
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full writeFloatLE(long, float) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size / 4; index++){
                controlOne.writeFloatBE(index * 4, testRNG.nextInt() * testRNG.nextFloat());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              LinkedList<Integer> finalIndices = indices;
              IntStream.range(0, Math.toIntExact(size - 3)).forEach(e -> finalIndices.add((e / 4) *
                      4)); // Round to nearest 4
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 2))) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 3));

                // Write it
                testByteArray.writeFloatBE(byteIndex, Float.intBitsToFloat(val));

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
                  if(written.contains(i)) assertEquals(controlOne.readByte(i), testVal);
                  else assertEquals(0, testVal);
                }
              }
            }

            // Come up with new control byte array (to check for overwrite correctness)
            var controlTwo = new TestOnlyByteArray(size);
            {
              Random testRNG = new Random(testLocalSeed + 342);
              for(long index = 0; index < size / 4; index++){
                controlTwo.writeFloatLE(index * 4, testRNG.nextInt() * testRNG.nextFloat());
              }
            }

            // Randomize the write order
            {
              indices = new LinkedList<>();
              Random testRNG = new Random(testLocalSeed + 232);
              LinkedList<Integer> finalIndices1 = indices;
              IntStream.range(0, Math.toIntExact(size - 3)).forEach(e -> finalIndices1.add((e / 4) * 4));
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                int val = (0xff & controlTwo.readByte(byteIndex + 3)) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 1))) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex));

                // Write it
                testByteArray.writeFloatLE(byteIndex, Float.intBitsToFloat(val));

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeDoubleBE(long, double) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteDoubleBECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(8L); // Test size of eight
    //sizesToTest.add((long) Short.MAX_VALUE); // Big enough
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((((long) rng.nextInt(500) + 5) / 8) * 8)) continue; // Round to nearest 8
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full writeDoubleBE(long, double) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size / 8; index++){
                controlOne.writeDoubleBE(index * 8, testRNG.nextLong() * testRNG.nextDouble());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              LinkedList<Integer> finalIndices = indices;
              IntStream.range(0, Math.toIntExact(size - 7)).forEach(e -> finalIndices.add((e / 8) *
                                                                                          8)); // Round to nearest 8
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                long val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 3))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 4))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 5))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 6))) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 7));

                // Write it
                testByteArray.writeDoubleBE(byteIndex, Double.longBitsToDouble(val));

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);
                written.add(byteIndex + 4);
                written.add(byteIndex + 5);
                written.add(byteIndex + 6);
                written.add(byteIndex + 7);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
                  if(written.contains(i)) assertEquals(controlOne.readByte(i), testVal);
                  else assertEquals(0, testVal);
                }
              }
            }

            // Come up with new control byte array (to check for overwrite correctness)
            ReadableWritableByteArray controlTwo = new TestOnlyByteArray(size);
            {
              Random testRNG = new Random(testLocalSeed + 342);
              for(long index = 0; index < size / 8; index++){
                controlTwo.writeDoubleBE(index * 8, testRNG.nextLong() * testRNG.nextDouble());
              }
            }

            // Randomize the write order
            {
              indices = new LinkedList<>();
              Random testRNG = new Random(testLocalSeed + 232);
              LinkedList<Integer> finalIndices1 = indices;
              IntStream.range(0, Math.toIntExact(size - 7)).forEach(e -> finalIndices1.add((e / 8) *
                                                                                           8)); // Round to nearest 8
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                long val = (0xff & controlTwo.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 3))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 4))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 5))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 6))) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex + 7));

                // Write it
                testByteArray.writeDoubleBE(byteIndex, Double.longBitsToDouble(val));

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);
                written.add(byteIndex + 4);
                written.add(byteIndex + 5);
                written.add(byteIndex + 6);
                written.add(byteIndex + 7);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));
    }

    // Return tests
    return tests;
  }

  @DisplayName("Test valid writeDoubleLE(long, double) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidWriteDoubleLECalls(){

    // Set up test container
    var tests = new LinkedList<DynamicTest>();

    // Get RNG
    var rng = getRNG();

    // Sizes to test
    var sizesToTest = new HashSet<Long>();
    sizesToTest.add(8L); // Test size of eight
    //sizesToTest.add((long) Short.MAX_VALUE); // Big enough
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((((long) rng.nextInt(500) + 5) / 8) * 8)) continue; // Round to nearest 8
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Get test-local RNG seed
      int testLocalSeed = (int) (rng.nextInt() + size);

      // Write test
      tests.add(dynamicTest(f("full writeDoubleLE(long, double) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the writeonly bytearray
          var testByteArray = createTestWriteOnlyByteArray(size);

          // Assert writeonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Write random bytes to control bytearray for later reference
            var controlOne = new TestOnlyByteArray(size);
            {
              var testRNG = new Random(testLocalSeed);
              for(long index = 0; index < size / 8; index++){
                controlOne.writeDoubleLE(index * 8, testRNG.nextLong() * testRNG.nextDouble());
              }
            }

            // Randomize the write order
            var indices = new LinkedList<Integer>();
            {
              var testRNG = new Random(testLocalSeed);
              LinkedList<Integer> finalIndices = indices;
              IntStream.range(0, Math.toIntExact(size - 7)).forEach(e -> finalIndices.add((e / 8) *
                      8)); // Round to nearest 8
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              var written = new HashSet<Integer>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                long val = (0xff & controlOne.readByte(byteIndex)) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 1))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 3))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 4))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 5))) << 8;
                val = (val | (0xff & controlOne.readByte(byteIndex + 6))) << 8;
                val |= (0xff & controlOne.readByte(byteIndex + 7));

                // Write it
                testByteArray.writeDoubleBE(byteIndex, Double.longBitsToDouble(val));

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);
                written.add(byteIndex + 4);
                written.add(byteIndex + 5);
                written.add(byteIndex + 6);
                written.add(byteIndex + 7);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
                  if(written.contains(i)) assertEquals(controlOne.readByte(i), testVal);
                  else assertEquals(0, testVal);
                }
              }
            }

            // Come up with new control byte array (to check for overwrite correctness)
            ReadableWritableByteArray controlTwo = new TestOnlyByteArray(size);
            {
              Random testRNG = new Random(testLocalSeed + 342);
              for(long index = 0; index < size / 8; index++){
                controlTwo.writeDoubleBE(index * 8, testRNG.nextLong() * testRNG.nextDouble());
              }
            }

            // Randomize the write order
            {
              indices = new LinkedList<>();
              Random testRNG = new Random(testLocalSeed + 232);
              LinkedList<Integer> finalIndices1 = indices;
              IntStream.range(0, Math.toIntExact(size - 7)).forEach(e -> finalIndices1.add((e / 8) *
                      8)); // Round to nearest 8
              Collections.shuffle(indices, testRNG);
            }

            // Loop until no more indices to try
            {
              Set<Integer> written = new HashSet<>();
              while(!indices.isEmpty()){

                // Get index to write
                int byteIndex = indices.remove(0);

                // Rearrange value
                long val = (0xff & controlTwo.readByte(byteIndex + 7)) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 6))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 5))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 4))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 3))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 2))) << 8;
                val = (val | (0xff & controlTwo.readByte(byteIndex + 1))) << 8;
                val |= (0xff & controlTwo.readByte(byteIndex));

                // Write it
                testByteArray.writeDoubleLE(byteIndex, Double.longBitsToDouble(val));

                // Add to written set
                written.add(byteIndex);
                written.add(byteIndex + 1);
                written.add(byteIndex + 2);
                written.add(byteIndex + 3);
                written.add(byteIndex + 4);
                written.add(byteIndex + 5);
                written.add(byteIndex + 6);
                written.add(byteIndex + 7);

                // Check it (slow, I know)
                for(int i = 0; i < controlOne.size(); i++){
                  byte testVal = readTestByteArray(testByteArray, i);
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
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
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
              () -> testByteArray.writeByte(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
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
              () -> testByteArray.writeByte(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
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

  @DisplayName("Test bad writeShortBE(long, short) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteShortBECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(2L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeShortBE(-1,short) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(-1, 2, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeShortBE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeShortBE({},short) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 2, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeShortBE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeShortBE({},short) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size, 2, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeShortBE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      tests.add(dynamicTest(f("writeShortBE({},short) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

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
              () -> testByteArray.writeShortBE(size - 1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeShortBE({},short) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 2, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeShortBE(index, (byte) 0)
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

  @DisplayName("Test bad writeShortLE(long, short) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteShortLECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(2L); // Test size of one
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeShortLE(-1,short) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(-1, 2, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeShortLE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeShortLE({},short) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 2, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeShortLE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeShortLE({},short) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(size, 2, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeShortLE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      tests.add(dynamicTest(f("writeShortLE({},short) on ByteArray of {}B size", size - 1, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

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
                    () -> testByteArray.writeShortLE(size - 1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeShortLE({},short) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 2, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeShortLE(index, (byte) 0)
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

  @DisplayName("Test bad writeIntBE(long, int) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteIntBECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(4L); // Test size of four
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeIntBE(-1,int) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(-1, 4, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeIntBE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeIntBE({},int) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 4, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeIntBE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeIntBE({},int) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size, 4, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeIntBE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      for(int offset = 1; offset < 4; offset++){
        int finalOffset = offset;
        tests.add(dynamicTest(f("writeIntBE({},int) on ByteArray of {}B size", size - offset, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            var testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> IndexingUtility.checkReadWrite(size - finalOffset, 4, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> testByteArray.writeIntBE(size - finalOffset, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
            }

          }catch(OutOfMemoryError oom){
            System.gc();
            oom.printStackTrace();
            LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space");
            fail("Cannot perform test due to insufficient memory space");
          }

          // Clean up
          System.gc();
        }));
      }

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeIntBE({},int) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 4, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeIntBE(index, (byte) 0)
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

  @DisplayName("Test bad writeIntLE(long, int) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteIntLECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(4L); // Test size of four
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeIntLE(-1,int) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(-1, 4, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeIntLE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeIntLE({},int) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 4, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeIntLE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeIntLE({},int) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(size, 4, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeIntLE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      for(int offset = 1; offset < 4; offset++){
        int finalOffset = offset;
        tests.add(dynamicTest(f("writeIntLE({},int) on ByteArray of {}B size", size - offset, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            var testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                      ByteArrayLengthOverBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(size - finalOffset, 4, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                      ByteArrayLengthOverBoundsException.class,
                      () -> testByteArray.writeIntLE(size - finalOffset, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
            }

          }catch(OutOfMemoryError oom){
            System.gc();
            oom.printStackTrace();
            LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space");
            fail("Cannot perform test due to insufficient memory space");
          }

          // Clean up
          System.gc();
        }));
      }

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeIntLE({},int) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 4, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeIntLE(index, (byte) 0)
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

  @DisplayName("Test bad writeLongBE(long, long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteLongBECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(8L); // Test size of four
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeLongBE(-1,long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(-1, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeLongBE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeLongBE({},long) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeLongBE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeLongBE({},long) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeLongBE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      for(int offset = 1; offset < 8; offset++){
        int finalOffset = offset;
        tests.add(dynamicTest(f("writeLongBE({},long) on ByteArray of {}B size", size - offset, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            var testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> IndexingUtility.checkReadWrite(size - finalOffset, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> testByteArray.writeLongBE(size - finalOffset, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
            }

          }catch(OutOfMemoryError oom){
            System.gc();
            oom.printStackTrace();
            LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space");
            fail("Cannot perform test due to insufficient memory space");
          }

          // Clean up
          System.gc();
        }));
      }

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeLongBE({},long) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeLongBE(index, (byte) 0)
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

  @DisplayName("Test bad writeLongLE(long, long) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteLongLECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(8L); // Test size of four
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeLongLE(-1,long) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(-1, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeLongLE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeLongLE({},long) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeLongLE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeLongLE({},long) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(size, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeLongLE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      for(int offset = 1; offset < 8; offset++){
        int finalOffset = offset;
        tests.add(dynamicTest(f("writeLongLE({},long) on ByteArray of {}B size", size - offset, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            var testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                      ByteArrayLengthOverBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(size - finalOffset, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                      ByteArrayLengthOverBoundsException.class,
                      () -> testByteArray.writeLongLE(size - finalOffset, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
            }

          }catch(OutOfMemoryError oom){
            System.gc();
            oom.printStackTrace();
            LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space");
            fail("Cannot perform test due to insufficient memory space");
          }

          // Clean up
          System.gc();
        }));
      }

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeLongLE({},long) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeLongLE(index, (byte) 0)
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

  @DisplayName("Test bad writeFloatBE(long, float) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteFloatBECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(4L); // Test size of four
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeFloatBE(-1,float) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(-1, 4, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeFloatBE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeFloatBE({},float) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 4, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeFloatBE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeFloatBE({},float) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size, 4, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeFloatBE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      for(int offset = 1; offset < 4; offset++){
        int finalOffset = offset;
        tests.add(dynamicTest(f("writeFloatBE({},float) on ByteArray of {}B size", size - offset, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            var testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> IndexingUtility.checkReadWrite(size - finalOffset, 4, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> testByteArray.writeFloatBE(size - finalOffset, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
            }

          }catch(OutOfMemoryError oom){
            System.gc();
            oom.printStackTrace();
            LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space");
            fail("Cannot perform test due to insufficient memory space");
          }

          // Clean up
          System.gc();
        }));
      }

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeFloatBE({},float) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 4, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeFloatBE(index, (byte) 0)
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

  @DisplayName("Test bad writeFloatLE(long, float) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteFloatLECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(4L); // Test size of four
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeFloatLE(-1,float) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(-1, 4, size)
            );

            // Now test the byte array
            ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeFloatLE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeFloatLE({},float) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 4, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeFloatLE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeFloatLE({},float) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(size, 4, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeFloatLE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      for(int offset = 1; offset < 4; offset++){
        int finalOffset = offset;
        tests.add(dynamicTest(f("writeFloatLE({},float) on ByteArray of {}B size", size - offset, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            var testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                      ByteArrayLengthOverBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(size - finalOffset, 4, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                      ByteArrayLengthOverBoundsException.class,
                      () -> testByteArray.writeFloatLE(size - finalOffset, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
            }

          }catch(OutOfMemoryError oom){
            System.gc();
            oom.printStackTrace();
            LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space");
            fail("Cannot perform test due to insufficient memory space");
          }

          // Clean up
          System.gc();
        }));
      }

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeFloatLE({},float) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              ByteArrayIndexOutOfBoundsException expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 4, size)
              );

              // Now test the byte array
              ByteArrayIndexOutOfBoundsException actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeFloatLE(index, (byte) 0)
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

  @DisplayName("Test bad writeDoubleBE(long, double) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteDoubleBECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(8L); // Test size of four
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeDoubleBE(-1,double) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(-1, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeDoubleBE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeDoubleBE({},double) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeDoubleBE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeDoubleBE({},double) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> IndexingUtility.checkReadWrite(size, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
              ByteArrayIndexOutOfBoundsException.class,
              () -> testByteArray.writeDoubleBE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      for(int offset = 1; offset < 8; offset++){
        int finalOffset = offset;
        tests.add(dynamicTest(f("writeDoubleBE({},double) on ByteArray of {}B size", size - offset, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            var testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> IndexingUtility.checkReadWrite(size - finalOffset, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                ByteArrayLengthOverBoundsException.class,
                () -> testByteArray.writeDoubleBE(size - finalOffset, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
            }

          }catch(OutOfMemoryError oom){
            System.gc();
            oom.printStackTrace();
            LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space");
            fail("Cannot perform test due to insufficient memory space");
          }

          // Clean up
          System.gc();
        }));
      }

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeDoubleBE({},double) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> IndexingUtility.checkReadWrite(index, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                ByteArrayIndexOutOfBoundsException.class,
                () -> testByteArray.writeDoubleBE(index, (byte) 0)
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

  @DisplayName("Test bad writeDoubleLE(long, double) calls")
  @TestFactory
  default Iterable<DynamicTest> testInvalidWriteDoubleLECalls(){

    // Set up test container
    List<DynamicTest> tests = new LinkedList<>();

    // Get RNG
    Random rng = getRNG();

    // Sizes to test
    Set<Long> sizesToTest = new HashSet<>();
    sizesToTest.add(8L); // Test size of four
    for(int trial = 0; trial < TRIALS; trial++){ // Add random sizes to try
      if(sizesToTest.add((long) rng.nextInt(500) + 5)) continue;
      trial--; // Existing number, try again
    }

    // Run the tests
    for(long size : sizesToTest){

      // Write test for negative index (-1)
      tests.add(dynamicTest(f("writeDoubleLE(-1,double) on ByteArray of {}B size", size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(-1, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeDoubleLE(-1, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for negative indices (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = -Math.abs(rng.nextInt() + 500_000);
        tests.add(dynamicTest(f("writeDoubleLE({},double) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeDoubleLE(index, (byte) 0)
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

      // Write test for index that exceeds capacity (Throws ByteArrayIndexOutOfBoundsException)
      tests.add(dynamicTest(f("writeDoubleLE({},double) on ByteArray of {}B size", size, size), () -> {

        // Wrap in try and catch for possible OOM if trying to allocate max memory
        try{

          // Allocate the readonly bytearray
          WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

          // Assert readonly if applicable
          if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

          try{

            // Assert size
            assertEquals(size, testByteArray.size());

            // Get the expected exception
            var expectedEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> IndexingUtility.checkReadWrite(size, 8, size)
            );

            // Now test the byte array
            var actualEx = assertThrows(
                    ByteArrayIndexOutOfBoundsException.class,
                    () -> testByteArray.writeDoubleLE(size, (byte) 0)
            );

            // Check the message
            assertEquals(expectedEx.getMessage(), actualEx.getMessage());

          }finally{
            cleanTestByteArray(testByteArray);
          }

        }catch(OutOfMemoryError oom){
          System.gc();
          LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space", oom);
          fail("Cannot perform test due to insufficient memory space");
        }

        // Clean up
        System.gc();
      }));

      // Write test for index that exceeds capacity (Throws ByteArrayLengthOverBoundsException)
      for(int offset = 1; offset < 8; offset++){
        int finalOffset = offset;
        tests.add(dynamicTest(f("writeDoubleLE({},double) on ByteArray of {}B size", size - offset, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            var testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                      ByteArrayLengthOverBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(size - finalOffset, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                      ByteArrayLengthOverBoundsException.class,
                      () -> testByteArray.writeDoubleLE(size - finalOffset, (byte) 0)
              );

              // Check the message
              assertEquals(expectedEx.getMessage(), actualEx.getMessage());

            }finally{
              cleanTestByteArray(testByteArray);
            }

          }catch(OutOfMemoryError oom){
            System.gc();
            oom.printStackTrace();
            LOGGER.warn("out of memory. Cannot perform this test due to insufficient memory space");
            fail("Cannot perform test due to insufficient memory space");
          }

          // Clean up
          System.gc();
        }));
      }

      // Write test for index that exceeds capacity (random)
      for(int trial = 0; trial < TRIALS; trial++){
        long index = size + Math.abs(rng.nextInt());
        tests.add(dynamicTest(f("writeDoubleLE({},double) on ByteArray of {}B size", index, size), () -> {

          // Wrap in try and catch for possible OOM if trying to allocate max memory
          try{

            // Allocate the readonly bytearray
            WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(size);

            // Assert readonly if applicable
            if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

            try{

              // Assert size
              assertEquals(size, testByteArray.size());

              // Get the expected exception
              var expectedEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> IndexingUtility.checkReadWrite(index, 8, size)
              );

              // Now test the byte array
              var actualEx = assertThrows(
                      ByteArrayIndexOutOfBoundsException.class,
                      () -> testByteArray.writeDoubleLE(index, (byte) 0)
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

  @DisplayName("Test valid subsetOf(long,long) calls with writeByte(long) calls")
  @TestFactory
  default Iterable<DynamicTest> testValidSubsetOfCallsWithWriteCalls(){

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
      tests.add(dynamicTest(f("subset({}, {}) on ByteArray of {}B size", 0, size, size), () -> this.testValidSubsetOfCallsWithWriteCalls(testLocalSeed, 0, size, size)));

      // Repeat trials
      for(int trial = 0; trial < TRIALS; trial++){

        // Choose byteIndex
        long byteIndex = size == 1 ? 0 : Long.max(0, rng.nextInt((int) size) - 1);

        // Choose size
        long availableSize = size - byteIndex;
        long subSize = size == 1 ? 1 : Long.min(size - byteIndex, rng.nextInt((int) availableSize) + 1);

        // Skip test if size is same
        if(subSize == size) continue;

        // Write test
        tests.add(dynamicTest(f("subset({}, {}) on ByteArray of {}B size", byteIndex, subSize, size), () -> this.testValidSubsetOfCallsWithWriteCalls(testLocalSeed, byteIndex, subSize, size)));
      }

      // Write specific test for code coverage...
      if(size > 1) {
        tests.add(dynamicTest(f("subset({}, {}) on ByteArray of {}B size", 0, size - 1, size), () -> this.testValidSubsetOfCallsWithWriteCalls(testLocalSeed, 0, size - 1, size)));
      }
    }

    // Return tests
    return tests;
  }

  private void testValidSubsetOfCallsWithWriteCalls(int testLocalSeed, long byteIndex, long subSize, long size){

    // Wrap in try and catch for possible OOM if trying to allocate max memory
    try{

      // Allocate the writeonly bytearray
      var testByteArray = createTestWriteOnlyByteArray(size);

      // Assert writeonly if applicable
      if(!isReadableWritableOK()) assertFalse(testByteArray instanceof ReadableWritableByteArray);

      try{

        // Assert size
        assertEquals(size, testByteArray.size());

        // Write random bytes to test bytearray to initialize it
        {
          Random testRNG = new Random(testLocalSeed);
          for(long index = 0; index < size; index++) testByteArray.writeByte(index, (byte) testRNG.nextInt());
        }

        // Get subset
        var subset = testByteArray.subsetOf(byteIndex, subSize);

        // Assert writeonly if applicable
        if(!isReadableWritableOK()) assertFalse(subset instanceof ReadableWritableByteArray);

        // Assert size
        assertEquals(subSize, subset.size());

        // Check if its subsetting properly
        {
          Random testRNG = new Random(testLocalSeed);
          long innerByteIndex = 0;
          for(long index = 0; index < size; index++){
            byte expected = (byte) testRNG.nextInt();
            if(index < byteIndex || index >= byteIndex + subSize) continue;
            assertEquals(
                    expected,
                    readTestByteArray(subset, innerByteIndex),
                    "Index: " + innerByteIndex
            );
            innerByteIndex++;
          }
        }

        // Write different random bytes to subset bytearray (to test if changes to subsetted byte array propagates to original bytearray)
        int diffTestLocalSeed = testLocalSeed + 233432;
        {
          Random testRNG = new Random(diffTestLocalSeed);
          for(long index = 0; index < subset.size(); index++){
            subset.writeByte(index, (byte) testRNG.nextInt());
          }
        }

        // Check by reading it
        {
          Random testRNG = new Random(testLocalSeed);
          Random altRNG = new Random(diffTestLocalSeed);
          for(long index = 0; index < size; index++){
            byte expected = (byte) testRNG.nextInt();
            if(index >= byteIndex && index < byteIndex + subSize) expected = (byte) altRNG.nextInt();
            assertEquals(expected, readTestByteArray(testByteArray, index), "Index: " + index);
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
  }
}
