package org.pitest.highwheel.bytecodeparser;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;

public class DependencyFieldVisitor extends FieldVisitor {

  private final AccessPoint   parent;
  private final AccessVisitor typeReceiver;

  public DependencyFieldVisitor(final AccessPoint owner,
      final AccessVisitor typeReceiver) {
    super(Opcodes.ASM7, null);
    this.typeReceiver = typeReceiver;
    this.parent = owner;
  }
  
  @Override
  public AnnotationVisitor visitAnnotation(final String desc,
      final boolean visible) {
    this.typeReceiver.apply(
        this.parent,
        AccessPoint.create(ElementName.fromString(org.objectweb.asm.Type.getType(
            desc).getClassName())), AccessType.ANNOTATED);
    return null;
  }
  
}