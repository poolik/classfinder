package com.poolik.classfinder.filter;

import com.poolik.classfinder.ClassHierarchyResolver;
import com.poolik.classfinder.info.AnnotationInfo;
import com.poolik.classfinder.info.ClassInfo;

public class Annotated implements ClassFilter {
  private final Class<?> annotation;

  public static ClassFilter with(Class<?> annotation) {
    return new Annotated(annotation);
  }

  public Annotated(Class<?> annotation) {
    this.annotation = annotation;
  }

  @Override
  public boolean accept(ClassInfo classInfo, ClassHierarchyResolver hierarchyFinder) {
    for (AnnotationInfo annotationInfo : classInfo.getAnnotations()) {
      if (annotation.getName().equals(annotationInfo.getName())) return true;
    }
    return false;
  }
}
