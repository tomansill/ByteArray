package com.ansill.arrays;

import test.ByteArrayTest;
import test.ReadOnlyByteArrayTest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static java.nio.file.StandardOpenOption.*;

public interface ChannelByteArrayTest extends ByteArrayTest{

  @Override
  default boolean is64BitAddressingSupported(){
    return true;
  }

  @Override
  default void cleanTestByteArray(@Nonnull ByteArray byteArray){

    // Unwrap if readonly or writeonly
    if(byteArray instanceof ReadOnlyByteArrayWrapper) byteArray = ((ReadOnlyByteArrayWrapper) byteArray).original;
    if(byteArray instanceof WriteOnlyByteArrayWrapper) byteArray = ((WriteOnlyByteArrayWrapper) byteArray).original;

    // Check if channel
    if(!(byteArray instanceof ChannelByteArray)) throw new RuntimeException("Unsupported class");

    // Cast
    ChannelByteArray channelByteArray = (ChannelByteArray) byteArray;

    // Get channel and truncate it
    try{
      channelByteArray.channel.truncate(0);
      if(channelByteArray.channel instanceof FileChannel){
        ((FileChannel) channelByteArray.channel).force(true);
      }
      channelByteArray.channel.close();
    }catch(IOException e){
      throw new RuntimeException(e);
    }

  }


  class ReadOnlyWithControlByteArrayTest implements ReadOnlyByteArrayTest, ChannelByteArrayTest{

    @Nonnull
    @Override
    public ReadOnlyByteArray createTestReadOnlyByteArray(long size){

      // TODO figure out tempdir
      Path filePath = Paths.get("/tmp/testFile_" + (Math.random() + "").replace(".", "") + ".tmp");

      try{
        File file = filePath.toFile();
        file.createNewFile();
        System.out.println(file.getCanonicalPath());
      }catch(IOException e){
        throw new RuntimeException(e);
      }

      try{

        // Open filechannel that should delete on exit
        FileChannel fileChannel = FileChannel.open(
          filePath,
          DELETE_ON_CLOSE,
          CREATE,
          READ,
          WRITE,
          SYNC
        );

        // Allocate with empty bytes
        long sizeWritten = 0;
        byte[] bytearray = new byte[4_000_000];
        ByteBuffer buffer = ByteBuffer.wrap(bytearray);
        Random random = new Random(SEED.hashCode());
        while(sizeWritten < size){

          // Figure out bytes to write
          int bytesToWrite = (int) Long.min(buffer.capacity(), size - sizeWritten);

          // Resize if needed
          buffer.limit(bytesToWrite);

          // Rand bytes
          random.nextBytes(bytearray);

          // Write
          fileChannel.write(buffer);

          // Clear
          buffer.clear();

          // Update size
          sizeWritten += bytesToWrite;
        }

        // Reposition
        fileChannel.position(0);
        fileChannel.force(true);

        // Return it
        return new ChannelByteArray(fileChannel).toReadOnly();

      }catch(IOException e){
        throw new RuntimeException(e);
      }
    }

    @Override
    public void writeTestReadOnlyByteArray(@Nonnull ReadOnlyByteArray testByteArray, long byteIndex, byte value){

      // Unwrap if wrapper
      if(testByteArray instanceof ReadOnlyByteArrayWrapper)
        testByteArray = ((ReadOnlyByteArrayWrapper) testByteArray).original;

      // Check if channelbytearray
      if(!(testByteArray instanceof ChannelByteArray)) throw new IllegalArgumentException("Cannot edit");

      // Edit
      try{
        ((ChannelByteArray) testByteArray).writeByte(byteIndex, value);
      }catch(ByteArrayIndexOutOfBoundsException e){
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isReadableWritableOK(){
      return false;
    }
  }
}
