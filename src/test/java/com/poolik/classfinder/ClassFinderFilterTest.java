package com.poolik.classfinder;

import com.poolik.classfinder.filter.*;
import com.poolik.classfinder.info.ClassInfo;
import com.poolik.classfinder.otherTestClasses.ConcreteClass;
import com.poolik.classfinder.otherTestClasses.SomeInterface;
import com.poolik.classfinder.testClasses.TestClass1;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static com.poolik.classfinder.filter.And.allOf;
import static com.poolik.classfinder.filter.Or.anyOf;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ClassFinderFilterTest extends TestWithTestClasses {

  @Test
  public void filtersInterfaces() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new Interface());
    assertThat(classes.size(), is(1));
    assertThat(classes.iterator().next().getClassName(), is(SomeInterface.class.getName()));
  }

  @Test
  public void filtersAbstractClasses() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new AbstractClass());

    assertThat(classes.size(), is(1));
    assertThat(classes.iterator().next().getClassName(), is(com.poolik.classfinder.otherTestClasses.AbstractClass.class.getName()));
  }

  @Test
  public void filtersByClassNameRegex() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(Regex.matches(".*\\d$"));
    assertThat(classes.size(), is(3));
  }

  @Test
  public void filtersSubclassesOfAbstractClass() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(Subclass.of(com.poolik.classfinder.otherTestClasses.AbstractClass.class));
    assertThat(classes.size(), is(1));
    assertThat(classes.iterator().next().getClassName(), is(ConcreteClass.class.getName()));
  }

  @Test
  public void filtersSubclassesOfInterface() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(Subclass.of(SomeInterface.class));
    assertThat(classes.size(), is(3));
  }

  @Test
  public void combinesFiltersWithAnd() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(allOf(Subclass.of(SomeInterface.class), new AbstractClass()));
    assertThat(classes.size(), is(1));
    assertThat(classes.iterator().next().getClassName(), is(com.poolik.classfinder.otherTestClasses.AbstractClass.class.getName()));
  }

  @Test
  public void combinesFiltersWithOr() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(anyOf(Regex.matches(".*1$"), new AbstractClass()));
    assertThat(classes.size(), is(2));
    assertThat(classes.iterator().next().getClassName(), anyOf(is(TestClass1.class.getName()), is(com.poolik.classfinder.otherTestClasses.AbstractClass.class.getName())));
    assertThat(classes.iterator().next().getClassName(), anyOf(is(TestClass1.class.getName()), is(com.poolik.classfinder.otherTestClasses.AbstractClass.class.getName())));
  }

  @Test
  public void invertsFilterWithNot() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(Not.a(Regex.matches(".*\\d$")));
    assertThat(classes.size(), is(6));
  }

  @Test
  public void filtersByAnnotation() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(Annotated.with(Deprecated.class));
    assertThat(classes.size(), is(1));
    assertThat(classes.iterator().next().getAnnotations().size(), is(2));
  }

  private ClassFinder getClassFinder() {
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(new File[]{otherClassesFolder.toFile(), classesFolder.toFile()});
    return classFinder;
  }
}
