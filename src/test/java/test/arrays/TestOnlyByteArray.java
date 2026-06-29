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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.ansill.arrays.IndexingUtility.checkReadWriteByte;

public class TestOnlyByteArray implements ReadableWritableByteArray{

  private static final Logger LOGGER = LoggerFactory.getLogger(TestOnlyByteArray.class);

  public final long start;

  public final long size;

  public final Runnable onClose;

  public final RandomAccessFile data;

  public TestOnlyByteArray(long size){
    if(size == 0) throw new IllegalArgumentException();
    start = 0;
    this.size = size;
	  try {
      var file = Files.createTempFile("byte-array-test",".bin").toFile();
      file.deleteOnExit();
      this.data = new RandomAccessFile(file, "rw");
      data.setLength(size);
      onClose = () -> {
        try{
          data.close();
        }catch (Exception e){
          LOGGER.warn("Failed to close RandomAccessFile", e);
        }
        try{
          if(file.delete()){
            LOGGER.debug("Successfully deleted RandomAccessFile {}", file.getAbsolutePath());
          }else{
            LOGGER.warn("Failed to delete RandomAccessFile {}", file.getAbsolutePath());
          }
        }catch (Exception e){
          LOGGER.warn("Failed to delete RandomAccessFile '{}'", file.getAbsolutePath(), e);
        }
      };
	  } catch (IOException e) {
		  throw new RuntimeException(e);
	  }
  }

  protected TestOnlyByteArray(long start, long size, @Nonnull Runnable onClose, @Nonnull RandomAccessFile data){
    this.start = start;
    this.size = size;
    this.data = data;
    this.onClose = onClose;
  }

  @Nonnull
  @Override
  public ReadOnlyByteArray toReadOnly(){
    return new ReadOnly(this.start, size, onClose, data);
  }

  @Nonnull
  @Override
  public WriteOnlyByteArray toWriteOnly(){
    return new WriteOnly(this.start, size, onClose, data);
  }

  @Override
  public long size(){
    return size;
  }

  @Override
  public synchronized byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, size);
    byteIndex += start;
	  try {
      this.data.seek(byteIndex);
      return this.data.readByte();
	  } catch (IOException e) {
		  throw new RuntimeException(e);
	  }
  }

  @Override
  public synchronized void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{
    checkReadWriteByte(byteIndex, size);
    byteIndex += this.start;
    try {
      this.data.seek(byteIndex);
      this.data.writeByte(value);
      this.data.getFD().sync(); // Expensive - should be used only for this unit test
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{
    if(start == 0 && length == size) return this;
    IndexingUtility.checkSubsetOf(start, length, this.size);
    return new TestOnlyByteArray(this.start + start, length, onClose, data);
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

    protected ReadOnly(long start, long size, @Nonnull Runnable onClose, @Nonnull RandomAccessFile data){
      this.original = new TestOnlyByteArray(start, size, onClose, data);
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
      return new ReadOnly(original.start + start, length, original.onClose, original.data);
    }
  }

  public static class WriteOnly extends TestOnlyByteArray implements WriteOnlyByteArray{

    protected WriteOnly(long start, long size, @Nonnull Runnable onClose, @Nonnull RandomAccessFile data){
      super(start, size, onClose, data);
    }
  }
}
