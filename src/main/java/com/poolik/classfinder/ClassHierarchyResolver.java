package com.poolik.classfinder;

import com.poolik.classfinder.info.ClassInfo;

import java.util.HashMap;
import java.util.Map;

public class ClassHierarchyResolver {

  private final Map<String,ClassInfo> foundClasses;

  public ClassHierarchyResolver(Map<String, ClassInfo> foundClasses) {
    this.foundClasses = foundClasses;
  }

  public Map<String, ClassInfo> findAllSuperClasses(ClassInfo classInfo) {
    Map<String, ClassInfo> superClasses = new HashMap<>();
    String superClassName = classInfo.getSuperClassName();
    if (superClassName != null) {
      ClassInfo superClassInfo = foundClasses.get(superClassName);
      if (superClassInfo != null) {
        superClasses.put(superClassName, superClassInfo);
        superClasses.putAll(findAllSuperClasses(superClassInfo));
      }
    }
    return superClasses;
  }

  public Map<String, ClassInfo> findAllInterfaces(ClassInfo classInfo) {
    Map<String, ClassInfo> interfaces = new HashMap<>();
    interfaces.putAll(recursivelyLoadnterfacesOfSuperclass(classInfo));

    for (String interfaceName : classInfo.getInterfaces()) {
      ClassInfo intfClassInfo = foundClasses.get(interfaceName);
      if (intfClassInfo != null) {
        interfaces.put(interfaceName, intfClassInfo);
        interfaces.putAll(findAllInterfaces(intfClassInfo));
      }
    }
    return interfaces;
  }

  private Map<String, ClassInfo> recursivelyLoadnterfacesOfSuperclass(ClassInfo classInfo) {
    Map<String, ClassInfo> interfaces = new HashMap<>();
    String superClassName = classInfo.getSuperClassName();
    if (superClassName != null) {
      ClassInfo superClassInfo = foundClasses.get(superClassName);
      if (superClassInfo != null) {
        interfaces.putAll(findAllInterfaces(superClassInfo));
      }
    }
    return interfaces;
  }
}