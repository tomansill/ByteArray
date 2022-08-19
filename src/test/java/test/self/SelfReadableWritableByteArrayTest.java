package test.self;

import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseReadableWritableByteArrayTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public interface SelfReadableWritableByteArrayTest
  extends BaseReadableWritableByteArrayTest, SelfReadOnlyByteArrayTest, SelfWriteOnlyByteArrayTest{

  @DisplayName("Test toString()")
  @Test
  default void testToString(){

    // Simple toString test
    WriteOnlyByteArray testByteArray = createTestWriteOnlyByteArray(1);

    // ToString it
    assertNotNull(testByteArray.toString());

  }
}
