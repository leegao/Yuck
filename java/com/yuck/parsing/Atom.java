package com.yuck.parsing;

public abstract class Atom {
  protected final String mLabel;

  protected Atom(String label) {
    mLabel = label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!getClass().equals(o.getClass())) return false;

    Atom atom = (Atom) o;

    return mLabel.equals(atom.mLabel);

  }

  @Override
  public int hashCode() {
    return mLabel.hashCode();
  }

  @Override
  public String toString() {
    return mLabel;
  }
}
