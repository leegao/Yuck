package com.yuck.ycode;

import java.nio.ByteBuffer;

public class Utils {
  public static ByteBuffer putString(ByteBuffer buffer, String string) {
    buffer.putInt(string.length());
    buffer.put(string.getBytes());
    return buffer;
  }

  public static ByteBuffer putConstant(ByteBuffer buffer, Object object) {
    if (object instanceof Boolean) {
      return buffer.put((byte) Constant.BOOL.ordinal()).put((byte) ((boolean) object ? 1 : 0));
    } else if (object instanceof Integer) {
      return buffer.put((byte) Constant.INT.ordinal()).putInt((int) object);
    } else if (object instanceof Float || object instanceof Double) {
      return buffer.put((byte) Constant.FLOAT.ordinal()).putFloat((float) object);
    } else if (object instanceof String) {
      return putString(buffer.put((byte) Constant.STRING.ordinal()), (String) object);
    } else {
      throw new IllegalStateException();
    }
  }
}
