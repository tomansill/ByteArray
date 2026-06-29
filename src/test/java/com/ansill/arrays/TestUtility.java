package com.ansill.arrays;

import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TestUtility{

  public static short flipEndian(short value){
    int b1 = (value >> 8) & 0xff;
    int b0 = value & 0xff;
    return (short) ((b0 << 8) | b1);
  }

  public static int flipEndian(int value) {
    return Integer.reverseBytes(value);
  }

  public static long flipEndian(long value) {
    return Long.reverseBytes(value);
  }

  public static float flipEndian(float value) {
    int bits = Float.floatToRawIntBits(value);
    bits = Integer.reverseBytes(bits);
    return Float.intBitsToFloat(bits);
  }

  public static double flipEndian(double value) {
    long bits = Double.doubleToRawLongBits(value);
    bits = Long.reverseBytes(bits);
    return Double.longBitsToDouble(bits);
  }

  @Nonnull
  public static String f(@Nonnull String message, @Nullable Object object, @Nonnull Object... objects){
    return format(message, object, objects);
  }

  @Nonnull
  public static String format(@Nullable String message, @Nullable Object object, @Nullable Object... objects){
    if(object == null && objects == null){
      return format(message, (Object[]) null);
    }else if(object == null){
      return format(message, objects);
    }else if(objects == null){
      return format(message, new Object[]{object});
    }else{
      Object[] newobj = new Object[objects.length + 1];
      newobj[0] = object;
      System.arraycopy(objects, 0, newobj, 1, objects.length);
      return format(message, newobj);
    }
  }

  @Nonnull
  private static String format(@Nullable String message, @Nullable Object... objects){
    if(message == null){
      return "null";
    }else if(objects == null){
      return message;
    }else{
      StringBuilder builder = new StringBuilder();
      int objectsIndex = 0;
      int previousIndex = 0;

      for(int braceIndex = 0; objectsIndex < objects.length && (braceIndex = message.indexOf("{}", braceIndex)) !=
                                                               -1; ++objectsIndex){
        builder.append(message, previousIndex, braceIndex);
        braceIndex = Math.min(braceIndex + 2, message.length());
        previousIndex = braceIndex;
        if(objects[objectsIndex] == null){
          builder.append("null");
        }else if(objects[objectsIndex] instanceof String){
          builder.append((String) objects[objectsIndex]);
        }else{
          builder.append(objects[objectsIndex].toString());
        }
      }

      if(previousIndex < message.length()){
        builder.append(message, previousIndex, message.length());
      }

      return builder.toString();
    }
  }

  /**
   * Recursively cleans ByteArrays by looking for TestOnlyByteArrays and invoke Unsafe::invokeCleaner to clean up DirectByteBuffers
   *
   * @param byteArray byte array to be cleaned
   */
  public static void clean(@Nonnull ByteArray byteArray){

    // Detect and cast it to appropriate class
    if(byteArray instanceof ReadableWritableMultipleByteArray){
      ReadableWritableMultipleByteArray rwmba = (ReadableWritableMultipleByteArray) byteArray;
      for(ReadableWritableByteArray inner : rwmba.indexMap.values()) clean(inner);
    }else if(byteArray instanceof ReadOnlyMultipleByteArray){
      ReadOnlyMultipleByteArray romba = (ReadOnlyMultipleByteArray) byteArray;
      for(ReadOnlyByteArray inner : romba.indexMap.values()) clean(inner);
    }else if(byteArray instanceof TestOnlyByteArray.ReadOnly){
      TestOnlyByteArray.ReadOnly tobaro = (TestOnlyByteArray.ReadOnly) byteArray;
      clean(tobaro.original);
    }else if(byteArray instanceof WriteOnlyByteArrayWrapper){
      WriteOnlyByteArrayWrapper wobaw = (WriteOnlyByteArrayWrapper) byteArray;
      clean(wobaw.original);
    }else if(byteArray instanceof ReadOnlyByteArrayWrapper){
      ReadOnlyByteArrayWrapper robaw = (ReadOnlyByteArrayWrapper) byteArray;
      clean(robaw.original);
    }else if(byteArray instanceof TestOnlyByteArray){
      TestOnlyByteArray toba = (TestOnlyByteArray) byteArray;
      toba.clean();
    }else System.err.println("Unhandled class: " + byteArray.getClass().getName());
  }
}
