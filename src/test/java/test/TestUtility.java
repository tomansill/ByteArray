package test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TestUtility{

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
}
