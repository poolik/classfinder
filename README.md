#Classfinder

Classpath scanner to find / filter specific classes

This library is largely based on https://github.com/bmc/javautil.
I extracted only the ```ClassFinder``` related portion of that library and updated
it to work with asm 4.1 (there's binary incompatibility between asm 3.x and 4.x
so this library didn't work together with projects that use asm 4.x)

#Filters
The following filters are available:

**Aggergate filters** - used to combine different concrete filters together with logical operators (AND, OR, NOT)
* NotClassFilter.java
* AndClassFilter.java
* OrClassFilter.java

**Concrete filters** - used to filter classes either by their modifiers (abstract, interface) or name
* SubclassClassFilter.java - matches classes that are assignable to the base class
* RegexClassFilter.java - matches classes by name
* InterfaceOnlyClassFilter.java - matches classes that implement given interface
* AbstractClassFilter.java - matches only abstract classes

#Examples
* Find all non abstract non interface classes implementing ```SomeInterface.class``` in ```someFolder```

```java
ClassFinder classFinder = new ClassFinder();
classFinder.add(someFolder);

ClassFilter filter = new AndClassFilter(
    new SubclassClassFilter(SomeInterface.class),
    new NotClassFilter(new InterfaceOnlyClassFilter()),
    new NotClassFilter(new AbstractClassFilter()));

Collection<ClassInfo> foundClasses = classFinder.findClasses(filter);
```

#Acknowlegement
This product includes software developed by Brian M. Clapper
(bmc@clapper.org, http://www.clapper.org/bmc/). That software is
copyright (c) 2004-2007 Brian M. Clapper.
