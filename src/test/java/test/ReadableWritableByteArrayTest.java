package test;

import com.ansill.arrays.ByteArray;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.WriteOnlyByteArray;

import javax.annotation.Nonnull;

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
}
