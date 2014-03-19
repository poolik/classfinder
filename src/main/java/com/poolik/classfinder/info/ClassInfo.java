/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M. Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package com.poolik.classfinder.info;

import com.poolik.classfinder.ClassFinderException;
import com.poolik.classfinder.EmptyVisitor;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Holds information about a loaded class in a way that doesn't rely on
 * the underlying API used to load the class information.</p>
 * 
 * <p>This class relies on the ASM byte-code manipulation library. If that
 * library is not available, this package will not work. See
 * <a href="http://asm.objectweb.org"><i>asm.objectweb.org</i></a> for
 * details on ASM.</p>
 *
 * @author Copyright &copy; 2006 Brian M. Clapper
 * @version <tt>$Revision$</tt>
 */
public class ClassInfo extends EmptyVisitor {
  public static int ASM_CR_ACCEPT_CRITERIA = 0;

  private int modifier = 0;
  private String className = null;
  private String superClassName = null;
  private String[] implementedInterfaces = null;
  private File locationFound = null;
  private Set<FieldInfo> fields = new HashSet<>();
  private Set<MethodInfo> methods = new HashSet<>();
  private Set<AnnotationInfo> annotations = new HashSet<>();

  /**
   * Create a new <tt>ClassInfo</tt> object.
   *
   * @param name           the class name
   * @param superClassName the parent class name, or null
   * @param interfaces     the names of interfaces the class implements,
   *                       or null
   * @param asmAccessMask  ASM API's access mask for the class
   * @param location       File (jar, zip) or directory where class was found
   */
  public ClassInfo(String name,
            String superClassName,
            String[] interfaces,
            int asmAccessMask,
            File location) {
    setClassFields(name, superClassName, interfaces, asmAccessMask, location);
  }


  public String getClassName() {
    return className;
  }

  /**
   * Get the parent (super) class name, if any. Returns null if the
   * superclass is <tt>java.lang.Object</tt>. Note: To find other
   * ancestor classes, use {@link com.poolik.classfinder.ClassHierarchyResolver#findAllSuperClasses}.
   *
   * @return the super class name, or null
   * @see com.poolik.classfinder.ClassHierarchyResolver#findAllSuperClasses
   */
  public String getSuperClassName() {
    return superClassName;
  }

  /**
   * Get the names of all <i>directly</i> implemented interfaces. To find
   * indirectly implemented interfaces, use
   * {@link com.poolik.classfinder.ClassHierarchyResolver#findAllInterfaces}.
   *
   * @return an array of the names of all directly implemented interfaces,
   * or null if there are none
   * @see com.poolik.classfinder.ClassHierarchyResolver#findAllInterfaces
   */
  public String[] getInterfaces() {
    return implementedInterfaces;
  }

  /**
   * Get the Reflection API-based modifier bitfield for the class. Use
   * <tt>java.lang.reflect.Modifier</tt> to decode this bitfield.
   *
   * @return the modifier
   */
  public int getModifier() {
    return modifier;
  }

  public File getClassLocation() {
    return locationFound;
  }

  public Set<FieldInfo> getFields() {
    return fields;
  }

  public Set<MethodInfo> getMethods() {
    return methods;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();

    if ((modifier & Modifier.PUBLIC) != 0)
      buf.append("public ");

    if ((modifier & Modifier.ABSTRACT) != 0)
      buf.append("abstract ");

    if ((modifier & Modifier.INTERFACE) != 0)
      buf.append("interface ");
    else
      buf.append("class ");

    buf.append(className);

    String sep = " ";
    if (implementedInterfaces.length > 0) {
      buf.append(" implements");
      for (String intf : implementedInterfaces) {
        buf.append(sep);
        buf.append(intf);
      }
    }

    if ((superClassName != null) &&
        (!superClassName.equals("java.lang.Object"))) {
      buf.append(sep);
      buf.append("extends ");
      buf.append(superClassName);
    }

    return buf.toString();
  }

  /**
   * "Visit" a field.
   *
   * @param access      field access modifiers, etc.
   * @param name        field name
   * @param description field description
   * @param signature   field signature
   * @param value       field value, if any
   * @return null.
   */
  @Override
  public FieldVisitor visitField(int access,
                                 String name,
                                 String description,
                                 String signature,
                                 Object value) {
    fields.add(new FieldInfo(access,
        name,
        description,
        signature,
        value));
    return null;
  }

  /**
   * "Visit" a method.
   *
   * @param access      field access modifiers, etc.
   * @param name        field name
   * @param description field description
   * @param signature   field signature
   * @param exceptions  list of exception names the method throws
   * @return null.
   */
  @Override
  public MethodVisitor visitMethod(int access,
                                   String name,
                                   String description,
                                   String signature,
                                   String[] exceptions) {
    methods.add(new MethodInfo(access,
        name,
        description,
        signature,
        exceptions));
    return null;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    annotations.add(new AnnotationInfo(desc, visible));
    return null;
  }

  /**
   * Translate an internal class/interface name to an external one.
   *
   * @param internalName the internal JVM name, from the ASM API
   * @return the external name
   */
  private String translateInternalClassName(String internalName) {
    return internalName.replaceAll("/", ".");
  }

  /**
   * Set the fields in this object.
   *
   * @param name           the class name
   * @param superClassName the parent class name, or null
   * @param interfaces     the names of interfaces the class implements,
   *                       or null
   * @param asmAccessMask  ASM API's access mask for the class
   * @param location       File (jar, zip) or directory where class was found
   */
  private void setClassFields(String name,
                              String superClassName,
                              String[] interfaces,
                              int asmAccessMask,
                              File location) {
    this.className = translateInternalClassName(name);
    this.locationFound = location;

    if ((superClassName != null) &&
        (!superClassName.equals("java/lang/Object"))) {
      this.superClassName = translateInternalClassName(superClassName);
    }

    if (interfaces != null) {
      this.implementedInterfaces = new String[interfaces.length];
      for (int i = 0; i < interfaces.length; i++) {
        this.implementedInterfaces[i] =
            translateInternalClassName(interfaces[i]);
      }
    }

    modifier = convertAccessMaskToModifierMask(asmAccessMask);
  }

  /**
   * Convert an ASM access mask to a reflection Modifier mask.
   *
   * @param asmAccessMask the ASM access mask
   * @return the Modifier mask
   */
  private int convertAccessMaskToModifierMask(int asmAccessMask) {
    int modifier = 0;

    // Convert the ASM access info into Reflection API modifiers.

    if ((asmAccessMask & Opcodes.ACC_FINAL) != 0)
      modifier |= Modifier.FINAL;

    if ((asmAccessMask & Opcodes.ACC_NATIVE) != 0)
      modifier |= Modifier.NATIVE;

    if ((asmAccessMask & Opcodes.ACC_INTERFACE) != 0)
      modifier |= Modifier.INTERFACE;

    if ((asmAccessMask & Opcodes.ACC_ABSTRACT) != 0)
      modifier |= Modifier.ABSTRACT;

    if ((asmAccessMask & Opcodes.ACC_PRIVATE) != 0)
      modifier |= Modifier.PRIVATE;

    if ((asmAccessMask & Opcodes.ACC_PROTECTED) != 0)
      modifier |= Modifier.PROTECTED;

    if ((asmAccessMask & Opcodes.ACC_PUBLIC) != 0)
      modifier |= Modifier.PUBLIC;

    if ((asmAccessMask & Opcodes.ACC_STATIC) != 0)
      modifier |= Modifier.STATIC;

    if ((asmAccessMask & Opcodes.ACC_STRICT) != 0)
      modifier |= Modifier.STRICT;

    if ((asmAccessMask & Opcodes.ACC_SYNCHRONIZED) != 0)
      modifier |= Modifier.SYNCHRONIZED;

    if ((asmAccessMask & Opcodes.ACC_TRANSIENT) != 0)
      modifier |= Modifier.TRANSIENT;

    if ((asmAccessMask & Opcodes.ACC_VOLATILE) != 0)
      modifier |= Modifier.VOLATILE;

    return modifier;
  }

  public Set<AnnotationInfo> getAnnotations() {
    return annotations;
  }
}