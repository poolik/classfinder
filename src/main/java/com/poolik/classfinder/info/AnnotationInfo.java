package com.poolik.classfinder.info;

public class AnnotationInfo {

  private final String name;
  private final boolean visibleAtRuntime;

  public AnnotationInfo(String classDescriptor, boolean visibleAtRuntime) {
    this.name = getClassName(classDescriptor);
    this.visibleAtRuntime = visibleAtRuntime;
  }

  private String getClassName(String classDescriptor) {
    return classDescriptor.replace("L", "").replace(";", "").replaceAll("/", ".");
  }

  public String getName() {
    return name;
  }

  public boolean isVisibleAtRuntime() {
    return visibleAtRuntime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AnnotationInfo that = (AnnotationInfo) o;

    return visibleAtRuntime == that.visibleAtRuntime
        && name.equals(that.name);

  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (visibleAtRuntime ? 1 : 0);
    return result;
  }
}