package com.example.innerclasses;

import com.example.Foo;

public class CallsMethodFromFooWithinInnerClass {
  
  public void foo() {
    Runnable r = new Runnable() {
      public void run() {
        Foo.aMethod(); 
      }
    };
    r.run();
  }

}
