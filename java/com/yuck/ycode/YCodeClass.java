package com.yuck.ycode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class YCodeClass {
  public final String name;
  public final List<String> extensions;
  public List<String> fields = new ArrayList<>();
  public BiMap<String, Integer> methods = HashBiMap.create();

  public YCodeClass(String name) {
    this.name = name;
    extensions = new ArrayList<>();
  }

  public YCodeClass(String name, List<String> extensions) {
    this.name = name;
    this.extensions = extensions;
  }

  public void addField(String id) {
    fields.add(id);
  }

  public void addMethod(int functionId, String method) {
    methods.put(method, functionId);
  }

  public DataOutputStream write(DataOutputStream buffer) throws IOException {
    Utils.writeString(buffer, name);
    buffer.writeShort(fields.size());
    for (String field : fields) {
      Utils.writeString(buffer, field);
    }
    buffer.writeShort(methods.size());
    List<String> keys = methods.keySet().stream().sorted().collect(Collectors.toList());
    for (String key : keys) {
      Utils.writeString(buffer, key);
      buffer.writeShort(methods.get(key));
    }
    buffer.writeShort(extensions.size());
    for (String extension : extensions) {
      Utils.writeString(buffer, extension);
    }
    return buffer;
  }

  public static YCodeClass read(DataInputStream buffer) throws IOException {
    String name = Utils.readString(buffer);
    YCodeClass clazz = new YCodeClass(name);
    int numFields = buffer.readShort();
    for (int i = 0; i < numFields; i++) {
      clazz.fields.add(Utils.readString(buffer));
    }
    int numMethods = buffer.readShort();
    for (int i = 0; i < numMethods; i++) {
      clazz.methods.put(Utils.readString(buffer), (int) buffer.readShort());
    }
    int numExtensions = buffer.readShort();
    for (int i = 0; i < numExtensions; i++) {
      clazz.extensions.add(Utils.readString(buffer));
    }
    return clazz;
  }


  public List<YCodeClass> supers(YCodeFunction function) {
    List<YCodeClass> extending = new ArrayList<>();
    for (String extension : extensions) {
      for (YCodeClass clazz : function.classes.keySet()) {
        if (clazz.name.equals(extension)) {
          extending.add(clazz);
          break;
        }
      }
    }
    return extending;
  }
}
