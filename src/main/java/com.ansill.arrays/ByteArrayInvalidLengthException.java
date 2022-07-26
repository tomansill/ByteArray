package com.ansill.arrays;

import javax.annotation.Nonnull;

public class ByteArrayInvalidLengthException extends Exception{

  private static final long serialVersionUID = 4677582783801854786L; // TODO change to IllegalArgumentException when done

  ByteArrayInvalidLengthException(@Nonnull String message){
    super(message);
  }
}
