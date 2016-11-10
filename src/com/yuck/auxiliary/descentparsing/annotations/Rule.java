package com.yuck.auxiliary.descentparsing.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {
  String rule();
  Class<?> type() default Object.class;
}
