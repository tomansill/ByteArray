package test;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.WriteOnlyByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public interface ReadableWritableByteArrayTest extends ReadOnlyByteArrayTest, WriteOnlyByteArrayTest{

  @Nonnull
  ReadableWritableByteArray createTestReadableWritableByteArray(long size);

  @Nonnull
  @Override
  default ByteArray createTestByteArray(long size){
    return createTestReadableWritableByteArray(size);
  }

  @Nonnull
  @Override
  default ReadOnlyByteArray createTestReadOnlyByteArray(long size){
    return createTestReadableWritableByteArray(size);
  }

  @Nonnull
  @Override
  default WriteOnlyByteArray createTestWriteOnlyByteArray(long size){
    return createTestReadableWritableByteArray(size);
  }

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
