/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M. Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package com.poolik.classfinder;

import com.poolik.classfinder.filter.ClassFilter;
import com.poolik.classfinder.info.ClassInfo;
import com.poolik.classfinder.info.ClassUtil;
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
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * <p>A <tt>ClassFinder</tt> object is used to find classes. By default, an
 * instantiated <tt>ClassFinder</tt> won't find any classes; you have to
 * add the classpath (via a call to {@link #addClassPath}), add jar files,
 * add zip files, and/or add directories to the <tt>ClassFinder</tt> so it
 * knows where to look. Adding a jar file to a <tt>ClassFinder</tt> causes
 * the <tt>ClassFinder</tt> to look at the jar's manifest for a
 * "Class-Path" entry; if the <tt>ClassFinder</tt> finds such an entry, it
 * adds the contents to the search path, as well. After the
 * <tt>ClassFinder</tt> has been "primed" with things to search, you call
 * its {@link #findClasses findClasses()} method to have it search for
 * the classes, optionally passing a {@link com.poolik.classfinder.filter.ClassFilter} that can be used
 * to filter out classes you're not interested in.</p>
 * <p/>
 * <p>This package also contains a rich set of {@link com.poolik.classfinder.filter.ClassFilter}
 * implementations, including:</p>
 * <p/>
 * <ul>
 * <li>A {@link com.poolik.classfinder.filter.RegexClassFilter} for filtering class names on a regular
 * expression
 * <li>Filters for testing various class attributes (such as whether a
 * class is an interface, or a subclass of a known class, etc.
 * <li>Filters that can combine other filters in logical operations
 * </ul>
 * <p/>
 * <p>The following example illustrates how you might use a
 * <tt>ClassFinder</tt> to locate all non-abstract classes that implement
 * the <tt>ClassFilter</tt> interface, searching the classpath as well
 * as anything specified on the command line.</p>
 * <p/>
 * <blockquote><pre>
 * import com.poolik.classfinder.*;
 * <p/>
 * public class Test
 * {
 *     public static void main (String[] args) throws Throwable
 *     {
 *         ClassFinder finder = new ClassFinder();
 *         for (String arg : args)
 *             finder.add(new File(arg));
 * <p/>
 *         ClassFilter filter =
 *             new AndClassFilter
 *                 // Must not be an interface
 *                 (new NotClassFilter (new InterfaceOnlyClassFilter()),
 * <p/>
 *                 // Must implement the ClassFilter interface
 *                 new SubclassClassFilter (ClassFilter.class),
 * <p/>
 *                 // Must not be abstract
 *                 new NotClassFilter (new AbstractClassFilter()));
 * <p/>
 *         Collection&lt;ClassInfo&gt; foundClasses = new ArrayList&lt;ClassInfo&gt;();
 *         finder.findClasses (foundClasses, filter);
 * <p/>
 *         for (ClassInfo classInfo : foundClasses)
 *             System.out.println ("Found " + classInfo.getClassName());
 *     }
 * }
 * </pre></blockquote>
 * <p/>
 * <p>This class, and the {@link com.poolik.classfinder.info.ClassInfo} class, rely on the ASM
 * byte-code manipulation library. If that library is not available, this
 * package will not work. See
 * <a href="http://asm.objectweb.org"><i>asm.objectweb.org</i></a>
 * for details on ASM.</p>
 * <p/>
 * <p><b>WARNING: This class is not thread-safe.</b></p>
 *
 * @author Copyright &copy; 2006 Brian M. Clapper
 * @version <tt>$Revision$</tt>
 */
public class ClassFinder {

  private LinkedHashMap<String, File> placesToSearch = new LinkedHashMap<String, File>();
  private Map<String, ClassInfo> foundClasses = new LinkedHashMap<String, ClassInfo>();
  private static final Logger log = LoggerFactory.getLogger(ClassFinder.class);
  private boolean errorIfResultEmpty;

  /**
   * Add the contents of the system classpath for classes.
   */
  public void addClassPath() {
    String path;

    try {
      path = System.getProperty("java.class.path");
    } catch (Exception ex) {
      path = "";
      log.error("Unable to get class path", ex);
    }

    StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);

    while (tok.hasMoreTokens())
      add(new File(tok.nextToken()));
  }

  /**
   * Add a jar file, zip file or directory to the list of places to search
   * for classes.
   *
   * @param file the jar file, zip file or directory
   * @return <tt>true</tt> if the file was suitable for adding;
   * <tt>false</tt> if it was not a jar file, zip file, or
   * directory.
   */
  public boolean add(File file) {
    log.info("Adding file to look into: " + file.getAbsolutePath());
    boolean added = false;

    if (ClassUtil.fileCanContainClasses(file)) {
      String absPath = file.getAbsolutePath();
      if (placesToSearch.get(absPath) == null) {
        placesToSearch.put(absPath, file);
        if (isJar(absPath))
          loadJarClassPathEntries(file);
      }

      added = true;
    } else {
      log.info("The given path '" + file.getAbsolutePath() + "' cannot contain classes!");
    }

    return added;
  }

  /**
   * Add an array jar files, zip files and/or directories to the list of
   * places to search for classes.
   *
   * @param files the array of jar files, zip files and/or directories.
   *              The array can contain a mixture of all of the above.
   * @return the total number of items from the array that were actually
   * added. (Elements that aren't jars, zip files or directories
   * are skipped.)
   */
  public int add(File[] files) {
    log.info("Adding files to look into: " + Arrays.toString(files));

    int totalAdded = 0;
    for (File file : files) {
      if (add(file))
        totalAdded++;
    }

    return totalAdded;
  }

  /**
   * Add a <tt>Collection</tt> of jar files, zip files and/or directories
   * to the list of places to search for classes.
   *
   * @param files the collection of jar files, zip files and/or directories.
   * @return the total number of items from the collection that were actually
   * added. (Elements that aren't jars, zip files or directories
   * are skipped.)
   */
  public int add(Collection<File> files) {
    int totalAdded = 0;

    for (File file : files) {
      if (add(file))
        totalAdded++;
    }

    return totalAdded;
  }

  /**
   * Clear the finder's notion of where to search.
   */
  public void clear() {
    placesToSearch.clear();
    foundClasses.clear();
  }

  public void setErrorIfResultEmpty(boolean errorIfResultEmpty) {
    this.errorIfResultEmpty = errorIfResultEmpty;
  }

  /**
   * Find all classes in the search areas, implicitly accepting all of
   * them.
   *
   * @return Collection of found classes
   */
  public Collection<ClassInfo> findClasses() {
    return findClasses(null);
  }

  /**
   * Search all classes in the search areas, keeping only those that
   * pass the specified filter.
   *
   * @param filter  the filter, or null for no filter
   * @return the number of matched classes added to the collection
   */
  public Collection<ClassInfo> findClasses(ClassFilter filter) {
    int total = 0;
    foundClasses.clear();

    loadAllClasses();

    log.info("Loaded " + foundClasses.size() + " classes.");

    // Next, weed out the ones we don't want.
    Collection<ClassInfo> classes = new ArrayList<ClassInfo>();
    total = filterClasses(filter, total, classes);

    if (total == 0 && errorIfResultEmpty) {
      log.info("Found no classes, throwing exception");
      throw new ClassFinderException("Didn't find any classes");
    } else {
      log.info("Returning " + total + " total classes");
    }
    foundClasses.clear();
    return classes;
  }

  private int filterClasses(ClassFilter filter, int total, Collection<ClassInfo> classes) {
    for (ClassInfo classInfo : foundClasses.values()) {
      String className = classInfo.getClassName();
      String locationName = classInfo.getClassLocation().getPath();
      log.trace("Looking at " + locationName + " (" + className + ")");
      if ((filter == null) || (filter.accept(classInfo, this))) {
        log.trace("Filter accepted " + className);
        total++;
        classes.add(classInfo);
      } else {
        log.trace("Filter rejected " + className);
      }
    }
    return total;
  }

  private void loadAllClasses() {
    for (File file : placesToSearch.values()) {
      loadClassesIn(file);
    }
  }

  private void loadClassesIn(File file) {
    String name = file.getPath();

    log.info("Finding classes in " + name);
    if (isJar(name))
      processJar(name, foundClasses);
    else if (isZip(name))
      processZip(name, foundClasses);
    else
      processDirectory(file, foundClasses);
  }

  /**
   * Intended to be called only from a {@link ClassFilter} object's
   * {@link ClassFilter#accept accept()} method, this method attempts to
   * find all the superclasses (except <tt>java.lang.Object</tt>for a
   * given class, by checking all the currently-loaded class data.
   *
   * @param classInfo    the {@link ClassInfo} objects for the class
   * @param superClasses where to store the {@link ClassInfo} objects
   *                     for the superclasses. The map is indexed by
   *                     class name.
   * @return the number of superclasses found
   */
  public int findAllSuperClasses(ClassInfo classInfo,
                                 Map<String, ClassInfo> superClasses) {
    int total = 0;

    String superClassName = classInfo.getSuperClassName();
    if (superClassName != null) {
      ClassInfo superClassInfo = foundClasses.get(superClassName);
      if (superClassInfo != null) {
        superClasses.put(superClassName, superClassInfo);
        total++;
        total += findAllSuperClasses(superClassInfo, superClasses);
      }
    }

    return total;
  }

  /**
   * Intended to be called only from a {@link ClassFilter} object's
   * {@link ClassFilter#accept accept()} method, this method attempts to
   * find all the interfaces implemented by given class (directly and
   * indirectly), by checking all the currently-loaded class data.
   *
   * @param classInfo  the {@link ClassInfo} objects for the class
   * @param interfaces where to store the {@link ClassInfo} objects
   *                   for the interfaces. The map is indexed by
   *                   class name
   * @return the number of interfaces found
   */
  public int findAllInterfaces(ClassInfo classInfo,
                               Map<String, ClassInfo> interfaces) {
    int total = 0;
    String superClassName = classInfo.getSuperClassName();
    if (superClassName != null) {
      ClassInfo superClassInfo = foundClasses.get(superClassName);
      if (superClassInfo != null) {
        total += findAllInterfaces(superClassInfo, interfaces);
      }
    }

    String[] interfaceNames = classInfo.getInterfaces();
    if (interfaces != null) {
      for (String interfaceName : interfaceNames) {
        ClassInfo intfClassInfo = foundClasses.get(interfaceName);
        if (intfClassInfo != null) {
          interfaces.put(interfaceName, intfClassInfo);
          total++;
          total += findAllInterfaces(intfClassInfo, interfaces);
        }
      }
    }

    return total;
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
        } catch (IOException ex) {
          log.error("Can't open \"" + entry.getName() +
              "\" in zip file \"" + zipName + "\": ",
              ex);
        } catch (ClassFinderException ex) {
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
    loadAllJarFilesInDir(dir);
    loadAllZipFilesInDir(dir);
  }

  private void loadAllZipFilesInDir(File dir) {
    loadAllFilesWithSuffixInDir(dir, ".zip");
  }

  private void loadAllJarFilesInDir(File dir) {
    loadAllFilesWithSuffixInDir(dir, ".jar");
  }

  private void loadAllFilesWithSuffixInDir(File dir, String suffix) {
    Collection<File> jarFiles = filterFilesBySuffix(dir, suffix);
    for (File jarFile : jarFiles) {
      loadClassesIn(jarFile);
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
      } catch (IOException ex) {
        log.error("Can't open \"" + path + "\": ", ex);
      } catch (ClassFinderException ex) {
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
    RegexFileFilter nameFilter =
        new RegexFileFilter("\\"+suffix+"$", FileFilterMatchType.FILENAME);
    AndFileFilter fileFilter = new AndFileFilter(nameFilter,
        new FileOnlyFilter());
    Collection<File> files = new ArrayList<File>();
    finder.findFiles(dir, fileFilter, files);
    return files;
  }

  private void loadJarClassPathEntries(File jarFile) {
    try {
      JarFile jar = new JarFile(jarFile);
      Manifest manifest = jar.getManifest();
      if (manifest == null)
        return;

      manifest.getEntries();
      Attributes attrs = manifest.getMainAttributes();
      Set<Object> keys = attrs.keySet();

      for (Object key : keys) {
        String value = (String) attrs.get(key);

        if (key.toString().equals("Class-Path")) {
          String jarName = jar.getName();
          log.trace("Adding Class-Path from jar " + jarName);

          StringBuilder buf = new StringBuilder();
          StringTokenizer tok = new StringTokenizer(value);
          while (tok.hasMoreTokens()) {
            buf.setLength(0);
            String element = tok.nextToken();
            String parent = jarFile.getParent();
            if (parent != null) {
              buf.append(parent);
              buf.append(File.separator);
            }

            buf.append(element);
          }

          String element = buf.toString();
          log.debug("From " + jarName + ": " + element);

          add(new File(element));
        }
      }
    } catch (IOException ex) {
      log.error("I/O error processing jar file \"" +
          jarFile.getPath() + "\"",
          ex);
    }
  }

  private void loadClassData(InputStream is, ClassVisitor classVisitor)
      throws ClassFinderException {
    try {
      ClassReader cr = new ClassReader(is);
      cr.accept(classVisitor, ClassInfo.ASM_CR_ACCEPT_CRITERIA);
    } catch (Exception ex) {
      throw new ClassFinderException( "Unable to load class from open input stream", ex);
    }
  }

  private boolean isJar(String fileName) {
    return fileName.toLowerCase().endsWith(".jar");
  }

  private boolean isZip(String fileName) {
    return fileName.toLowerCase().endsWith(".zip");
  }
}