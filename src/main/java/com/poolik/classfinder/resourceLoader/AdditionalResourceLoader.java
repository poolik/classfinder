package com.poolik.classfinder.resourceLoader;

import com.poolik.classfinder.ClassFinder;

import java.io.File;

public interface AdditionalResourceLoader {
  public boolean canLoadAdditional(File file);
  public void loadAdditional(File file, ClassFinder classFinder);
}
