package com.ansill.arrays;

import javax.annotation.Nonnull;

/** Exception that indicates provided length is invalid */
public class ByteArrayInvalidLengthException extends Exception{

  /** Serial Version UID */
  private static final long serialVersionUID = 4677582783801854786L; // TODO change to IllegalArgumentException when done

  /**
   * Constructor
   *
   * @param message message
   */
  ByteArrayInvalidLengthException(@Nonnull String message){
    super(message);
  }
}
