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
import com.poolik.classfinder.resourceLoader.AdditionalResourceLoader;
import com.poolik.classfinder.resourceLoader.JarClasspathEntriesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static com.poolik.classfinder.info.FileUtil.fileCanContainClasses;

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
 *
 * <p>This package also contains a rich set of {@link com.poolik.classfinder.filter.ClassFilter}
 * implementations, including:</p>
 *
 * <ul>
 * <li>A {@link com.poolik.classfinder.filter.RegexClassFilter} for filtering class names on a regular
 * expression
 * <li>Filters for testing various class attributes (such as whether a
 * class is an interface, or a subclass of a known class, etc.
 * <li>Filters that can combine other filters in logical operations
 * </ul>
 *
 * <p>The following example illustrates how you might use a
 * <tt>ClassFinder</tt> to locate all non-abstract classes that implement
 * the <tt>ClassFilter</tt> interface, searching the classpath as well
 * as anything specified on the command line.</p>
 *
 * <blockquote><pre>
 * import com.poolik.classfinder.*;
 *
 * public class Test
 * {
 *     public static void main (String[] args) throws Throwable
 *     {
 *         ClassFinder finder = new ClassFinder();
 *         for (String arg : args)
 *             finder.add(new File(arg));
 *
 *         ClassFilter filter =
 *             new AndClassFilter
 *                 // Must not be an interface
 *                 (new NotClassFilter (new InterfaceOnlyClassFilter()),
 *
 *                 // Must implement the ClassFilter interface
 *                 new SubclassClassFilter (ClassFilter.class),
 *
 *                 // Must not be abstract
 *                 new NotClassFilter (new AbstractClassFilter()));
 *
 *         Collection&lt;ClassInfo&gt; foundClasses = finder.findClasses(filter);
 *
 *         for (ClassInfo classInfo : foundClasses)
 *             System.out.println ("Found " + classInfo.getClassName());
 *     }
 * }
 * </pre></blockquote>
 * <p>This class, and the {@link com.poolik.classfinder.info.ClassInfo} class, rely on the ASM
 * byte-code manipulation library. If that library is not available, this
 * package will not work. See
 * <a href="http://asm.objectweb.org"><i>asm.objectweb.org</i></a>
 * for details on ASM.</p>
 *
 * <p><b>WARNING: This class is not thread-safe.</b></p>
 *
 * @author Copyright &copy; 2006 Brian M. Clapper
 * @version <tt>$Revision$</tt>
 */
public class  ClassFinder {

  private Map<String, File> placesToSearch = new LinkedHashMap<>();
  private static Collection<AdditionalResourceLoader> resourceLoaders = Arrays.<AdditionalResourceLoader>asList(new JarClasspathEntriesLoader());
  private static final Logger log = LoggerFactory.getLogger(ClassFinder.class);
  private boolean errorIfResultEmpty;

  /**
   * Add the contents of the system classpath for classes.
   */
  public void addClassPath() {
    try {
      String path = System.getProperty("java.class.path");
      StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);
      while (tok.hasMoreTokens())
        add(new File(tok.nextToken()));
    } catch (Exception ex) {
      log.error("Unable to get class path", ex);
    }
  }

  /**
   * Add a jar file, zip file or directory to the list of places to search
   * for classes.
   *
   * @param file the jar file, zip file or directory
   * @return this
   */
  public ClassFinder add(File file) {
    log.info("Adding file to look into: " + file.getAbsolutePath());

    if (fileCanContainClasses(file)) {
      String absPath = file.getAbsolutePath();
      if (placesToSearch.get(absPath) == null) {
        placesToSearch.put(absPath, file);
        for (AdditionalResourceLoader resourceLoader : resourceLoaders) {
          if (resourceLoader.canLoadAdditional(file)) resourceLoader.loadAdditional(file, this);
        }
      }
    } else {
      log.info("The given path '" + file.getAbsolutePath() + "' cannot contain classes!");
    }

    return this;
  }

  /**
   * Add an array jar files, zip files and/or directories to the list of
   * places to search for classes.
   *
   * @param files the array of jar files, zip files and/or directories.
   *              The array can contain a mixture of all of the above.
   * @return this
   */
  public ClassFinder add(File[] files) {
    return add(Arrays.asList(files));
  }

  /**
   * Add a <tt>Collection</tt> of jar files, zip files and/or directories
   * to the list of places to search for classes.
   *
   * @param files the collection of jar files, zip files and/or directories.
   * @return this
   */
  public ClassFinder add(Collection<File> files) {
    for (File file : files)
      add(file);

    return this;
  }

  /**
   * Clear the finder's notion of where to search.
   */
  public void clear() {
    placesToSearch.clear();
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
   * @return Collection of found classes
   */
  public Collection<ClassInfo> findClasses(ClassFilter filter) {
    Map<String,ClassInfo> foundClasses = new ParallelClassLoader().loadClassesFrom(placesToSearch.values());
    log.info("Loaded " + foundClasses.size() + " classes.");

    Collection<ClassInfo> filteredClasses = filterClasses(filter, foundClasses);

    if (filteredClasses.size() == 0 && errorIfResultEmpty) {
      log.warn("Found no classes, throwing exception");
      throw new ClassFinderException("Didn't find any classes");
    } else {
      log.info("Returning " + filteredClasses.size() + " total classes");
    }
    foundClasses.clear();
    return filteredClasses;
  }

  private Collection<ClassInfo> filterClasses(ClassFilter filter, Map<String, ClassInfo> foundClasses) {
    Collection<ClassInfo> classes = new ArrayList<>();
    for (ClassInfo classInfo : foundClasses.values()) {
      String className = classInfo.getClassName();
      String locationName = classInfo.getClassLocation().getPath();
      log.trace("Looking at " + locationName + " (" + className + ")");
      if ((filter == null) || (filter.accept(classInfo, new ClassHierarchyResolver(foundClasses)))) {
        log.trace("Filter accepted " + className);
        classes.add(classInfo);
      } else {
        log.trace("Filter rejected " + className);
      }
    }
    return classes;
  }
}