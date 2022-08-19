package test;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.WriteOnlyByteArray;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface BaseWriteOnlyByteArrayTest extends BaseByteArrayTest{

  @Nonnull
  WriteOnlyByteArray createTestWriteOnlyByteArray(@Nonnegative long size);

  byte readTestWriteOnlyByteArray(@Nonnull WriteOnlyByteArray testByteArray, @Nonnegative long byteIndex);

  @Nonnull
  @Override
  default ByteArray createTestByteArray(long size){
    return createTestWriteOnlyByteArray(size);
  }

}
