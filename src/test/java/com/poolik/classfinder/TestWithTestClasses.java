package com.poolik.classfinder;

import com.poolik.classfinder.io.DirUtils;
import com.poolik.classfinder.io.Predicate;
import com.poolik.classfinder.io.Predicates;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class TestWithTestClasses {

  protected Path classesFolder;
  protected Path otherClassesFolder;

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Before
  public void createClasses() throws IOException, URISyntaxException {
    testFolder.create();
    classesFolder = Paths.get(getTestFolder(), "classes");
    otherClassesFolder = Paths.get(getTestFolder(), "otherClasses");
    copyClasses("/com/poolik/classfinder/testClasses", classesFolder);
    copyClasses("/com/poolik/classfinder/otherTestClasses", otherClassesFolder);
  }

  protected void copyClasses(String from, Path to) throws IOException, URISyntaxException {
    copyClasses(from, to, Predicates.<Path>alwaysTrue());
  }

  protected void copyClasses(String from, Path to, Predicate<Path> predicate) throws IOException, URISyntaxException {
    DirUtils.deleteIfExists(to);
    Path fromPath = Paths.get(getClass().getResource(from).toURI());
    DirUtils.copy(fromPath, to, predicate);
  }

  protected String getTestFolder() {
    return testFolder.getRoot().getAbsolutePath();
  }
}