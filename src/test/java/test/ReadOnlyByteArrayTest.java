package test;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ReadOnlyByteArray;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface ReadOnlyByteArrayTest extends ByteArrayTest{


  @Nonnull
  @Override
  default ByteArray createTestByteArray(long size){
    return createTestReadOnlyByteArray(size);
  }

  @Nonnull
  ReadOnlyByteArray createTestReadOnlyByteArray(@Nonnegative long size);

  void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, @Nonnegative long byteIndex, byte value);
}
