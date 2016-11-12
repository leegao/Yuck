package com.yuck.auxiliary.descentparsing.annotations;

public @interface Resolve {
  String variable();
  String term();
  String[] conflicts() default {};
}
