package com.poolik.classfinder.info;

import java.util.Arrays;

/**
 * Holds information about a method within a class.
 *
 * @see ClassInfo
 */
public class MethodInfo {
  private int access = 0;
  private String name = null;
  private String description = null;
  private String signature = null;
  private String[] exceptions = null;

  /**
   * Create and initialize a new <tt>MethodInfo</tt> object.
   *
   * @param access      method access modifiers, etc.
   * @param name        method name
   * @param description method description
   * @param signature   method signature
   * @param exceptions  list of thrown exceptions (by name)
   */
  public MethodInfo(int access,
                    String name,
                    String description,
                    String signature,
                    String[] exceptions) {
    this.access = access;
    this.name = name;
    this.description = description;
    this.signature = signature;
    this.exceptions = exceptions;
  }

  /**
   * Get the access modifiers for this method.
   *
   * @return the access modifiers, or 0 if none are set.
   */
  public int getAccess() {
    return access;
  }

  /**
   * Get the method name.
   *
   * @return the method name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the method description, if any.
   *
   * @return the method description, or null
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the method's signature, if any.
   *
   * @return the method signature, or null.
   */
  public String getSignature() {
    return signature;
  }

  /**
   * Get the class names of the thrown exceptions
   *
   * @return the names of the thrown exceptions, or null.
   */
  public String[] getExceptions() {
    return exceptions;
  }

  @Override
  public int hashCode() {
    return signature.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MethodInfo that = (MethodInfo) o;

    return access == that.access
        && description.equals(that.description)
        && Arrays.equals(exceptions, that.exceptions)
        && name.equals(that.name)
        && !(signature != null ? !signature.equals(that.signature) : that.signature != null);
  }

  public String toString() {
    return (signature != null) ? signature : name;
  }
}