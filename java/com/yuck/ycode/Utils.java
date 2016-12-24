package com.yuck.ycode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Utils {
  public static DataOutputStream writeString(DataOutputStream buffer, String string) throws IOException {
    buffer.writeShort(string.length());
    buffer.write(string.getBytes());
    return buffer;
  }

  public static String readString(DataInputStream buffer) throws IOException {
    int length = buffer.readShort();
    byte[] bytes = new byte[length];
    buffer.read(bytes);
    return new String(bytes);
  }

  public static DataOutputStream writeConstant(DataOutputStream buffer, Object object) throws IOException {
    if (object instanceof Boolean) {
      buffer.writeByte(Constant.BOOL.ordinal());
      buffer.writeBoolean((boolean) object);
      return buffer;
    } else if (object instanceof Integer) {
      buffer.writeByte(Constant.INT.ordinal());
      buffer.writeInt((int) object);
      return buffer;
    } else if (object instanceof Float || object instanceof Double) {
      buffer.writeByte(Constant.FLOAT.ordinal());
      buffer.writeFloat((float) object);
      return buffer;
    } else if (object instanceof String) {
      buffer.writeByte(Constant.STRING.ordinal());
      return writeString(buffer, (String) object);
    } else {
      throw new IllegalStateException();
    }
  }

  public static Object readConstant(DataInputStream buffer) throws IOException {
    Constant constant = Constant.values()[buffer.readByte()];
    switch (constant) {
      case BOOL:
        return buffer.readBoolean();
      case INT:
        return buffer.readInt();
      case FLOAT:
        return buffer.readFloat();
      case STRING:
        return readString(buffer);
    }
    throw new IllegalStateException();
  }
}
