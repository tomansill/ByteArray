package test.arrays;

import com.ansill.arrays.ByteArrayIndexOutOfBoundsException;
import com.ansill.arrays.ByteArrayInvalidLengthException;
import com.ansill.arrays.ByteArrayLengthOverBoundsException;
import com.ansill.arrays.IndexingUtility;
import com.ansill.arrays.ReadOnlyByteArray;
import com.ansill.arrays.ReadableWritableByteArray;
import com.ansill.arrays.WriteOnlyByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.ansill.arrays.IndexingUtility.checkReadWriteByte;

public class TestOnlyByteArray implements ReadableWritableByteArray{

  private static final Logger LOGGER = LoggerFactory.getLogger(TestOnlyByteArray.class);

  private static final int MAX_IN_MEMORY_SIZE = Integer.MAX_VALUE / 2;

  public final long start;

  public final long size;

  private final Runnable onClose;

  @Nullable
  private final RandomAccessFile raf;

  @Nullable
  private final byte[] bytes;

  public TestOnlyByteArray(long size){
    if(size == 0) throw new IllegalArgumentException();
    start = 0;
    this.size = size;
    if(size >= MAX_IN_MEMORY_SIZE) {
      this.bytes = null;
      try {
        var file = Files.createTempFile("byte-array-test-", ".bin").toFile();
        file.deleteOnExit();
        this.raf = new RandomAccessFile(file, "rw");
        raf.setLength(size);
        LOGGER.debug("Created temporary file {} of {}B", file.getAbsolutePath(), size);
        onClose = () -> {
          try {
            raf.close();
          } catch (Exception e) {
            LOGGER.warn("Failed to close RandomAccessFile", e);
          }
          try {
            if (file.delete()) {
              LOGGER.debug("Successfully deleted RandomAccessFile '{}' of {}B", file.getAbsolutePath(), size);
            } else {
              LOGGER.warn("Failed to delete RandomAccessFile '{}' of {}B", file.getAbsolutePath(), size);
            }
          } catch (Exception e) {
            LOGGER.warn("Failed to delete RandomAccessFile '{}' of {}B", file.getAbsolutePath(), size, e);
          }
        };
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }else{
      this.bytes = new byte[(int) size];
      this.raf = null;
      this.onClose = () -> {};
    }
  }

  protected TestOnlyByteArray(long start, long size, @Nonnull Runnable onClose, @Nullable RandomAccessFile raf, @Nullable byte[] bytes){
    this.start = start;
    this.size = size;
    this.raf = raf;
    this.onClose = onClose;
    this.bytes = bytes;
  }

  @Nonnull
  @Override
  public ReadOnlyByteArray toReadOnly(){
    return new ReadOnly(this.start, size, onClose, raf, bytes);
  }

  @Nonnull
  @Override
  public WriteOnlyByteArray toWriteOnly(){
    return new WriteOnly(this.start, size, onClose, raf, bytes);
  }

  @Override
  public long size(){
    return size;
  }

  @Override
  public synchronized byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, size);
    byteIndex += start;
    if(this.raf != null) {
      try {
        this.raf.seek(byteIndex);
        return this.raf.readByte();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }else if(this.bytes != null){
      return this.bytes[(int) byteIndex];
    }
    throw new RuntimeException("Unknown state");
  }

  @Override
  public synchronized void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, size);
    byteIndex += this.start;
    if(this.raf != null) {
      try {
        this.raf.seek(byteIndex);
        this.raf.writeByte(value);
        this.raf.getFD().sync(); // Expensive - should be used only for this unit test
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }else if(bytes != null){
      this.bytes[(int) byteIndex] = value;
    }
  }

  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{
    if(start == 0 && length == size) return this;
    IndexingUtility.checkSubsetOf(start, length, this.size);
    return new TestOnlyByteArray(this.start + start, length, onClose, raf, bytes);
  }

  public void clean(){
    onClose.run();
  }

  @Override
  public String toString(){

    // Size
    int size = (int) Long.min(128, this.size());

    // List of bytes as hex
    List<String> bytes = new ArrayList<>(size);

    // Go over the bytes
    for(int index = 0; index < size; index++){

      // Read
      byte value = this.readByte(index);

      // Convert to hex
      String hexValue = Long.toHexString(value & 0xffL);

      // Prefix if one char
      if(hexValue.length() == 1) hexValue = "0" + hexValue;

      // Add to list
      bytes.add(hexValue);
    }

    // If truncated, then add ellipsis
    var combined = String.join("_", bytes);
    if(size != this.size()) combined += "...";

    // Build string and return
    return TestOnlyByteArray.class.getSimpleName() +
           "(size=" +
           this.size() +
           ", content=[" +
            combined +
           "])";
  }

  public static class ReadOnly implements ReadOnlyByteArray{

    @Nonnull
    public final TestOnlyByteArray original;

    protected ReadOnly(long start, long size, @Nonnull Runnable onClose, @Nullable RandomAccessFile data, @Nullable byte[] bb){
      this.original = new TestOnlyByteArray(start, size, onClose, data, bb);
    }

    @Override
    public long size(){
      return original.size();
    }

    @Override
    public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
      return original.readByte(byteIndex);
    }

    @Nonnull
    @Override
    public ReadOnlyByteArray subsetOf(long start, long length)
    throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{

      // Return self
      if(start == 0 && length == this.size()) return this;

      // Check
      IndexingUtility.checkSubsetOf(start, length, original.size());

      // Modify and return
      return new ReadOnly(original.start + start, length, original.onClose, original.raf, original.bytes);
    }
  }

  public static class WriteOnly extends TestOnlyByteArray implements WriteOnlyByteArray{

    protected WriteOnly(long start, long size, @Nonnull Runnable onClose, @Nullable RandomAccessFile data, @Nullable byte[] bb){
      super(start, size, onClose, data, bb);
    }
  }
}
