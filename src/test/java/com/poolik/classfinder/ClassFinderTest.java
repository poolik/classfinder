package com.poolik.classfinder;

import com.poolik.classfinder.filter.Subclass;
import com.poolik.classfinder.info.ClassInfo;
import com.poolik.classfinder.io.DirUtils;
import com.poolik.classfinder.io.Predicate;
import com.poolik.classfinder.otherTestClasses.AbstractClass;
import com.poolik.classfinder.otherTestClasses.ConcreteClass;
import com.poolik.classfinder.testClasses.TestInZip;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.jboss.shrinkwrap.api.container.ManifestContainer.DEFAULT_MANIFEST_NAME;
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
  private String classPath;

  @Before
  public void saveClasspath() {
    classPath = System.getProperty("java.class.path");
    System.setProperty("java.class.path", getTestFolder());
  }

  @After
  public void restoreClasspath() {
    System.setProperty("java.class.path", classPath);
  }

  @Test
  public void findsClassesFromDirectory() throws IOException, URISyntaxException {
    copyTestClassesExcludingZip();
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(classesFolder.toFile());

    assertThat(classFinder.findClasses().size(), is(4));
  }

  @Test
  public void findsClassesFromNestedDirectories() throws IOException, URISyntaxException {
    DirUtils.deleteIfExists(classesFolder);
    copyTestClassesExcludingZip(classesFolder.resolve("child" + File.separator + "inner"));
    ClassFinder classFinder = new ClassFinder();
    classFinder.add(classesFolder.toFile());

    Collection<ClassInfo> classes = classFinder.findClasses();
    assertThat(classes.size(), is(4));
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

  @Test
  public void findsClassesFromJarManifestClassPathJar() {
    ClassFinder classFinder = new ClassFinder();
    createJarTo(new File(getTestFolder()));
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "parent.jar")
        .addAsManifestResource(DEFAULT_MANIFEST_NAME);
    File target = new File(getTestFolder(), "parent.jar");
    archive.as(ZipExporter.class).exportTo(target, true);
    classFinder.add(target);

    Collection<ClassInfo> classes = classFinder.findClasses();
    assertThat(classes.size(), is(1));
  }

  @Test
  public void findsClassesFromJarManifestClassPathMultpleJars() {
    ClassFinder classFinder = new ClassFinder();
    createJarToContaining(new File(getTestFolder()), TestInZip.class, "classes1.jar");
    createJarToContaining(new File(getTestFolder()), ConcreteClass.class, "classes2.jar");

    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "parent.jar")
        .addAsManifestResource("MANIFEST_MULTIPLE.MF", DEFAULT_MANIFEST_NAME);
    File target = new File(getTestFolder(), "parent.jar");
    archive.as(ZipExporter.class).exportTo(target, true);
    classFinder.add(target);

    Collection<ClassInfo> classes = classFinder.findClasses();
    assertThat(classes.size(), is(2));
  }

  @Test
  public void findsClassesFromClassPath() {
    System.setProperty("java.class.path", getTestFolder());
    ClassFinder classFinder = new ClassFinder();
    classFinder.addClasspath();
    Collection<ClassInfo> classes = classFinder.findClasses(new Subclass(AbstractClass.class));

    assertThat(classes.size(), is(1));
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

  private void copyTestClassesExcludingZip(Path to) throws IOException, URISyntaxException {
    copyClasses("/com/poolik/classfinder/testClasses", to, excludeZip);
  }

  private File createJarTo(File parent) {
    return createJarToContaining(parent, TestInZip.class, "classes.jar");
  }

  private File createJarToContaining(File parent, Class<?> clazz, String fileName) {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, fileName)
        .addClass(clazz)
        .addManifest();

    File target = new File(parent, fileName);
    archive.as(ZipExporter.class).exportTo(target, true);
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