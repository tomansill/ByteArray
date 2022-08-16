package test;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class UtilityTest{

  @DisplayName("Test Wrapping for Primitive Byte Arrays")
  @Test
  void testWrapPrimitiveArray(){

    // Create arrays
    byte[] array = new byte[7];

    // Wrap it
    ReadableWritableByteArray ba = ByteArray.wrap(array);

    // Check class
    assertEquals("PrimitiveByteArray", ba.getClass().getSimpleName());

    // Check Size
    assertEquals(7, ba.size());

    // Check ba, should be all zero
    for(long i = 0; i < ba.size(); i++) assertEquals(0, ba.readByte(i));

    // Modify some arrays
    array[1] = 20;
    array[2] = 33;
    array[6] = 123;

    // Check again
    assertEquals(0, ba.readByte(0));
    assertEquals(array[1], ba.readByte(1));
    assertEquals(array[2], ba.readByte(2));
    assertEquals(0, ba.readByte(3));
    assertEquals(0, ba.readByte(4));
    assertEquals(0, ba.readByte(5));
    assertEquals(array[6], ba.readByte(6));

    // Modify byte array
    ba.writeByte(0, (byte) 22);
    ba.writeByte(1, (byte) 17);
    ba.writeByte(3, (byte) -23);
    ba.writeByte(5, (byte) 2);

    // Check arrays
    assertEquals(22, array[0]);
    assertEquals(17, array[1]);
    assertEquals(33, array[2]);
    assertEquals(-23, array[3]);
    assertEquals(0, array[4]);
    assertEquals(2, array[5]);
    assertEquals(123, array[6]);
  }

  @DisplayName("Test Wrapping for Multiple Primitive Byte Arrays")
  @Test
  void testWrapMultiplePrimitiveArray(){

    // Create arrays
    byte[] array1 = new byte[2];
    byte[] array2 = new byte[3];
    byte[] array3 = new byte[2];

    // Wrap it
    ReadableWritableByteArray ba = ByteArray.wrap(array1, array2, array3);

    // Check class
    assertEquals("ReadableWritableMultipleByteArray", ba.getClass().getSimpleName());

    // Check Size
    assertEquals(7, ba.size());

    // Check ba, should be all zero
    for(long i = 0; i < ba.size(); i++) assertEquals(0, ba.readByte(i));

    // Modify some arrays
    array1[1] = 20;
    array2[0] = 33;
    array3[1] = 123;

    // Check again
    assertEquals(0, ba.readByte(0));
    assertEquals(array1[1], ba.readByte(1));
    assertEquals(array2[0], ba.readByte(2));
    assertEquals(0, ba.readByte(3));
    assertEquals(0, ba.readByte(4));
    assertEquals(0, ba.readByte(5));
    assertEquals(array3[1], ba.readByte(6));

    // Modify byte array
    ba.writeByte(0, (byte) 22);
    ba.writeByte(1, (byte) 17);
    ba.writeByte(3, (byte) -23);
    ba.writeByte(5, (byte) 2);

    // Check arrays
    assertEquals(22, array1[0]);
    assertEquals(17, array1[1]);
    assertEquals(33, array2[0]);
    assertEquals(-23, array2[1]);
    assertEquals(0, array2[2]);
    assertEquals(2, array3[0]);
    assertEquals(123, array3[1]);
  }

  @DisplayName("Test Wrapping for ByteBuffer")
  @Test
  void testWrapByteBuffer(){

    // Create arrays
    ByteBuffer bb = ByteBuffer.allocate(7);

    // Wrap it
    ReadableWritableByteArray ba = ByteArray.wrap(bb);

    // Check class
    assertEquals("ByteBufferByteArray", ba.getClass().getSimpleName());

    // Check Size
    assertEquals(7, ba.size());

    // Check ba, should be all zero
    for(long i = 0; i < ba.size(); i++) assertEquals(0, ba.readByte(i));

    // Modify some arrays
    bb.put(1, (byte) 20);
    bb.put(2, (byte) 33);
    bb.put(6, (byte) 123);

    // Check again
    assertEquals(0, ba.readByte(0));
    assertEquals(bb.get(1), ba.readByte(1));
    assertEquals(bb.get(2), ba.readByte(2));
    assertEquals(0, ba.readByte(3));
    assertEquals(0, ba.readByte(4));
    assertEquals(0, ba.readByte(5));
    assertEquals(bb.get(6), ba.readByte(6));

    // Modify byte array
    ba.writeByte(0, (byte) 22);
    ba.writeByte(1, (byte) 17);
    ba.writeByte(3, (byte) -23);
    ba.writeByte(5, (byte) 2);

    // Check arrays
    assertEquals(22, bb.get(0));
    assertEquals(17, bb.get(1));
    assertEquals(33, bb.get(2));
    assertEquals(-23, bb.get(3));
    assertEquals(0, bb.get(4));
    assertEquals(2, bb.get(5));
    assertEquals(123, bb.get(6));
  }


  @DisplayName("Test Wrapping for Multiple ByteBuffers")
  @Test
  void testWrapMultipleByteBuffer(){

    // Create arrays
    ByteBuffer bb1 = ByteBuffer.allocate(2);
    ByteBuffer bb2 = ByteBuffer.allocate(3);
    ByteBuffer bb3 = ByteBuffer.allocate(2);

    // Wrap it
    ReadableWritableByteArray ba = ByteArray.wrap(bb1, bb2, bb3);

    // Check class
    assertEquals("ReadableWritableMultipleByteArray", ba.getClass().getSimpleName());

    // Check Size
    assertEquals(7, ba.size());

    // Check ba, should be all zero
    for(long i = 0; i < ba.size(); i++) assertEquals(0, ba.readByte(i));

    // Modify some arrays
    bb1.put(1, (byte) 20);
    bb2.put(0, (byte) 33);
    bb3.put(1, (byte) 123);

    // Check again
    assertEquals(0, ba.readByte(0));
    assertEquals(bb1.get(1), ba.readByte(1));
    assertEquals(bb2.get(0), ba.readByte(2));
    assertEquals(0, ba.readByte(3));
    assertEquals(0, ba.readByte(4));
    assertEquals(0, ba.readByte(5));
    assertEquals(bb3.get(1), ba.readByte(6));

    // Modify byte array
    ba.writeByte(0, (byte) 22);
    ba.writeByte(1, (byte) 17);
    ba.writeByte(3, (byte) -23);
    ba.writeByte(5, (byte) 2);

    // Check arrays
    assertEquals(22, bb1.get(0));
    assertEquals(17, bb1.get(1));
    assertEquals(33, bb2.get(0));
    assertEquals(-23, bb2.get(1));
    assertEquals(0, bb2.get(2));
    assertEquals(2, bb3.get(0));
    assertEquals(123, bb3.get(1));
  }

  @DisplayName("Test Combining ByteArrays using Variadic method")
  @Test
  void testCombineVariadic(){

    // Create arrays
    ReadableWritableByteArray ba1 = ByteArray.wrap(ByteBuffer.allocate(2));
    ReadableWritableByteArray ba2 = ByteArray.wrap(ByteBuffer.allocate(3));
    ReadableWritableByteArray ba3 = ByteArray.wrap(ByteBuffer.allocate(2));

    // Wrap it
    ReadableWritableByteArray ba = ByteArray.combine(ba1, ba2, ba3);

    // Check class
    assertEquals("ReadableWritableMultipleByteArray", ba.getClass().getSimpleName());

    // Check Size
    assertEquals(7, ba.size());

    // Check ba, should be all zero
    for(long i = 0; i < ba.size(); i++) assertEquals(0, ba.readByte(i));

    // Modify some arrays
    ba1.writeByte(1, (byte) 20);
    ba2.writeByte(0, (byte) 33);
    ba3.writeByte(1, (byte) 123);

    // Check again
    assertEquals(0, ba.readByte(0));
    assertEquals(ba1.readByte(1), ba.readByte(1));
    assertEquals(ba2.readByte(0), ba.readByte(2));
    assertEquals(0, ba.readByte(3));
    assertEquals(0, ba.readByte(4));
    assertEquals(0, ba.readByte(5));
    assertEquals(ba3.readByte(1), ba.readByte(6));

    // Modify byte array
    ba.writeByte(0, (byte) 22);
    ba.writeByte(1, (byte) 17);
    ba.writeByte(3, (byte) -23);
    ba.writeByte(5, (byte) 2);

    // Check arrays
    assertEquals(22, ba1.readByte(0));
    assertEquals(17, ba1.readByte(1));
    assertEquals(33, ba2.readByte(0));
    assertEquals(-23, ba2.readByte(1));
    assertEquals(0, ba2.readByte(2));
    assertEquals(2, ba3.readByte(0));
    assertEquals(123, ba3.readByte(1));
  }

  @DisplayName("Test Combining Readonly ByteArrays using Variadic method")
  @Test
  void testCombineReadOnlyVariadic(){

    // Create arrays
    ReadableWritableByteArray ba1 = ByteArray.wrap(ByteBuffer.allocate(2));
    ReadableWritableByteArray ba2 = ByteArray.wrap(ByteBuffer.allocate(3));
    ReadableWritableByteArray ba3 = ByteArray.wrap(ByteBuffer.allocate(2));

    // Wrap it
    ReadOnlyByteArray ba = ByteArray.combineReadOnly(ba1.toReadOnly(), ba2, ba3.toReadOnly());

    // Check class
    assertEquals("ReadOnlyMultipleByteArray", ba.getClass().getSimpleName());

    // Check Size
    assertEquals(7, ba.size());

    // Check ba, should be all zero
    for(long i = 0; i < ba.size(); i++) assertEquals(0, ba.readByte(i));

    // Modify some arrays
    ba1.writeByte(1, (byte) 20);
    ba2.writeByte(0, (byte) 33);
    ba3.writeByte(1, (byte) 123);

    // Check again
    assertEquals(0, ba.readByte(0));
    assertEquals(ba1.readByte(1), ba.readByte(1));
    assertEquals(ba2.readByte(0), ba.readByte(2));
    assertEquals(0, ba.readByte(3));
    assertEquals(0, ba.readByte(4));
    assertEquals(0, ba.readByte(5));
    assertEquals(ba3.readByte(1), ba.readByte(6));
  }

  @DisplayName("Test Combining ByteArrays using List method")
  @Test
  void testCombineList(){

    // Create arrays
    ReadableWritableByteArray ba1 = ByteArray.wrap(ByteBuffer.allocate(2));
    ReadableWritableByteArray ba2 = ByteArray.wrap(ByteBuffer.allocate(3));
    ReadableWritableByteArray ba3 = ByteArray.wrap(ByteBuffer.allocate(2));

    // Wrap it
    ReadableWritableByteArray ba = ByteArray.combine(Arrays.asList(ba1, ba2, ba3));

    // Check class
    assertEquals("ReadableWritableMultipleByteArray", ba.getClass().getSimpleName());

    // Check Size
    assertEquals(7, ba.size());

    // Check ba, should be all zero
    for(long i = 0; i < ba.size(); i++) assertEquals(0, ba.readByte(i));

    // Modify some arrays
    ba1.writeByte(1, (byte) 20);
    ba2.writeByte(0, (byte) 33);
    ba3.writeByte(1, (byte) 123);

    // Check again
    assertEquals(0, ba.readByte(0));
    assertEquals(ba1.readByte(1), ba.readByte(1));
    assertEquals(ba2.readByte(0), ba.readByte(2));
    assertEquals(0, ba.readByte(3));
    assertEquals(0, ba.readByte(4));
    assertEquals(0, ba.readByte(5));
    assertEquals(ba3.readByte(1), ba.readByte(6));

    // Modify byte array
    ba.writeByte(0, (byte) 22);
    ba.writeByte(1, (byte) 17);
    ba.writeByte(3, (byte) -23);
    ba.writeByte(5, (byte) 2);

    // Check arrays
    assertEquals(22, ba1.readByte(0));
    assertEquals(17, ba1.readByte(1));
    assertEquals(33, ba2.readByte(0));
    assertEquals(-23, ba2.readByte(1));
    assertEquals(0, ba2.readByte(2));
    assertEquals(2, ba3.readByte(0));
    assertEquals(123, ba3.readByte(1));
  }

  @DisplayName("Test Combining Readonly ByteArrays using List method")
  @Test
  void testCombineReadOnlyList(){

    // Create arrays
    ReadableWritableByteArray ba1 = ByteArray.wrap(ByteBuffer.allocate(2));
    ReadableWritableByteArray ba2 = ByteArray.wrap(ByteBuffer.allocate(3));
    ReadableWritableByteArray ba3 = ByteArray.wrap(ByteBuffer.allocate(2));

    // Wrap it
    ReadOnlyByteArray ba = ByteArray.combineReadOnly(Arrays.asList(ba1, ba2.toReadOnly(), ba3.toReadOnly()));

    // Check class
    assertEquals("ReadOnlyMultipleByteArray", ba.getClass().getSimpleName());

    // Check Size
    assertEquals(7, ba.size());

    // Check ba, should be all zero
    for(long i = 0; i < ba.size(); i++) assertEquals(0, ba.readByte(i));

    // Modify some arrays
    ba1.writeByte(1, (byte) 20);
    ba2.writeByte(0, (byte) 33);
    ba3.writeByte(1, (byte) 123);

    // Check again
    assertEquals(0, ba.readByte(0));
    assertEquals(ba1.readByte(1), ba.readByte(1));
    assertEquals(ba2.readByte(0), ba.readByte(2));
    assertEquals(0, ba.readByte(3));
    assertEquals(0, ba.readByte(4));
    assertEquals(0, ba.readByte(5));
    assertEquals(ba3.readByte(1), ba.readByte(6));
  }

  @DisplayName("Test invalid wrap calls")
  @SuppressWarnings("ConstantConditions")
  @TestFactory
  Iterable<DynamicTest> testInvalidWrap(){

    // Tests container
    List<DynamicTest> tests = new LinkedList<>();

    // Create tests
    tests.add(dynamicTest("null primitive byte array", () -> {
      IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> ByteArray.wrap((byte[]) null));
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("null bytebuffer", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap((ByteBuffer) null)
      );
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("null primitive byte arrays", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(null, (byte[]) null)
      );
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("null bytebuffers", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(null, (ByteBuffer) null)
      );
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("some null primitive byte arrays variant 1", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(new byte[2], null, null)
      );
      assertEquals("null elements in rest array", iae.getMessage());
    }));
    tests.add(dynamicTest("some null primitive byte arrays variant 2", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(null, new byte[2])
      );
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("some null primitive byte arrays variant 3", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(new byte[2], (byte[][]) null)
      );
      assertEquals("rest array is null", iae.getMessage());
    }));
    tests.add(dynamicTest("some null bytebuffers variant 1", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(ByteBuffer.allocate(2), null, null)
      );
      assertEquals("null elements in rest array", iae.getMessage());
    }));
    tests.add(dynamicTest("some null bytebuffers variant 2", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(null, ByteBuffer.allocate(2))
      );
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("some null bytebuffers variant 3", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(ByteBuffer.allocate(2), (ByteBuffer[]) null)
      );
      assertEquals("rest array is null", iae.getMessage());
    }));
    tests.add(dynamicTest("readonly bytebuffer", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(ByteBuffer.allocate(2).asReadOnlyBuffer())
      );
      assertEquals("ReadOnly ByteBuffer was passed in", iae.getMessage());
    }));
    tests.add(dynamicTest("readonly bytebuffers variant 1", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(ByteBuffer.allocate(2).asReadOnlyBuffer(), ByteBuffer.allocate(2))
      );
      assertEquals("ReadOnly ByteBuffer was passed in", iae.getMessage());
    }));
    tests.add(dynamicTest("readonly bytebuffers variant 2", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.wrap(ByteBuffer.allocate(2), ByteBuffer.allocate(2).asReadOnlyBuffer())
      );
      assertEquals("ReadOnly ByteBuffer was passed in", iae.getMessage());
    }));

    // Return tests
    return tests;
  }

  @DisplayName("Test invalid combine calls")
  @SuppressWarnings("ConstantConditions")
  @TestFactory
  Iterable<DynamicTest> testInvalidCombine(){

    // Tests container
    List<DynamicTest> tests = new LinkedList<>();

    // Create tests
    tests.add(dynamicTest("null byte array (list)", () -> {
      IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> ByteArray.combine(null));
      assertEquals("ByteArrays list is null", iae.getMessage());
    }));
    tests.add(dynamicTest("null byte arrays (variadic)", () -> {
      IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> ByteArray.combine(null, null));
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("null readonly byte array (list)", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combineReadOnly(null)
      );
      assertEquals("ByteArrays list is null", iae.getMessage());
    }));
    tests.add(dynamicTest("null readonly byte arrays (variadic)", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combineReadOnly(null, null)
      );
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("empty byte array list", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combine(Collections.emptyList())
      );
      assertEquals("ByteArrays list is empty", iae.getMessage());
    }));
    tests.add(dynamicTest("empty readonly byte array list", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combineReadOnly(Collections.emptyList())
      );
      assertEquals("ByteArrays list is empty", iae.getMessage());
    }));
    tests.add(dynamicTest("byte array list with some nulls variant 1", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combine(Arrays.asList(ByteArray.wrap(new byte[1]), null))
      );
      assertEquals("There is a null element in the ByteArray list", iae.getMessage());
    }));
    tests.add(dynamicTest("byte array list with some nulls variant 2", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combine(Arrays.asList(null, ByteArray.wrap(new byte[1])))
      );
      assertEquals("There is a null element in the ByteArray list", iae.getMessage());
    }));
    tests.add(dynamicTest("readonly byte array list with some nulls variant 1", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combineReadOnly(Arrays.asList(ByteArray.wrap(new byte[1]), null))
      );
      assertEquals("There is a null element in the ByteArray list", iae.getMessage());
    }));
    tests.add(dynamicTest("readonly byte array list with some nulls variant 2", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combineReadOnly(Arrays.asList(null, ByteArray.wrap(new byte[1])))
      );
      assertEquals("There is a null element in the ByteArray list", iae.getMessage());
    }));
    tests.add(dynamicTest("byte array variadic with some nulls variant 1", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combine(ByteArray.wrap(new byte[1]), null, ByteArray.wrap(new byte[1]))
      );
      assertEquals("second element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("byte array variadic with some nulls variant 2", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combine(null, ByteArray.wrap(new byte[1]), ByteArray.wrap(new byte[1]))
      );
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("byte array variadic with some nulls variant 3", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combine(ByteArray.wrap(new byte[1]),
          ByteArray.wrap(new byte[1]),
          null,
          ByteArray.wrap(new byte[1])
        )
      );
      assertEquals("null elements in rest array", iae.getMessage());
    }));
    tests.add(dynamicTest("byte array variadic with some nulls variant 4", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combine(ByteArray.wrap(new byte[1]),
          ByteArray.wrap(new byte[1]),
          (ReadableWritableByteArray[]) null
        )
      );
      assertEquals("rest array is null", iae.getMessage());
    }));
    tests.add(dynamicTest("readonly byte array variadic with some nulls variant 1", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combineReadOnly(ByteArray.wrap(new byte[1]).toReadOnly(), null, ByteArray.wrap(new byte[1]))
      );
      assertEquals("second element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("readonly byte array variadic with some nulls variant 2", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combineReadOnly(null, ByteArray.wrap(new byte[1]), ByteArray.wrap(new byte[1]).toReadOnly())
      );
      assertEquals("first element is null", iae.getMessage());
    }));
    tests.add(dynamicTest("readonly byte array variadic with some nulls variant 3", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combineReadOnly(ByteArray.wrap(new byte[1]),
          ByteArray.wrap(new byte[1]).toReadOnly(),
          null,
          ByteArray.wrap(new byte[1])
        )
      );
      assertEquals("null elements in rest array", iae.getMessage());
    }));
    tests.add(dynamicTest("readonly byte array variadic with some nulls variant 4", () -> {
      IllegalArgumentException iae = assertThrows(
        IllegalArgumentException.class,
        () -> ByteArray.combineReadOnly(ByteArray.wrap(new byte[1]).toReadOnly(),
          ByteArray.wrap(new byte[1]),
          (ReadableWritableByteArray[]) null
        )
      );
      assertEquals("rest array is null", iae.getMessage());
    }));

    // Return tests
    return tests;
  }

}
