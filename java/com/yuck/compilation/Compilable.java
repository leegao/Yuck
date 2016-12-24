package com.yuck.compilation;

public interface Compilable<T> {
  T compile(T function, YCodeCompilationContext context);
}
