package com.ansill.arrays;

import javax.annotation.Nonnull;

public class ByteArrayIndexOutOfBoundsException extends Exception{ // TODO change to illegalargumentexception when done
  private static final long serialVersionUID = -3888286169181876110L;

  ByteArrayIndexOutOfBoundsException(@Nonnull String message){
    super(message);
  }

}
