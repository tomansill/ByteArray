package com.ansill.arrays;

import javax.annotation.Nonnull;

/** Exception that indicates that the provided length is over the bounds */
public class ByteArrayLengthOverBoundsException extends IllegalArgumentException{

  /** Serial Version UID */
  private static final long serialVersionUID = 46772783801854786L;

  /**
   * Constructor
   *
   * @param message message
   */
  ByteArrayLengthOverBoundsException(@Nonnull String message){
    super(message);
  }
}
