package test.arrays;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.TestUtility;
import com.ansill.arrays.WriteOnlyByteArray;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.BaseByteArrayTest;
import test.BaseReadableWritableByteArrayTest;
import test.other.ReadOnlyByteArrayWithOtherByteArrayTest;
import test.other.WriteOnlyByteArrayWithOtherByteArrayTest;
import test.self.SelfByteArrayTest;
import test.self.SelfReadOnlyByteArray64BitTest;
import test.self.SelfReadableWritableByteArray64BitTest;
import test.self.SelfWriteOnlyByteArray64BitTest;

import javax.annotation.Nonnull;

/** Serves as a sanity check and ensure this test is working as expected */
@DisplayName("TestOnlyByteArray Test Suite - (sanity check)")
public class TestOnlyByteArrayNestedTest{

  private static final Logger LOGGER = LoggerFactory.getLogger(TestOnlyByteArrayNestedTest.class);

  public abstract static class TestOnlyByteArrayTest implements BaseByteArrayTest{

    @Override
    public @NonNull String getExpectedToStringClassName() {
      return "TestOnlyByteArray";
    }

    @Override
    public void cleanTestByteArray(@Nonnull ByteArray byteArray){

      // Save size
      long size = byteArray.size();

      // Do a recursive clean
      TestUtility.clean(byteArray);

      // Trigger a GC to blast away any unused stuff
      System.gc();

      // Log it if actually big
      if(size >= (Integer.MAX_VALUE * 0.5)) LOGGER.debug("Cleared away {}B", size);
    }

    @Override
    public byte readTestByteArray(@NonNull ByteArray testByteArray, long byteIndex) {

      // Check if TestByteArray
      if(!(testByteArray instanceof TestOnlyByteArray)) throw new RuntimeException();

      // Write
      try{
        return ((TestOnlyByteArray) testByteArray).readByte(byteIndex);
      }catch(ByteArrayIndexOutOfBoundsException e){
        throw new RuntimeException(e);
      }
    }

    @Override
    public void writeTestByteArray(@Nonnull ByteArray testByteArray, long byteIndex, byte value){

      // Check if TestByteArray
      if(!(testByteArray instanceof TestOnlyByteArray)) throw new RuntimeException();

      // Write
      try{
        ((TestOnlyByteArray) testByteArray).writeByte(byteIndex, value);
      }catch(ByteArrayIndexOutOfBoundsException e){
        throw new RuntimeException(e);
      }
    }
  }

  @Nested
  @DisplayName("TestOnlyByteArray ReadOnly test on self")
  public class ReadOnlyTestOnlyByteArrayTest extends TestOnlyByteArrayTest
    implements SelfReadOnlyByteArray64BitTest, ReadOnlyByteArrayWithOtherByteArrayTest{

    @Override
    @TestFactory
    @DisplayName("Test toString()")
    public Iterable<DynamicTest> testToString() {
      return SelfByteArrayTest.generateTestsToString(
              getRNG(),
              "ReadOnlyByteArray",
              getToStringPerformanceLimit(),
              getExpectedToStringClassName(),
              this::createTestReadOnlyByteArray,
              this::writeTestByteArray,
              this::readTestByteArray,
              this::cleanTestByteArray,
              this.isReadableWritableOK()
      );
    }

    @Override
    public boolean isReadableWritableOK(){
      return true;
    }

    @Override
    public @NonNull ReadOnlyByteArray createTestReadOnlyByteArray(long size) {
      return new TestOnlyByteArray(size);
    }
  }

  @Nested
  @DisplayName("TestOnlyByteArray WriteOnly test on self")
  public class WriteOnlyTestOnlyByteArrayTest extends TestOnlyByteArrayTest
    implements SelfWriteOnlyByteArray64BitTest, WriteOnlyByteArrayWithOtherByteArrayTest{

    @Override
    public boolean isReadableWritableOK(){
      return true;
    }

    @Nonnull
    @Override
    public WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
      return new TestOnlyByteArray(size).toWriteOnly();
    }
    @Override
    @TestFactory
    @DisplayName("Test toString()")
    public Iterable<DynamicTest> testToString() {
      return SelfByteArrayTest.generateTestsToString(
              getRNG(),
              "WriteOnlyByteArray",
              getToStringPerformanceLimit(),
              getExpectedToStringClassName(),
              this::createTestWriteOnlyByteArray,
              this::writeTestByteArray,
              this::readTestByteArray,
              this::cleanTestByteArray,
              this.isReadableWritableOK()
      );
    }
  }

  @Nested
  @DisplayName("TestOnlyByteArray ReadableWritable test on self")
  public class ReadableWritableByteArrayTest extends TestOnlyByteArrayTest
    implements BaseReadableWritableByteArrayTest, SelfReadableWritableByteArray64BitTest{

    @Override
    public boolean isReadableWritableOK(){
      return true;
    }
    @Override
    @TestFactory
    @DisplayName("Test toString()")
    public Iterable<DynamicTest> testToString() {
      return SelfByteArrayTest.generateTestsToString(
              getRNG(),
              "ReadableWritableByteArray",
              getToStringPerformanceLimit(),
              getExpectedToStringClassName(),
              this::createTestReadableWritableByteArray,
              this::writeTestByteArray,
              this::readTestByteArray,
              this::cleanTestByteArray,
              this.isReadableWritableOK()
      );
    }

    @Nonnull
    @Override
    public ReadableWritableByteArray createTestReadableWritableByteArray(long size){
      return new TestOnlyByteArray(size);
    }

    }
}
