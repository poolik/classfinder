package com.poolik.classfinder.io;

import com.poolik.classfinder.io.visitor.SuffixFileVisitor;
import com.poolik.classfinder.io.visitor.CopyDirVisitor;
import com.poolik.classfinder.io.visitor.DeleteDirVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;

public class DirUtils {

  private DirUtils() {}

  public static void deleteIfExists(Path path) throws IOException {
    if (Files.exists(path)) {
      validate(path);
      Files.walkFileTree(path, new DeleteDirVisitor());
    }
  }

  public static void copy(Path from, Path to, Predicate<Path> copyPredicate) throws IOException {
    validate(from);
    Files.walkFileTree(from, EnumSet.of(FileVisitOption.FOLLOW_LINKS),Integer.MAX_VALUE,new CopyDirVisitor(from, to, copyPredicate));
  }

  public static Collection<File> findWithSuffix(Path from, String suffix) throws IOException {
    validate(from);
    SuffixFileVisitor visitor = new SuffixFileVisitor(suffix);
    Files.walkFileTree(from, visitor);
    return visitor.getFiles();
  }

  private static void validate(Path... paths) {
    for (Path path : paths) {
      Objects.requireNonNull(path);
      if (!Files.isDirectory(path)) {
        throw new IllegalArgumentException(String.format("%s is not a directory", path.toString()));
      }
    }
  }
}