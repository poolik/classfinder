package com.poolik.classfinder.resourceLoader;

import com.poolik.classfinder.ClassFinder;
import com.poolik.classfinder.info.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarClassPathentriesLoader implements AdditionalResourceLoader {
  private static final Logger log = LoggerFactory.getLogger(JarClassPathentriesLoader.class);

  @Override
  public boolean canLoadAdditional(File file) {
    return FileUtil.isJar(file.getAbsolutePath());
  }

  @Override
  public void loadAdditional(File jarFile, ClassFinder classFinder) {
    try {
      loadJarClassPathEntries(jarFile, classFinder);
    } catch (IOException ex) {
      log.error("I/O error processing jar file '"+jarFile.getPath() + "'", ex);
    }
  }

  private void loadJarClassPathEntries(File jarFile, ClassFinder classFinder) throws IOException {
    JarFile jar = new JarFile(jarFile);
    Manifest manifest = jar.getManifest();
    if (manifest == null)
      return;

    String classPathEntry = getManifestClasspathEntry(manifest);
    if (classPathEntry != null) {
      StringTokenizer tok = new StringTokenizer(classPathEntry);
      while (tok.hasMoreTokens()) {
        StringBuilder buf = new StringBuilder();
        String element = tok.nextToken();
        addJarFileParent(jarFile, buf);
        buf.append(element);

        addJarToPlacesToSearch(jarFile, classFinder, buf);
      }
    }
  }

  private void addJarToPlacesToSearch(File jarFile, ClassFinder classFinder, StringBuilder buf) {
    String classPathJar = buf.toString();
    log.debug("From " + jarFile.getPath() + ": " + classPathJar);
    classFinder.add(new File(classPathJar));
  }

  private void addJarFileParent(File jarFile, StringBuilder buf) {
    String parent = jarFile.getParent();
    if (parent != null) {
      buf.append(parent);
      buf.append(File.separator);
    }
  }

  private String getManifestClasspathEntry(Manifest manifest) {
    Attributes attrs = manifest.getMainAttributes();
    for (Object key : attrs.keySet()) {
      if (key.toString().equals("Class-Path")) {
        return (String) attrs.get(key);
      }
    }
    return null;
  }
}