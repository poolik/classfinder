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

package com.poolik.classfinder.filter;

import com.poolik.classfinder.ClassHierarchyResolver;
import com.poolik.classfinder.info.ClassInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>An <tt>Or</tt> contains logically ORs other
 * {@link ClassFilter} objects. When its {@link #accept accept()}
 * method is called, the <tt>Or</tt> object passes
 * the class name through the contained filters. The class name is
 * accepted if it is accepted by any one of the contained filters. This
 * class conceptually provides a logical "OR" operator for class name
 * filters.</p>
 * 
 * <p>The contained filters are applied in the order they were added to
 * the <tt>Or</tt> object. This class's
 * {@link #accept accept()} method stops looping over the contained filters
 * as soon as it encounters one whose <tt>accept()</tt> method returns
 * <tt>true</tt> (implementing a "short-circuited OR" operation.) </p>
 *
 * @author Copyright &copy; 2006 Brian M. Clapper
 * @version <tt>$Revision$</tt>
 * @see ClassFilter
 * @see Or
 * @see Not
 * @see com.poolik.classfinder.ClassFinder
 */
public final class Or implements ClassFilter {
  private List<ClassFilter> filters = new LinkedList<>();

  public static Or anyOf(ClassFilter... filters) {
    return new Or(filters);
  }

  public Or(ClassFilter... filters) {
    for (ClassFilter filter : filters)
      addFilter(filter);
  }

  public Or addFilter(ClassFilter filter) {
    filters.add(filter);
    return this;
  }

  public Or removeFilter(ClassFilter filter) {
    filters.remove(filter);
    return this;
  }

  /**
   * <p>Determine whether a class name is to be accepted or not, based on
   * the contained filters. The class name name is accepted if any
   * one of the contained filters accepts it. This method stops
   * looping over the contained filters as soon as it encounters one
   * whose {@link ClassFilter#accept accept()} method returns
   * <tt>true</tt> (implementing a "short-circuited OR" operation.)</p>
   * 
   * <p>If the set of contained filters is empty, then this method
   * returns <tt>true</tt>.</p>
   *
   * @param classInfo   the {@link com.poolik.classfinder.info.ClassInfo} object to test
   * @return <tt>true</tt> if the name matches, <tt>false</tt> if it doesn't
   */
  public boolean accept(ClassInfo classInfo, ClassHierarchyResolver hierarchyResolver) {
    boolean accepted = false;

    if (filters.size() == 0) accepted = true;
    else {
      for (ClassFilter filter : filters) {
        accepted = filter.accept(classInfo, hierarchyResolver);
        if (accepted) break;
      }
    }
    return accepted;
  }
}