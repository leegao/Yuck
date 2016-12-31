package com.yuck.interpreter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.*;

public class YuckList extends YuckObject {
  public YuckList(InterpreterContext context) {
    super(context);
  }

  @Override
  public YuckObjectKind getKind() {
    return YuckObjectKind.LIST;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YuckList yuckList = (YuckList) o;
    return Objects.equals(list, yuckList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  public final List<YuckObject> list = new ArrayList<>();

  public void add(YuckObject yuckObject) {
    list.add(yuckObject);
  }

  @Override
  public YuckObject tableLoad(YuckObject key) {
    if (key instanceof YuckInteger) {
      return list.get(((YuckInteger) key).number);
    }
    return super.tableLoad(key);
  }

  @Override
  public void tableStore(YuckObject key, YuckObject val) {
    if (key instanceof YuckInteger) {
      list.set(((YuckInteger) key).number, val);
      return;
    }
    super.tableStore(key, val);
  }

  @Override
  public boolean isFilled() {
    return list.size() != 0;
  }


  @Override
  public String toString() {
    return Objects.toString(list);
  }

  public YuckObject iterator(InterpreterContext context) {
    List<YuckObject> copy = Lists.newArrayList(list);
    Iterator<YuckObject> iterator = copy.iterator();
    Map<String, YuckObject> map = new HashMap<>();
    map.put(
        "hasNext", new NativeFunction(c -> new YuckBoolean(iterator.hasNext(), c), context)
    );
    map.put(
        "next", new NativeFunction(c -> iterator.next(), context)
    );
    return new YuckModule(map, context);
  }

  public YuckObject add(InterpreterContext context) {
    Preconditions.checkArgument(context.locals.size() > 0);
    list.add(context.get(0));
    return this;
  }

  @Override
  public YuckObject getField(String field) {
    if (field.equals("@iterator")) {
      return new NativeFunction(this::iterator, context);
    } else if (field.equals("append")) {
      return new NativeFunction(this::add, context);
    }
    return super.getField(field);
  }
}
