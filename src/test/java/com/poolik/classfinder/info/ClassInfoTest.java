package com.poolik.classfinder.info;

import com.poolik.classfinder.ClassFinder;
import com.poolik.classfinder.TestWithTestClasses;
import com.poolik.classfinder.filter.Subclass;
import com.poolik.classfinder.otherTestClasses.AbstractClass;
import com.poolik.classfinder.otherTestClasses.ConcreteClass;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ClassInfoTest extends TestWithTestClasses {

  private ClassFinder getClassFinder() {
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(otherClassesFolder.toFile());
    return classFinder;
  }

  @Test
  public void findsCorrectFieldInfo() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new Subclass(AbstractClass.class));
    assertThat(classes.size(), is(1));
    ClassInfo info = classes.iterator().next();
    assertThat(info.getClassName(), is(ConcreteClass.class.getName()));

    Set<FieldInfo> fields = info.getFields();
    assertThat(fields.size(), is(2));
    assertTrue(Modifier.isPrivate(getField("test", fields).getAccess()));
    assertTrue(Modifier.isPublic(getField("anotherValue", fields).getAccess()));
  }

  @Test
  public void findsCorrectMethodInfo() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new Subclass(AbstractClass.class));
    assertThat(classes.size(), is(1));
    ClassInfo info = classes.iterator().next();
    assertThat(info.getClassName(), is(ConcreteClass.class.getName()));

    Set<MethodInfo> methods = info.getMethods();
    System.out.println(methods);
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
