package com.poolik.classfinder;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.poolik.classfinder.info.ClassInfo;
import com.poolik.classfinder.testClasses.TestInZip;
import com.poolik.classfinder.util.DirUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ClassFinderTest extends TestWithTestClasses {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();
  private Predicate<Path> excludeZip = new Predicate<Path>() {
    @Override
    public boolean apply(Path path) {
      return !path.getFileName().toString().contains("Zip");
    }
  };

  @Test
  public void findsClassesFromDirectory() throws IOException, URISyntaxException {
    copyTestClassesExcludingZip();
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(classesFolder.toFile());

    assertThat(classFinder.findClasses().size(), is(4));
  }

  @Test
  public void findsClassesFromManyDirectories() throws IOException, URISyntaxException {
    copyTestClassesExcludingZip();
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(Arrays.asList(classesFolder.toFile(), otherClassesFolder.toFile()));

    Collection<ClassInfo> classes = classFinder.findClasses();
    assertThat(classes.size(), is(8));
  }

  @Test
  public void clearsPlacesToLook() throws IOException, URISyntaxException {
    copyTestClassesExcludingZip();
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(classesFolder.toFile());

    assertThat(classFinder.findClasses().size(), is(4));
    classFinder.clear();
    assertThat(classFinder.findClasses().size(), is(0));
  }

  @Test
  public void looksForClassesWithinZipFilesOfDir() {
    ClassFinder classFinder = new ClassFinder();
    createZipTo(classesFolder.toFile());
    classFinder.add(classesFolder.toFile());

    assertThat(classFinder.findClasses().size(), is(5));
  }

  @Test
  public void looksForClassesWithinJarFilesOfDir() {
    ClassFinder classFinder = new ClassFinder();
    createJarTo(classesFolder.toFile());
    classFinder.add(classesFolder.toFile());

    assertThat(classFinder.findClasses().size(), is(5));
  }

  @Test
  public void findsClassesFromZip() {
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(createZipTo(new File(getTestFolder())));

    assertThat(classFinder.findClasses().size(), is(1));
  }

  @Test
  public void findsClassesFromJar() {
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(createJarTo(new File(getTestFolder())));

    assertThat(classFinder.findClasses().size(), is(1));
  }

  @Test(expected = ClassFinderException.class)
  public void throwsClassFinderExceptionIfNoResultsAndSetToThrow() {
    ClassFinder classFinder = new ClassFinder();
    classFinder.setErrorIfResultEmpty(true);

    classFinder.findClasses();
  }

  private void copyTestClassesExcludingZip() throws IOException, URISyntaxException {
    copyClasses("/com/poolik/classfinder/testClasses", classesFolder, excludeZip);
  }

  private File createJarTo(File parent) {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "classes.jar")
        .addClass(TestInZip.class)
        .addManifest();

    File target = new File(parent, "classes.jar");
    archive.as(ZipExporter.class).exportTo(
        target, true);
    return target;
  }

  private File createZipTo(File parent) {
    File zipFile = new File(parent, "classes.zip");
    ZipUtil.pack(classesFolder.toFile(), zipFile, new NameMapper() {
      @Override
      public String map(String name) {
        if (!name.contains("Zip")) return null;
        return name;
      }
    });
    return zipFile;
  }
}