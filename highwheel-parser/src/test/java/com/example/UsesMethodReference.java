package com.example;

import java.util.function.Supplier;

public class UsesMethodReference {

  public static void foo() {
    Supplier<Object> supplier= Foo::aMethod;
    supplier.get();
  }
}
