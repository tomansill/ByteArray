package test.self;

import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseReadableWritableByteArrayTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public interface SelfReadableWritableByteArray64BitTest extends
  BaseReadableWritableByteArrayTest, SelfWriteOnlyByteArray64BitTest, SelfReadOnlyByteArray64BitTest{

  @Override
  @DisplayName("Test toString()")
  @Test
  default void testToString(){

    // Simple toString test
    WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(1);

    // ToString it
    assertNotNull(testByteArray.toString());

  }
}
