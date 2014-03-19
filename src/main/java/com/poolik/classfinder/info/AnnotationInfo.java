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
}
