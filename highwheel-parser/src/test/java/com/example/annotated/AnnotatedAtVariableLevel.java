package com.example.annotated;

public class AnnotatedAtVariableLevel {

  int foo() {
    @AnAnnotation
    int i = 0;
    
    return i++;
  }
}
