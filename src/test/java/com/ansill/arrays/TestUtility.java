package com.ansill.arrays;

import sun.misc.Unsafe;
import test.arrays.TestOnlyByteArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public final class TestUtility{

  public static final Unsafe UNSAFE;

  static{
    try{
      Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      UNSAFE = (Unsafe) f.get(null);
    }catch(NoSuchFieldException | IllegalAccessException nsfe){
      throw new ExceptionInInitializerError(nsfe);
    }
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
   * @param unsafe    unsafe object
   * @param byteArray byte array to be cleaned
   */
  public static void clean(@Nonnull Unsafe unsafe, @Nonnull ByteArray byteArray){

    // Detect and cast it to appropriate class
    if(byteArray instanceof ReadableWritableMultipleByteArray){
      ReadableWritableMultipleByteArray rwmba = (ReadableWritableMultipleByteArray) byteArray;
      for(ReadableWritableByteArray inner : rwmba.indexMap.values()) clean(unsafe, inner);
    }else if(byteArray instanceof ReadOnlyMultipleByteArray){
      ReadOnlyMultipleByteArray romba = (ReadOnlyMultipleByteArray) byteArray;
      for(ReadOnlyByteArray inner : romba.indexMap.values()) clean(unsafe, inner);
    }else if(byteArray instanceof TestOnlyByteArray.ReadOnly){
      TestOnlyByteArray.ReadOnly tobaro = (TestOnlyByteArray.ReadOnly) byteArray;
      clean(unsafe, tobaro.original);
    }else if(byteArray instanceof WriteOnlyByteArrayWrapper){
      WriteOnlyByteArrayWrapper wobaw = (WriteOnlyByteArrayWrapper) byteArray;
      clean(unsafe, wobaw.original);
    }else if(byteArray instanceof ReadOnlyByteArrayWrapper){
      ReadOnlyByteArrayWrapper robaw = (ReadOnlyByteArrayWrapper) byteArray;
      clean(unsafe, robaw.original);
    }else if(byteArray instanceof TestOnlyByteArray){
      TestOnlyByteArray toba = (TestOnlyByteArray) byteArray;
      for(ByteBuffer buffer : toba.data) unsafe.invokeCleaner(buffer);
    }else System.err.println("Unhandled class: " + byteArray.getClass().getName());
  }
}
