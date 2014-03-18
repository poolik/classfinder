package com.poolik.classfinder;

import com.poolik.classfinder.filter.*;
import com.poolik.classfinder.info.ClassInfo;
import com.poolik.classfinder.otherTestClasses.AbstractClass;
import com.poolik.classfinder.otherTestClasses.ConcreteClass;
import com.poolik.classfinder.otherTestClasses.SomeInterface;
import com.poolik.classfinder.testClasses.TestClass1;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ClassFinderFilterTest extends TestWithTestClasses {

  @Test
  public void filtersInterfaces() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new InterfaceOnlyClassFilter());
    assertThat(classes.size(), is(1));
    assertThat(classes.iterator().next().getClassName(), is(SomeInterface.class.getName()));
  }

  @Test
  public void filtersAbstractClasses() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new AbstractClassFilter());

    assertThat(classes.size(), is(1));
    assertThat(classes.iterator().next().getClassName(), is(AbstractClass.class.getName()));
  }

  @Test
  public void filtersByClassNameRegex() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new RegexClassFilter(".*\\d$"));
    assertThat(classes.size(), is(3));
  }

  @Test
  public void filtersSubclassesOfAbstractClass() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new SubclassClassFilter(AbstractClass.class));
    assertThat(classes.size(), is(1));
    assertThat(classes.iterator().next().getClassName(), is(ConcreteClass.class.getName()));
  }

  @Test
  public void filtersSubclassesOfInterface() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new SubclassClassFilter(SomeInterface.class));
    assertThat(classes.size(), is(3));
  }

  @Test
  public void combinesFiltersWithAnd() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new AndClassFilter(new SubclassClassFilter(SomeInterface.class), new AbstractClassFilter()));
    assertThat(classes.size(), is(1));
    assertThat(classes.iterator().next().getClassName(), is(AbstractClass.class.getName()));
  }

  @Test
  public void combinesFiltersWithOr() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new OrClassFilter(new RegexClassFilter(".*1$"), new AbstractClassFilter()));
    assertThat(classes.size(), is(2));
    assertThat(classes.iterator().next().getClassName(), anyOf(is(TestClass1.class.getName()), is(AbstractClass.class.getName())));
    assertThat(classes.iterator().next().getClassName(), anyOf(is(TestClass1.class.getName()), is(AbstractClass.class.getName())));
  }

  @Test
  public void invertsFilterWithNot() {
    Collection<ClassInfo> classes = getClassFinder().findClasses(new NotClassFilter(new RegexClassFilter(".*\\d$")));
    assertThat(classes.size(), is(6));
  }

  private ClassFinder getClassFinder() {
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(new File[]{otherClassesFolder.toFile(), classesFolder.toFile()});
    return classFinder;
  }
}
