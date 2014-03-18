package com.poolik.classfinder;

import org.objectweb.asm.*;

public class EmptyVisitor extends ClassVisitor {

  public EmptyVisitor() {
    super(Opcodes.ASM4);
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    return null;
  }
}