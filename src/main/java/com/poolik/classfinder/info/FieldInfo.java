package com.poolik.classfinder.info;

/**
 * Holds information about a field within a class.
 *
 * @see ClassInfo
 */
public class FieldInfo {
  private int access = 0;
  private String name = null;
  private String description = null;
  private String signature = null;
  private Object value = null;

 /**
   * Create and initialize a new <tt>FieldInfo</tt> object.
   *
   * @param access      field access modifiers, etc.
   * @param name        field name
   * @param description field description
   * @param signature   field signature
   * @param value       field value, if any
   */
  public FieldInfo(int access,
                   String name,
                   String description,
                   String signature,
                   Object value) {
    this.access = access;
    this.name = name;
    this.description = description;
    this.signature = signature;
    this.value = value;
  }

  /**
   * Get the access modifiers for this field
   *
   * @return the access modifiers, or 0 if none are set.
   */
  public int getAccess() {
    return access;
  }

  /**
   * Get the field name.
   *
   * @return the field name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the field description, if any.
   *
   * @return the field description, or null
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the field's signature, if any.
   *
   * @return the field signature, or null.
   */
  public String getSignature() {
    return signature;
  }

  /**
   * Get the field value, if any.
   *
   * @return the field value, or null.
   */
  public Object getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FieldInfo fieldInfo = (FieldInfo) o;

    return description.equals(fieldInfo.description)
        && name.equals(fieldInfo.name)
        && !(signature != null ? !signature.equals(fieldInfo.signature) : fieldInfo.signature != null);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + (signature != null ? signature.hashCode() : 0);
    return result;
  }

  /**
   * Return a string representation of the method. Currently, the string
   * representation is just the method's signature, or the name if the
   * signature is null.
   *
   * @return a string representation
   */
  public String toString() {
    return (signature != null) ? signature : name;
  }
}
