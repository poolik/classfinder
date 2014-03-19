package com.poolik.classfinder.info;

import com.poolik.classfinder.ClassFinder;
import com.poolik.classfinder.TestWithTestClasses;
import com.poolik.classfinder.filter.And;
import com.poolik.classfinder.filter.Interface;
import com.poolik.classfinder.filter.Not;
import com.poolik.classfinder.filter.Subclass;
import com.poolik.classfinder.otherTestClasses.AbstractClass;
import com.poolik.classfinder.otherTestClasses.SomeInterface;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ClassInfoTest extends TestWithTestClasses {

  private ClassFinder getClassFinder() {
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(otherClassesFolder.toFile());
    return classFinder;
  }

  @Test
  public void toStringReturnSensibleClassSignature() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(Subclass.of(AbstractClass.class));
    assertThat(classes.iterator().next().toString(), is("public class com.poolik.classfinder.otherTestClasses.ConcreteClass extends com.poolik.classfinder.otherTestClasses.AbstractClass"));
    classes = getClassFinder().findClasses(And.allOf(Subclass.of(SomeInterface.class), Not.a(Subclass.of(AbstractClass.class)), Not.a(new Interface())));
    assertThat(classes.iterator().next().toString(), is("public class com.poolik.classfinder.otherTestClasses.SomeInterfaceImpl implements com.poolik.classfinder.otherTestClasses.SomeInterface"));
  }

  @Test
  public void findsCorrectFieldInfo() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new Subclass(AbstractClass.class));
    Set<FieldInfo> fields = classes.iterator().next().getFields();
    assertThat(fields.size(), is(2));
    assertTrue(Modifier.isPrivate(getField("test", fields).getAccess()));
    assertTrue(Modifier.isPublic(getField("anotherValue", fields).getAccess()));
  }

  @Test
  public void findsCorrectMethodInfo() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new Subclass(AbstractClass.class));
    Set<MethodInfo> methods = classes.iterator().next().getMethods();

    assertThat(methods.size(), is(3));
    assertTrue(Modifier.isPrivate(getMethod("test", methods).getAccess()));
    assertTrue(Modifier.isPublic(getMethod("another", methods).getAccess()));
    assertTrue(Modifier.isStatic(getMethod("another", methods).getAccess()));
  }

  private MethodInfo getMethod(String methodName, Set<MethodInfo> methods) {
    for (MethodInfo method : methods) {
      if (methodName.equals(method.getName())) return method;
    }
    return null;
  }

  private FieldInfo getField(String fieldName, Set<FieldInfo> fields) {
    for (FieldInfo field : fields) {
      if (fieldName.equals(field.getName())) return field;
    }
    return null;
  }
}