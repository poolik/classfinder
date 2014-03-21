#Classfinder
[![Build Status](https://travis-ci.org/poolik/classfinder.png?branch=master)](https://travis-ci.org/poolik/classfinder)
[![Coverage Status](https://coveralls.io/repos/poolik/classfinder/badge.png?branch=master)](https://coveralls.io/r/poolik/classfinder?branch=master)

Classpath, folder, zip and jar scanner to find specific classes. Classes can be filtered with various
filters to find only those that match some specific criteria. If presented with a folder to search, Classfinder
will recursively search it for .class, .jar and .zip files which are all also scanned.

Classfinder is an ideal library to create your own lightweight frameworks or tools, where you need to
dynamically find implementations of some interface or initiate objects. See examples below.

Once you've found the needed classes you can initiate new objects by
using ```Class.forName(classInfo.getClassName()).newInstance()```

This library is based on https://github.com/bmc/javautil.
I extracted the ```ClassFinder``` related portion of that library and updated
it to work with asm 4.1 (there's binary incompatibility between asm 3.x and 4.x).
I then added tests and started refactoring / further development.

#Filters
The following filters are available:

**Aggergate filters** - used to combine different concrete filters together with logical operators (AND, OR, NOT)
* Not.java
* And.java
* Or.java

**Concrete filters** - used to filter classes either by their modifiers (abstract, interface), name or other criteria
* Subclass.java - matches classes that are assignable to the base class
* Regex.java - matches classes by name
* Interface.java - matches only interfaces
* AbstractClass.java - matches only abstract classes
* Annotated.java - matches classes that are annotated with the given annotation

#Examples
#### Find all non abstract non interface classes implementing ```SomeInterface``` in ```someFolder```

NB! Note the use of simple factory methods, these are only for convenience and readability,
if you don't like them, you can always just create new ClassFilters directly

```java
ClassFinder finder = new ClassFinder().add(someFolder);

ClassFilter filter = And.allOf(
    Subclass.of(SomeInterface.class),
    Not.a(new Interface()),
    Not.a(new AbstractClass()));

Collection<ClassInfo> foundClasses = finder.findClasses(filter);
```

#### Find all test classes in ```myJar.jar```

```java
ClassFinder finder = new ClassFinder().add(Paths.get("/path/to/myJar.jar").toFile());

ClassFilter filter = Or.anyOf(
    Subclass.of(TestCase.class),
    Regex.matches(".*Test$"));

Collection<ClassInfo> testClasses = finder.findClasses(filter);
```

#### Find all ```@Deprecated``` classes in classpath

```java
ClassFinder finder = new ClassFinder().addClasspath();
Collection<ClassInfo> deprecated = finder.findClasses(Annotated.with(Deprecated.class));
```

#Acknowlegement
This product includes software developed by Brian M. Clapper
(bmc@clapper.org, http://www.clapper.org/bmc/). That software is
copyright (c) 2004-2007 Brian M. Clapper.