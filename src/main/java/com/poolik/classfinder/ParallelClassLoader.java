package com.poolik.classfinder;

import com.poolik.classfinder.info.ClassInfo;
import com.poolik.classfinder.info.FileUtil;
import com.poolik.classfinder.io.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ParallelClassLoader implements ClassLoader {
  private static final Logger log = LoggerFactory.getLogger(ParallelClassLoader.class);

  @Override
  public Map<String, ClassInfo> loadClassesFrom(Collection<File> placesToSearch) {
    HashMap<String, ClassInfo> foundClasses = new HashMap<>();
    for (File file : placesToSearch) {
      loadClassesIn(file, foundClasses);
    }
    return foundClasses;
  }

  private void loadClassesIn(File file, Map<String, ClassInfo> foundClasses) {
    String name = file.getPath();

    log.info("Finding classes in " + name);
    if (FileUtil.isJar(name))
      processJar(name, foundClasses);
    else if (FileUtil.isZip(name))
      processZip(name, foundClasses);
    else
      processDirectory(file, foundClasses);
  }

  private void processJar(String jarName,
                          Map<String, ClassInfo> foundClasses) {
    JarFile jar = null;
    try {
      jar = new JarFile(jarName);
      File jarFile = new File(jarName);
      processOpenZip(jar, jarFile,
          new ClassInfoClassVisitor(foundClasses, jarFile));
    } catch (IOException ex) {
      log.error("Can't open jar file \"" + jarName + "\"", ex);
    } finally {
      try {
        if (jar != null) jar.close();
      } catch (IOException ex) {
        log.error("Can't close " + jarName, ex);
      }
    }
  }

  private void processZip(String zipName,
                          Map<String, ClassInfo> foundClasses) {
    ZipFile zip = null;

    try {
      zip = new ZipFile(zipName);
      File zipFile = new File(zipName);
      processOpenZip(zip, zipFile,
          new ClassInfoClassVisitor(foundClasses, zipFile));
    } catch (IOException ex) {
      log.error("Can't open jar file \"" + zipName + "\"", ex);
    } finally {
      try {
        if (zip != null) zip.close();
      } catch (IOException ex) {
        log.error("Can't close " + zipName, ex);
      }
    }
  }

  private void processOpenZip(ZipFile zip,
                              File zipFile,
                              ClassVisitor classVisitor) {
    String zipName = zipFile.getPath();
    for (Enumeration<? extends ZipEntry> e = zip.entries();
         e.hasMoreElements(); ) {
      ZipEntry entry = e.nextElement();

      if ((!entry.isDirectory()) &&
          (entry.getName().toLowerCase().endsWith(".class"))) {
        try {
          log.trace("Loading " + zipName + "(" + entry.getName() +
              ")");
          loadClassData(zip.getInputStream(entry), classVisitor);
        } catch (IOException | ClassFinderException ex) {
          log.error("Can't open \"" + entry.getName() +
                  "\" in zip file \"" + zipName + "\": ",
              ex);
        }
      }
    }
  }

  private void processDirectory(File dir,
                                Map<String, ClassInfo> foundClasses) {
    loadAllClassFilesInDir(dir, foundClasses);
    loadAllJarFilesInDir(dir, foundClasses);
    loadAllZipFilesInDir(dir, foundClasses);
  }

  private void loadAllZipFilesInDir(File dir, Map<String, ClassInfo> foundClasses) {
    loadAllFilesWithSuffixInDir(dir, ".zip", foundClasses);
  }

  private void loadAllJarFilesInDir(File dir, Map<String, ClassInfo> foundClasses) {
    loadAllFilesWithSuffixInDir(dir, ".jar", foundClasses);
  }

  private void loadAllFilesWithSuffixInDir(File dir, String suffix, Map<String, ClassInfo> foundClasses) {
    Collection<File> jarFiles = filterFilesBySuffix(dir, suffix);
    for (File jarFile : jarFiles) {
      loadClassesIn(jarFile, foundClasses);
    }
  }

  private void loadAllClassFilesInDir(File dir, Map<String, ClassInfo> foundClasses) {
    Collection<File> files = filterFilesBySuffix(dir, ".class");

    ClassVisitor classVisitor = new ClassInfoClassVisitor(foundClasses,
        dir);

    for (File f : files) {
      String path = f.getPath();
      log.trace("Loading " + f.getPath());
      InputStream is = null;
      try {
        is = new FileInputStream(f);
        loadClassData(is, classVisitor);
      } catch (IOException | ClassFinderException ex) {
        log.error("Can't open \"" + path + "\": ", ex);
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException ex) {
            log.error("Can't close InputStream for \"" +
                    path + "\"",
                ex);
          }
        }
      }
    }
  }

  private Collection<File> filterFilesBySuffix(File dir, String suffix) {
    RecursiveFileFinder finder = new RecursiveFileFinder();
    RegexFileFilter nameFilter = new RegexFileFilter("\\"+suffix+"$", FileFilterMatchType.FILENAME);
    AndFileFilter fileFilter = new AndFileFilter(nameFilter, new FileOnlyFilter());
    Collection<File> files = new ArrayList<>();
    finder.findFiles(dir, fileFilter, files);
    return files;
  }

  private void loadClassData(InputStream is, ClassVisitor classVisitor)
      throws ClassFinderException {
    try {
      ClassReader cr = new ClassReader(is);
      cr.accept(classVisitor, ClassInfo.ASM_CR_ACCEPT_CRITERIA);
    } catch (Exception ex) {
      throw new ClassFinderException("Unable to load class from open input stream", ex);
    }
  }
}
