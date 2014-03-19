package com.poolik.classfinder;

import com.poolik.classfinder.info.ClassInfo;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public interface ClassLoader {
  public Map<String, ClassInfo> loadClassesFrom(Collection<File> placesToSearch);
}
