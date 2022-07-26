package com.ansill.arrays;

import javax.annotation.Nonnull;

public class ByteArrayLengthOverBoundsException extends Exception{ // TODO change to IllegalArgumentException when done
  private static final long serialVersionUID = 46772783801854786L;

  ByteArrayLengthOverBoundsException(@Nonnull String message){
    super(message);
  }
}
