package com.ansill.arrays;

import javax.annotation.Nonnull;

/** Exception that indicates Byte Array index is out of the bounds */
public class ByteArrayIndexOutOfBoundsException extends IllegalArgumentException{

  /** Serial version UID */
  private static final long serialVersionUID = -3888286169181876110L;

  /**
   * Constructor
   *
   * @param message message
   */
  ByteArrayIndexOutOfBoundsException(@Nonnull String message){
    super(message);
  }

}
