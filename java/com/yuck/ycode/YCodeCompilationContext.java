package com.yuck.ycode;

import com.google.common.base.Preconditions;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class YCodeCompilationContext {
  public class Scope implements Closeable {
    private Scope() {}

    @Override
    public void close() {
      Preconditions.checkArgument(scopes.get(scopes.size() - 1) == this);
      scopes.remove(scopes.size() - 1);
    }
  }

  List<Scope> scopes = new ArrayList<>();

  public Scope push() {
    Scope scope = new Scope();
    scopes.add(scope);
    return scope;
  }
}
