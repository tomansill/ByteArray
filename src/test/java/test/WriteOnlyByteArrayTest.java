package test;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.WriteOnlyByteArray;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface WriteOnlyByteArrayTest extends ByteArrayTest{

  @Nonnull
  WriteOnlyByteArray createTestWriteOnlyByteArray(@Nonnegative long size);

  byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, @Nonnegative long byteIndex);

  @Nonnull
  @Override
  default ByteArray createTestByteArray(long size){
    return createTestWriteOnlyByteArray(size);
  }

  // TODO add test to test valid subsetOf calls
}
