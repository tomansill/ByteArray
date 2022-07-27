package com.ansill.arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/** Channel implementation for ByteArray */
class ChannelByteArray implements ReadableWritableByteArray{

  @Nonnull
  final SeekableByteChannel channel;

  @Nonnegative
  private final long start;

  @Nonnegative
  private final long size;

  @Nonnull
  private final ByteBuffer reusableSingleByteBuffer = ByteBuffer.allocateDirect(1);

  ChannelByteArray(@Nonnull SeekableByteChannel channel) throws IOException{
    this.channel = channel;
    this.start = 0;
    this.size = channel.size();
  }

  private ChannelByteArray(@Nonnull SeekableByteChannel channel, @Nonnegative long start, @Nonnegative long size){
    this.channel = channel;
    this.start = start;
    this.size = size;
  }

  @Override
  public long size(){
    return size;
  }

  @Override
  public byte readByte(long byteIndex) throws ByteArrayIndexOutOfBoundsException{

    // Check parameter
    IndexingUtility.checkReadWriteByte(byteIndex, size);

    // Synchronize on singlebytebuffer
    synchronized(reusableSingleByteBuffer){

      // Synchronize on channel
      synchronized(channel){
        try{

          // Position the channel
          channel.position(byteIndex + start);

          // Read
          channel.read(reusableSingleByteBuffer);

        }catch(IOException ioe){
          throw new RuntimeException(ioe); // TODO better exception
        }
      }

      // Flip bytebuffer
      reusableSingleByteBuffer.flip();

      // Read byte
      byte value = reusableSingleByteBuffer.get();

      // Flip it
      reusableSingleByteBuffer.clear();

      // Return value
      return value;
    }
  }

  @Override
  public void writeByte(long byteIndex, byte value) throws ByteArrayIndexOutOfBoundsException{

    // Check parameter
    IndexingUtility.checkReadWriteByte(byteIndex, size);

    // Synchronize on singlebytebuffer
    synchronized(reusableSingleByteBuffer){

      // Prepare bytebuffer
      reusableSingleByteBuffer.put(value);

      // Flip it
      reusableSingleByteBuffer.flip();

      // Synchronize on channel
      synchronized(channel){
        try{

          // Position the channel
          channel.position(byteIndex + start);

          // Read
          channel.write(reusableSingleByteBuffer);

        }catch(IOException ioe){
          throw new RuntimeException(ioe); // TODO better exception
        }
      }

      // Flip bytebuffer
      reusableSingleByteBuffer.clear();
    }
  }

  @Override
  public void read(@Nonnegative long byteIndex, @Nonnull WriteOnlyByteArray destination)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    IndexingUtility.checkRead(byteIndex, destination, size);

    // If in a wrapper, unwrap it
    if(destination instanceof WriteOnlyByteArrayWrapper)
      destination = ((WriteOnlyByteArrayWrapper) destination).original;

    // If ByteBufferByteArray, use backing bytebuffer
    if(destination instanceof ByteBufferByteArray){

      // Get bytebuffer
      ByteBuffer destinationByteBuffer = ((ByteBufferByteArray) destination).data.duplicate();

      // Synchronize on this and channel
      synchronized(this){
        synchronized(channel){

          try{

            // Position the channel
            channel.position(start + byteIndex);

            // Write
            channel.read(destinationByteBuffer);

          }catch(IOException ioe){
            throw new RuntimeException(ioe); // TODO better exception
          }
        }
      }

      // Exit
      return;
    }

    // If PrimitiveByteArray, use backing byte array
    if(destination instanceof PrimitiveByteArray){

      // Cast
      PrimitiveByteArray pbadestination = (PrimitiveByteArray) destination;

      // Create bytebuffer
      ByteBuffer destinationBB = ByteBuffer.wrap(((PrimitiveByteArray) destination).data);

      // Adjust start/end
      destinationBB.position(pbadestination.start).limit(pbadestination.start + pbadestination.size);

      // Synchronize on this and channel
      synchronized(this){
        synchronized(channel){

          try{

            // Position the channel
            channel.position(start + byteIndex);

            // Write
            channel.read(destinationBB);

          }catch(IOException ioe){
            throw new RuntimeException(ioe); // TODO better exception
          }
        }
      }

      // Exit
      return;
    }

    // If unhandled, use manual byte copy

    // Set up bytebuffer before doing copy
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) destination.size()); // TODO size

    // Synchronize on channel
    synchronized(channel){

      try{

        // Position the channel
        channel.position(start + byteIndex);

        // Write
        channel.read(byteBuffer);

      }catch(IOException ioe){
        throw new RuntimeException(ioe); // TODO better exception
      }
    }

    // Copy over
    for(long index = 0; index < destination.size(); index++){
      destination.writeByte(index, byteBuffer.get((int) index));
    }
  }

  @Override
  public void write(@Nonnegative long byteIndex, @Nonnull ReadOnlyByteArray source)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException{

    // Check parameters
    IndexingUtility.checkWrite(byteIndex, source, size);

    // If in a wrapper, unwrap it
    if(source instanceof ReadOnlyByteArrayWrapper) source = ((ReadOnlyByteArrayWrapper) source).original;

    // If ByteBufferByteArray, use backing bytebuffer
    if(source instanceof ByteBufferByteArray){

      // Get bytebuffer
      ByteBuffer sourceByteBuffer = ((ByteBufferByteArray) source).data.duplicate();

      // Synchronize on this and channel
      synchronized(this){
        synchronized(channel){

          try{

            // Position the channel
            channel.position(start + byteIndex);

            // Write
            channel.write(sourceByteBuffer);

          }catch(IOException ioe){
            throw new RuntimeException(ioe); // TODO better exception
          }
        }
      }

      // Exit
      return;
    }

    // If PrimitiveByteArray, use backing byte array
    if(source instanceof PrimitiveByteArray){

      // Cast
      PrimitiveByteArray pbaSource = (PrimitiveByteArray) source;

      // Create bytebuffer
      ByteBuffer sourceBB = ByteBuffer.wrap(((PrimitiveByteArray) source).data);

      // Adjust start/end
      sourceBB.position(pbaSource.start).limit(pbaSource.start + pbaSource.size);

      // Synchronize on this and channel
      synchronized(this){
        synchronized(channel){

          try{

            // Position the channel
            channel.position(start + byteIndex);

            // Write
            channel.write(sourceBB);

          }catch(IOException ioe){
            throw new RuntimeException(ioe); // TODO better exception
          }
        }
      }

      // Exit
      return;
    }

    // If unhandled, use manual byte copy

    // Set up bytebuffer before doing copy
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) source.size()); // TODO size

    // Copy bytes
    for(long index = 0; index < source.size(); index++){
      byteBuffer.put((int) index, source.readByte(index));
    }

    // Synchronize on channel
    synchronized(channel){

      try{

        // Position the channel
        channel.position(start + byteIndex);

        // Write
        channel.write(byteBuffer);

      }catch(IOException ioe){
        throw new RuntimeException(ioe); // TODO better exception
      }
    }
  }

  @Nonnull
  @Override
  public ReadableWritableByteArray subsetOf(long start, long length)
  throws ByteArrayIndexOutOfBoundsException, ByteArrayLengthOverBoundsException, ByteArrayInvalidLengthException{
    if(start == 0 && length == size) return this;
    IndexingUtility.checkSubsetOf(start, length, size);
    return new ChannelByteArray(channel, this.start + start, length);
  }
}
